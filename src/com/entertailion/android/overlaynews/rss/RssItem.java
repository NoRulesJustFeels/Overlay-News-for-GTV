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

import java.util.Date;

import android.graphics.Bitmap;

/**
 * Data structure for RSS item. See RssFeed.
 * 
 * @author leon_nicholls
 *
 */
public class RssItem implements Comparable<RssItem> {
	private int id;
	private String title;
	private String link;
	private String description;
	private String content;
	private Date date;
	private Bitmap bitmap;

	public RssItem(int id, String title, String link, String description, String content, Date date) {
		this.id = id;
		this.title = title;
		this.link = link;
		this.description = description;
		this.content = content;
		this.date = date;
	}

	public RssItem() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public int compareTo(RssItem other) {
		if (getDate() != null && other.getDate() != null) {
			return getDate().compareTo(other.getDate());
		} else {
			return 0;
		}
	}
	
	// cache the feed logo bitmap
	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}


}
