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
package com.entertailion.android.overlaynews.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.entertailion.android.overlaynews.R;

/**
 * Utility functions. 
 * 
 * @author leon_nicholls
 *
 */
public class Utils {
	private static final String LOG_TAG = "Utils";

	private static final String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

	public static final String WEB_SITE_ICON_PREFIX = "web_site_icon_";

	public static String getRssFeed(String url, Context context, boolean refresh) {
		String rss = Utils.getCachedData(context, url, refresh);
		if (rss != null) {
			rss = rss.trim();
			if (rss.startsWith(XML_PREFIX)) {
				return rss;
			} else {
				try {
					Document doc = Jsoup.parse(rss);
					Element link = doc.select("link[type=application/rss+xml]").first();
					if (link != null && link.attr("rel").equalsIgnoreCase("alternate")) {
						String href = link.attr("href");
						if (href != null) {
							rss = Utils.getCachedData(context, href, refresh);
							return rss;
						}
					}
				} catch (Exception e) {
					Log.e(LOG_TAG, "Jsoup exception", e);
				}
			}
		}
		return rss;
	}

	public synchronized static final String getCachedData(Context context, String url, boolean refresh) {
		Log.d(LOG_TAG, "getCachedData: " + url);
		String data = null;
		boolean exists = false;
		String cleanUrl = "cache." + clean(url);
		File file = context.getFileStreamPath(cleanUrl);
		if (file != null && file.exists()) {
			exists = true;
		}

		if (!refresh && exists) {
			try {
				FileInputStream fis = context.openFileInput(cleanUrl);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				StringBuffer buffer = new StringBuffer();
				for (String line; (line = br.readLine()) != null;) {
					buffer.append(line);
				}
				fis.close();
				data = buffer.toString();
			} catch (Exception e) {
				Log.e(LOG_TAG, "Error getData: " + url, e);
			}
		} else {
			boolean found = false;
			StringBuilder builder = new StringBuilder();
			try {
				InputStream stream = new HttpRequestHelper().getHttpStream(url);
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				stream.close();
				found = true;
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error getData: " + url, e);
			} catch (Exception e) {
				Log.e(LOG_TAG, "stream is NULL");
			}
			data = builder.toString();
			if (!found && exists) {
				try {
					FileInputStream fis = context.openFileInput(cleanUrl);
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					StringBuffer buffer = new StringBuffer();
					for (String line; (line = br.readLine()) != null;) {
						buffer.append(line);
					}
					fis.close();
					data = buffer.toString();
				} catch (Exception e) {
					Log.e(LOG_TAG, "Error getData: " + url, e);
				}
			}
			if (data != null && data.trim().length() > 0) {
				try {
					FileOutputStream fos = context.openFileOutput(cleanUrl, Context.MODE_PRIVATE);
					fos.write(data.getBytes());
					fos.close();
				} catch (FileNotFoundException e) {
					Log.e(LOG_TAG, "Error getData: " + url, e);
				} catch (IOException e) {
					Log.e(LOG_TAG, "Error getData: " + url, e);
				}
			}
		}
		return data;
	}

	public static String clean(String value) {
		return value.replaceAll(":", "_").replaceAll("/", "_").replaceAll("\\\\", "_").replaceAll("\\?", "_").replaceAll("#", "_");
	}

