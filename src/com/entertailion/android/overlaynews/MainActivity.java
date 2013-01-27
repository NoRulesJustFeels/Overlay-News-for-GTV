/*
 * Copyright (C) 2013 ENTERTAILION, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.entertailion.android.overlaynews;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.entertailion.android.overlaynews.database.FeedsTable;
import com.entertailion.android.overlaynews.database.ItemsTable;
import com.entertailion.android.overlaynews.rss.RssFeed;
import com.entertailion.android.overlaynews.rss.RssItem;
import com.entertailion.android.overlaynews.utils.Analytics;
import com.entertailion.android.overlaynews.utils.Utils;

/**
 * Main activity to display the overlay news headlines. Each news headline
 * (RssItem) is displayed at the bottom of the screen for a configurable amount
 * of time.
 * 
 * @author leon_nicholls
 * 
 */
public class MainActivity extends Activity {
	private static final String LOG_TAG = "MainActivity";
	private boolean finished = false;
	private ArrayList<RssItem> items = new ArrayList<RssItem>();
	private RssItem currentItem;
	private Handler handler = new Handler();
	private TextView textView1, textView2;
	private ImageView bottomGradient, icon;
	private Animation moveUp, moveDown, moveImageDown, fadeOut;
	private int counter;
	private String titleText;
	private static BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
	private int delay, duration;
	private long startTime;

	private ServiceConnection downloadServiceConnection;
	private DownloadService downloadService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		connectToDownloadService();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		textView1 = (TextView) findViewById(R.id.title1);
		textView1.setTextColor(getResources().getColor(R.color.app_name));
		textView2 = (TextView) findViewById(R.id.title2);
		textView2.setTextColor(getResources().getColor(R.color.app_name));
		bottomGradient = (ImageView) findViewById(R.id.bottom_gradient);
		icon = (ImageView) findViewById(R.id.icon);

