/*
 * Copyright (C) 2013 ENTERTAILION LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.entertailion.android.overlaynews;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.entertailion.android.overlaynews.database.FeedsTable;
import com.entertailion.android.overlaynews.database.ItemsTable;
import com.entertailion.android.overlaynews.rss.RssFeed;
import com.entertailion.android.overlaynews.rss.RssHandler;
import com.entertailion.android.overlaynews.rss.RssItem;
import com.entertailion.android.overlaynews.utils.Utils;

public class Downloader {
	private static final String LOG_TAG = "Downloader";
	private static int TIMER_REPEAT = 1000 * 60 * 11;

	private Context context;
	private Timer timer;
	private long updateTime;

	public Downloader(Context context) {
		this.context = context;
		try {
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					if (System.currentTimeMillis() - updateTime > TIMER_REPEAT) {
						getFeeds();
					}
				}
			}, 0, TIMER_REPEAT);
		} catch (Exception e) {
			Log.e(LOG_TAG, "Downloader timer", e);
		}
	}

	public void getFeeds() {
		Log.d(LOG_TAG, "getFeeds");
		new Thread() {
			public void run() {
				updateFeeds();
			}
		}.start();
	}

	private synchronized void updateFeeds() {
		Log.d(LOG_TAG, "updateFeeds");
		updateTime = System.currentTimeMillis();
		try {
			// iterate through feeds in database
			ArrayList<RssFeed> feeds = FeedsTable.getFeeds(context);
			Log.d(LOG_TAG, "feeds=" + feeds);
			if (feeds != null) {
				for (RssFeed feed : feeds) {
					// Check if TTL set for feed
					if (feed.getTtl() != -1) {
						if (System.currentTimeMillis() - (feed.getDate().getTime() + feed.getTtl() * 60 * 1000) < 0) {
							// too soon
							Log.d(LOG_TAG, "TTL not reached: " + feed.getTitle());
							break;
						}
					}
					String rss = Utils.getRssFeed(feed.getLink(), context, true);
					RssHandler rh = new RssHandler();
					RssFeed rssFeed = rh.getFeed(rss);
					if (rssFeed.getTitle()==null) {
						try {
							Uri uri = Uri.parse(feed.getLink());
							rssFeed.setTitle(uri.getHost());
						} catch (Exception e) {
							Log.e(LOG_TAG, "get host", e);
						}
					}
					Uri uri = Uri.parse(feed.getLink());
					Log.d(LOG_TAG, "host=" + uri.getScheme() + "://" + uri.getHost());
					String icon = Utils.getWebSiteIcon(context, "http://" + uri.getHost());
					Log.d(LOG_TAG, "icon1=" + icon);
					if (icon == null) {
						// try base host address
						int count = StringUtils.countMatches(uri.getHost(), ".");
						if (count > 1) {
							int index = uri.getHost().indexOf('.');
							String baseHost = uri.getHost().substring(index + 1);
							icon = Utils.getWebSiteIcon(context, "http://" + baseHost);
							Log.d(LOG_TAG, "icon2=" + icon);
						}
					}
					if (icon != null) {
						try {
							FileInputStream fis = context.openFileInput(icon);
							Bitmap bitmap = BitmapFactory.decodeStream(fis);
							fis.close();
							rssFeed.setImage(icon);
							rssFeed.setBitmap(bitmap);
						} catch (Exception e) {
							Log.d(LOG_TAG, "updateFeeds", e);
						}
					}
					if (rssFeed.getBitmap() == null && rssFeed.getLogo() != null) {
						Log.d(LOG_TAG, "logo=" + rssFeed.getLogo());
						Bitmap bitmap = Utils.getBitmapFromURL(rssFeed.getLogo());
						if (bitmap != null) {
							icon = Utils.WEB_SITE_ICON_PREFIX + Utils.clean(rssFeed.getLogo()) + ".png";
							Utils.saveToFile(context, bitmap, bitmap.getWidth(), bitmap.getHeight(), icon);
							rssFeed.setImage(icon);
							rssFeed.setBitmap(bitmap);
						}
					}
					// update database
					long time = 0;
					if (rssFeed.getDate() != null) {
						time = rssFeed.getDate().getTime();
					}
					FeedsTable.updateFeed(context, feed.getId(), rssFeed.getTitle(), feed.getLink(), rssFeed.getDescription(), time, rssFeed.getViewDate().getTime(), rssFeed.getLogo(),
							rssFeed.getImage(), rssFeed.getTtl());
					ItemsTable.deleteItems(context, feed.getId());
					for (RssItem item : rssFeed.getItems()) {
						if (item.getTitle()!=null) {
							time = 0;
							if (item.getDate() != null) {
								time = item.getDate().getTime();
							}
							ItemsTable.insertItem(context, feed.getId(), item.getTitle(), item.getLink(), item.getDescription(), item.getContent(), time);
						}
					}
					// release resources
					Bitmap bitmap = rssFeed.getBitmap();
					if (bitmap != null) {
						rssFeed.setBitmap(null);
						bitmap.recycle();
						bitmap = null;
					}
				}
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "updateFeeds", e);
		}
	}

	public void destroy() {
		if (timer != null) {
			timer.cancel();
		}
	}

}
