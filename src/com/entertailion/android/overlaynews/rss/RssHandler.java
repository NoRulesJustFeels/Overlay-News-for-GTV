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

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * Parser for RSS feed. See RssFeed.
 * 
 * @author leon_nicholls
 *
 */
public class RssHandler extends DefaultHandler {
	private static final String LOG_CAT = "Handler";

	private RssFeed feed;
	private RssItem item;
	private StringBuilder chars = new StringBuilder();
	private boolean isItem = false;
	private boolean isImage = false;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

	public RssFeed getFeed() {
		return feed;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		chars.delete(0, chars.length());

		if (qName.equalsIgnoreCase("image")) {
			if (!isItem) {
				isImage = true;
			}
		} else if (qName.equalsIgnoreCase("item")) {
			isItem = true;
			item = new RssItem();
			feed.addItem(item);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		chars.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if (localName.equalsIgnoreCase("image")) {
			if (!isItem) {
				isImage = false;
			}
		} else if (localName.equalsIgnoreCase("url")) {
			if (isImage) {
				feed.setLogo(StringEscapeUtils.unescapeXml(chars.toString()));
			}
		} else if (localName.equalsIgnoreCase("ttl")) {
			if (!isItem) {
				feed.setTtl(Integer.parseInt(StringEscapeUtils.unescapeXml(chars.toString())));
			}
		} else if (localName.equalsIgnoreCase("title")) {
			if (!isImage) {
				if (isItem) {
					item.setTitle(StringEscapeUtils.unescapeXml(chars.toString()));
				} else {
					feed.setTitle(StringEscapeUtils.unescapeXml(chars.toString()));
				}
			}
		} else if (localName.equalsIgnoreCase("description")) {
			if (isItem) {
				item.setDescription(StringEscapeUtils.unescapeXml(chars.toString()));
			} else {
				feed.setDescription(StringEscapeUtils.unescapeXml(chars.toString()));
			}
		} else if (localName.equalsIgnoreCase("pubDate")) {
			if (isItem) {
				try {
					item.setDate(dateFormat.parse(StringEscapeUtils.unescapeXml(chars.toString())));
				} catch (Exception e) {
					Log.e(LOG_CAT, "pubDate", e);
				}
			} else {
				try {
					feed.setDate(dateFormat.parse(StringEscapeUtils.unescapeXml(chars.toString())));
				} catch (Exception e) {
					Log.e(LOG_CAT, "pubDate", e);
				}
			}
		} else if (localName.equalsIgnoreCase("item")) {
			isItem = false;
		} else if (localName.equalsIgnoreCase("link")) {
			if (isItem) {
				item.setLink(StringEscapeUtils.unescapeXml(chars.toString()));
			} else {
				feed.setLink(StringEscapeUtils.unescapeXml(chars.toString()));
			}
		}

		if (localName.equalsIgnoreCase("item")) {
		}

	}

	public RssFeed getFeed(String data) {
		feed = null;
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			InputSource inStream = new org.xml.sax.InputSource();
			inStream.setCharacterStream(new StringReader(data));

			xr.setContentHandler(this);
			feed = new RssFeed();
			xr.parse(inStream);

			xr = null;
			sp = null;
			spf = null;
		} catch (Exception e) {
			Log.e(LOG_CAT, "getFeed", e);
		}

		return feed;
	}

}