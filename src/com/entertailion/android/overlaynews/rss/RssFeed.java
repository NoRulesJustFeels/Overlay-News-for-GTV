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
package com.entertailion.android.overlaynews.rss;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.graphics.Bitmap;

import com.entertailion.android.overlaynews.R;

/**
 * Data structure for RSS feed
 * 
 * @author leon_nicholls
 *
 */
public class RssFeed implements Comparable<RssFeed> {
	private int id;
	private String title;
	private String link;
	private String description;
	private Date date;
	private Date viewDate;
	private ArrayList<RssItem> items;
	private String logo;
	private String image;
	private Bitmap bitmap;
	private int ttl;

	public RssFeed(int id, String title, String link, String description, Date date, Date viewDate, String logo, String image, Bitmap bitmap, int ttl) {
		this();
		this.id = id;
		this.title = title;
		this.link = link;
		this.description = description;
		this.date = date;
		this.viewDate = viewDate;
		this.logo = logo;
		this.image = image;
		this.bitmap = bitmap;
		this.ttl = ttl;
	}

	public RssFeed() {
		this.items = new ArrayList<RssItem>();
		this.date = new Date(0);
		this.viewDate = new Date(0);
		this.ttl = -1;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ArrayList<RssItem> getItems() {
		return items;
	}

	public void addItem(RssItem item) {
		items.add(item);
	}

	public Date getViewDate() {
		return viewDate;
	}

	public void setViewDate(Date viewDate) {
		this.viewDate = viewDate;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	@Override
	public int compareTo(RssFeed other) {
		if (getDate() != null && other.getDate() != null) {
			return getDate().compareTo(other.getDate());
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {
		return title;
	}

	public static Integer getSiteIcon(String url) {
		Set<String> keySet = websiteIconMap.keySet();
		for (String key : keySet) {
			if (url.contains(key)) {
				return websiteIconMap.get(key);
			}
		}
		return null;
	}

	/**
	 * Cache of local copies of the icons for popular news web sites.
	 */
	private static Map<String, Integer> websiteIconMap = new HashMap<String, Integer>();
	static {
		websiteIconMap.put("google", R.drawable.site_google);
		websiteIconMap.put("techmeme", R.drawable.site_techmeme);
		websiteIconMap.put("yahoo", R.drawable.site_yahoo);
		websiteIconMap.put("espn", R.drawable.site_espn);
		websiteIconMap.put("engadget", R.drawable.site_engadget);
		websiteIconMap.put("boingboing", R.drawable.site_boingboing);
		websiteIconMap.put("techcrunch", R.drawable.site_techcrunch);
		websiteIconMap.put("simplyrecipes", R.drawable.site_simplyrecipes);
		websiteIconMap.put("xataka", R.drawable.site_xataka);
		websiteIconMap.put("mashable", R.drawable.site_mashable);
		websiteIconMap.put("cnn", R.drawable.site_cnn);
		websiteIconMap.put("nbc", R.drawable.site_nbc);
		websiteIconMap.put("nytimes", R.drawable.site_nytimes);
		websiteIconMap.put("huffingtonpost", R.drawable.site_huffingtonpost);
		websiteIconMap.put("foxnews", R.drawable.site_foxnews);
		websiteIconMap.put("washingtonpost", R.drawable.site_washingtonpost);
		websiteIconMap.put("latimes", R.drawable.site_latimes);
		websiteIconMap.put("dailymail", R.drawable.site_dailymail);
		websiteIconMap.put("reuters", R.drawable.site_reuters);
		websiteIconMap.put("abcnews", R.drawable.site_abcnews);
		websiteIconMap.put("usatoday", R.drawable.site_usatoday);
		websiteIconMap.put("bbc", R.drawable.site_bbc);
		websiteIconMap.put("drudgereport", R.drawable.site_drudgereport);
	}

}