	public static final Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			int size = Math.max(myBitmap.getWidth(), myBitmap.getHeight());
			Bitmap b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			c.drawBitmap(myBitmap, (size - myBitmap.getWidth()) / 2, (size - myBitmap.getHeight()) / 2, paint);
			return b;
		} catch (Exception e) {
			Log.e(LOG_TAG, "Faild to get the image from URL:" + src, e);
			return null;
		}
	}

	/**
	 * Determine if there is a high resolution icon available for the web site.
	 * 
	 * @param context
	 * @param url
	 * @return
	 */
	public static final String getWebSiteIcon(Context context, String url) {
		Log.d(LOG_TAG, "getWebSiteIcon=" + url);
		String icon = null;
		if (url != null) {
			String data = Utils.getCachedData(context, url, true);
			if (data != null) {
				Document doc = Jsoup.parse(data);
				if (doc != null) {
					String href = null;
					Elements metas = doc.select("meta[itemprop=image]");
					if (metas.size() > 0) {
						Element meta = metas.first();
						href = meta.attr("abs:content");
						// weird jsoup bug: abs doesn't always work
						if (href == null || href.trim().length() == 0) {
							href = url + meta.attr("content");
						}
					}
					if (href == null || href.trim().length() == 0) {
						// Find the Microsoft tile icon
						metas = doc.select("meta[name=msapplication-TileImage]");
						if (metas.size() > 0) {
							Element meta = metas.first();
							href = meta.attr("abs:content");
							// weird jsoup bug: abs doesn't always work
							if (href == null || href.trim().length() == 0) {
								href = url + meta.attr("content");
							}
						}
					}
					if (href == null || href.trim().length() == 0) {
						// Find the Apple touch icon
						Elements links = doc.select("link[rel=apple-touch-icon]");
						if (links.size() > 0) {
							Element link = links.first();
							href = link.attr("abs:href");
							// weird jsoup bug: abs doesn't always work
							if (href == null || href.trim().length() == 0) {
								href = url + link.attr("href");
							}
						}
					}
					if (href == null || href.trim().length() == 0) {
						// Find the Facebook open graph icon
						metas = doc.select("meta[property=og:image]");
						if (metas.size() > 0) {
							Element link = metas.first();
							href = link.attr("abs:content");
							// weird jsoup bug: abs doesn't always work
							if (href == null || href.trim().length() == 0) {
								href = url + link.attr("content");
							}
						}
					}
					if (href == null || href.trim().length() == 0) {
						metas = doc.select("link[rel=image_src]");
						if (metas.size() > 0) {
							Element link = metas.first();
							href = link.attr("abs:href");
							// weird jsoup bug: abs doesn't always work
							if (href == null || href.trim().length() == 0) {
								href = url + link.attr("href");
							}
						}
					}
					if (href != null && href.trim().length() > 0) {
						try {
							Bitmap bitmap = Utils.getBitmapFromURL(href);
							if (bitmap != null) {
								icon = WEB_SITE_ICON_PREFIX + Utils.clean(href) + ".png";
								Utils.saveToFile(context, bitmap, bitmap.getWidth(), bitmap.getHeight(), icon);
								bitmap.recycle();
							}
						} catch (Exception e) {
							Log.d(LOG_TAG, "getWebSiteIcon", e);
						}
					}
				}
			}
		}
		return icon;
	}

	public static final void saveToFile(Context context, Bitmap bitmap, int targetWidth, int targetHeight, String fileName) throws IOException {
		FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
		// FileOutputStream fos = new FileOutputStream(fileName);
		if (bitmap.getWidth() == targetWidth && bitmap.getHeight() == targetHeight) {
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
		} else {
			Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
			scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
		}
		fos.close();
	}

	public static final void logDeviceInfo(Context context) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			Log.i(LOG_TAG, "Version=" + pi.versionName);
			Log.i(LOG_TAG, "IP Address=" + Utils.getLocalIpAddress());
			Log.i(LOG_TAG, "android.os.Build.VERSION.RELEASE=" + android.os.Build.VERSION.RELEASE);
			Log.i(LOG_TAG, "android.os.Build.VERSION.INCREMENTAL=" + android.os.Build.VERSION.INCREMENTAL);
			Log.i(LOG_TAG, "android.os.Build.DEVICE=" + android.os.Build.DEVICE);
			Log.i(LOG_TAG, "android.os.Build.MODEL=" + android.os.Build.MODEL);
			Log.i(LOG_TAG, "android.os.Build.PRODUCT=" + android.os.Build.PRODUCT);
			Log.i(LOG_TAG, "android.os.Build.MANUFACTURER=" + android.os.Build.MANUFACTURER);
			Log.i(LOG_TAG, "android.os.Build.BRAND=" + android.os.Build.BRAND);
		} catch (Exception e) {
			Log.e(LOG_TAG, "logDeviceInfo", e);
		}
	}

	public static final String getLocalIpAddress() {
		InetAddress inetAddress = Utils.getLocalInetAddress();
		if (inetAddress != null) {
			return inetAddress.getHostAddress().toString();
		}
		return null;
	}

	public static final InetAddress getLocalInetAddress() {
		InetAddress selectedInetAddress = null;
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				if (intf.isUp()) {
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							if (inetAddress instanceof Inet4Address) { // only
																		// want
																		// ipv4
																		// address
								if (inetAddress.getHostAddress().toString().charAt(0) != '0') {
									if (selectedInetAddress == null) {
										selectedInetAddress = inetAddress;
									} else if (intf.getName().startsWith("eth")) { // prefer
																					// wired
																					// interface
										selectedInetAddress = inetAddress;
									}
								}
							}
						}
					}
				}
			}
			return selectedInetAddress;
		} catch (Throwable e) {
			Log.e(LOG_TAG, "Failed to get the IP address", e);
		}
		return null;
	}

	public static final String getVersion(Context context) {
		String versionString = context.getString(R.string.unknown_build);
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionString = info.versionName;
		} catch (Exception e) {
			// do nothing
		}
		return versionString;
	}

	public static boolean isValidUrl(String url) {
		String urlPattern = "^http(s{0,1})://[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
		return url.matches(urlPattern);
	}
}