		moveUp = AnimationUtils.loadAnimation(this, R.anim.move_up);
		moveImageDown = AnimationUtils.loadAnimation(this, R.anim.move_down);
		moveDown = AnimationUtils.loadAnimation(this, R.anim.move_down);
		moveDown.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				icon.setVisibility(View.INVISIBLE);
				textView1.setVisibility(View.INVISIBLE);
				textView2.setVisibility(View.INVISIBLE);
				bottomGradient.setVisibility(View.INVISIBLE);
				doFinish();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}

		});
		fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		fadeOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				getTextView().setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}

		});

		// get configuration settings; see ConfigActivity
		final SharedPreferences prefs = getSharedPreferences(ConfigActivity.PREFS_NAME, Context.MODE_PRIVATE);
		delay = prefs.getInt(ConfigActivity.PREFERENCE_DELAY, ConfigActivity.PREFERENCE_DELAY_DEFAULT) * 1000;
		duration = prefs.getInt(ConfigActivity.PREFERENCE_DURATION, ConfigActivity.PREFERENCE_DURATION_DEFAULT) * 1000;

		// Tell other overlay apps
		((OverlayApplication) getApplicationContext()).setOverlayState(OutgoingReceiver.OVERLAY_INTENT_STATE_STARTED);

		new Thread(new Runnable() {
			public void run() {
				long currentTime = System.currentTimeMillis();
				// iterate through feeds in database
				items.clear();
				ArrayList<RssFeed> feeds = FeedsTable.getFeeds(MainActivity.this);
				if (feeds != null) {
					for (RssFeed feed : feeds) {
						Log.d(LOG_TAG, "feed=" + feed.getTitle() + ", image=" + feed.getImage() + ", link=" + feed.getLink() + ", date=" + feed.getDate()
								+ ", viewDate=" + feed.getViewDate());
						long viewDateTime = feed.getViewDate().getTime();
						ArrayList<RssItem> feedItems = ItemsTable.getItems(MainActivity.this, feed.getId());
						if (feedItems != null && feedItems.size() > 0) {
							Integer resourceId = RssFeed.getSiteIcon(feed.getLink());
							if (resourceId != null) {
								feed.setBitmap(loadBitmap(resourceId));
							} else if (feed.getImage() != null) {
								try {
									FileInputStream fis = MainActivity.this.openFileInput(feed.getImage());
									Bitmap bitmap = BitmapFactory.decodeStream(fis);
									fis.close();
									feed.setBitmap(bitmap);
									Log.d(LOG_TAG, "logo=" + feed.getLogo());
								} catch (Exception e) {
									Log.d(LOG_TAG, "getFeeds", e);
								}
							}
							for (RssItem item : feedItems) {
								// check if item already displayed
								if (item.getDate().getTime() > viewDateTime) {
									item.setBitmap(feed.getBitmap());
									items.add(item);
									feed.setViewDate(new Date(currentTime));
								} else {
									Log.d(LOG_TAG, "already checked: " + item.getTitle());
								}
							}
						}
						try {
							// update feed last view date
							FeedsTable.updateFeed(MainActivity.this, feed.getId(), feed.getTitle(), feed.getLink(), feed.getDescription(), feed.getDate()
									.getTime(), feed.getViewDate().getTime(), feed.getLogo(), feed.getImage(), feed.getTtl());
						} catch (Exception e) {
							Log.e(LOG_TAG, "onCreate", e);
						}
					}
					if (items.size()>0) {
						// sort by date
						Collections.sort(items, new Comparator<RssItem>() {
	
							@Override
							public int compare(RssItem lhs, RssItem rhs) {
								// newest dates first
								int comparison = lhs.getDate().compareTo(rhs.getDate());
								if (comparison < 0) {
									return 1;
								} else if (comparison > 0) {
									return -1;
								}
								return comparison;
							}
	
						});
						startTime = System.currentTimeMillis();
						handler.post(new Runnable() {
							public void run() {
								postNextItem();
							}
						});
					} else {
						Log.d(LOG_TAG, "no new news");
						doFinish();
					}
				}
			}
		}).start();
		bottomGradient.setVisibility(View.INVISIBLE);

		// Set the context for Google Analytics
		Analytics.createAnalytics(this);
		Utils.logDeviceInfo(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		disconnectFromDownloadService();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Start Google Analytics for this activity
		Analytics.startAnalytics(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Stop Google Analytics for this activity
		Analytics.stopAnalytics(this);
	}

	@Override
	protected void onResume() {
		Log.d(LOG_TAG, "onResume");
		super.onResume();

		Analytics.logEvent(Analytics.OVERLAY_NEWS_MAIN);
	}

	/**
	 * Connect to the background service.
	 */
	private void connectToDownloadService() {
		downloadServiceConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(LOG_TAG, "onServiceConnected");
				downloadService = ((DownloadService.LocalBinder) service).getService();
			}

			public void onServiceDisconnected(ComponentName name) {
				Log.d(LOG_TAG, "onServiceDisconnected");
				downloadService = null;
			}
		};
		if (downloadServiceConnection != null) {
			Intent intent = new Intent(MainActivity.this, DownloadService.class);
			bindService(intent, downloadServiceConnection, BIND_AUTO_CREATE);
		}
	}

	/**
	 * Close the connection to the background service.
	 */
	private synchronized void disconnectFromDownloadService() {
		if (downloadServiceConnection != null) {
			unbindService(downloadServiceConnection);
			downloadServiceConnection = null;
		}
		downloadService = null;
	}

	private void postNextItem() {
		long timeDelta = System.currentTimeMillis() - startTime;
		if (counter < items.size() && timeDelta < duration) {
			currentItem = items.get(counter);
			icon.setImageBitmap(currentItem.getBitmap());
			titleText = currentItem.getTitle();
			if (counter == 0) {
				getTextView().setText(titleText);
				bottomGradient.setVisibility(View.VISIBLE);
				bottomGradient.startAnimation(moveUp);
				getTextView().startAnimation(moveUp);
				icon.startAnimation(moveUp);
			} else {
				getTextView().setText(titleText);
				getTextView().setVisibility(View.VISIBLE);
				getTextView().startAnimation(moveUp);
				getOtherTextView().startAnimation(fadeOut);
			}
			handler.postDelayed(new Runnable() {
				public void run() {
					postNextItem();
				}
			}, delay);
			counter++;
		} else {
			bottomGradient.startAnimation(moveImageDown);
			icon.startAnimation(moveImageDown);
			getOtherTextView().startAnimation(moveDown);
			getTextView().setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Finish activity when the user interacts
	 */
	protected void doFinish() {
		currentItem = null;
		synchronized (this) {
			if (!finished) {
				finished = true;
				new Thread(new Runnable() {

					@Override
					public void run() {
						((OverlayApplication) getApplicationContext()).setOverlayState(OutgoingReceiver.OVERLAY_INTENT_STATE_STOPPED);
						MainActivity.this.finish();
					}

				}).start();
			}
		}
	}

	/**
	 * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER: {
			// Load current item in browser
			if (currentItem != null) {
				// show feedback
				textView1.setTextColor(getResources().getColor(R.color.white));
				textView2.setTextColor(getResources().getColor(R.color.white));
				// load browser
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(currentItem.getLink()));
				startActivity(intent);
			}

			return true;
		}
		}
		// dismiss for all other keys
		doFinish();
		return super.dispatchKeyEvent(e);
	};

	/**
	 * @see android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent e) {
		doFinish();
		return super.dispatchTouchEvent(e);
	};

	/**
	 * @see android.app.Activity#dispatchGenericMotionEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean dispatchGenericMotionEvent(MotionEvent e) {
		doFinish();
		return super.dispatchGenericMotionEvent(e);
	};

	/**
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (!hasFocus) {
			doFinish();
		}
	}

	private TextView getTextView() {
		return counter % 2 == 1 ? textView2 : textView1;
	}

	private TextView getOtherTextView() {
		return counter % 2 == 1 ? textView1 : textView2;
	}

	/**
	 * Loads a bitmap from a resource and converts it to a bitmap.
	 * 
	 * @param context
	 *            The application context.
	 * @param resourceId
	 *            The id of the resource to load.
	 * @return A bitmap containing the image contents of the resource, or null
	 *         if there was an error.
	 */
	protected Bitmap loadBitmap(int resourceId) {
		bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bitmap = null;
		InputStream is = getResources().openRawResource(resourceId);
		try {
			bitmap = BitmapFactory.decodeStream(is, null, bitmapOptions);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// Ignore.
			}
		}

		return bitmap;
	}

}
