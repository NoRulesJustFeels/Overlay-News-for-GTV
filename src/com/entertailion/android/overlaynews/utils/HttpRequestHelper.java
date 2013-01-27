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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/**
 * Utility class to handle HTTP requests.
 */
public class HttpRequestHelper {
	DefaultHttpClient httpClient;
	HttpContext localContext;
	private String ret;
	private String TAG = "HttpRequestHelper";

	HttpResponse response = null;
	HttpPost httpPost = null;
	HttpGet httpGet = null;

	public HttpRequestHelper() {
		httpClient = createHttpClient();
		localContext = new BasicHttpContext();
	}

	public static DefaultHttpClient createHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 60000);
		HttpConnectionParams.setSoTimeout(params, 60000);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUserAgent(params,
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.5 Safari/537.22");

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

		return new DefaultHttpClient(conMgr, params);
	}

	public void clearCookies() {
		httpClient.getCookieStore().clear();
	}

	public void abort() {
		try {
			if (httpClient != null) {
				Log.i(TAG, "Abort.");
				httpPost.abort();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Log.e(TAG, "Failed to abort", e);
		}
	}

	public String sendPost(String url, Map<String, String> params) {
		return sendPost(url, params, null);
	}

	public String sendPost(String url, Map<String, String> params, String contentType) {
		ret = null;

		try {
			httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
			httpPost = new HttpPost(url);
			response = null;
			Log.d(TAG, "Setting httpPost headers");
			httpPost.setHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");

			if (contentType != null) {
				httpPost.setHeader("Content-Type", contentType);
			} else {
				httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			}

			httpPost.setEntity(buildData(params));
			response = httpClient.execute(httpPost, localContext);
			if (response != null) {
				ret = EntityUtils.toString(response.getEntity());
			}
		} catch (Throwable e) {
			Log.e(TAG, "Failed to post to url: " + url, e);
			e.printStackTrace();
		}

		Log.d(TAG, "Returning value:" + ret);
		return ret;
	}

	public String sendGet(String url) {
		httpGet = new HttpGet(url);

		try {
			response = httpClient.execute(httpGet);
		} catch (Throwable e) {
			Log.e(TAG, "sendGet: " + url, e);
		}

		// we assume that the response body contains the error message
		try {
			ret = EntityUtils.toString(response.getEntity());
		} catch (Throwable e) {
			Log.e(TAG, "sendGet: " + url, e);
		}

		return ret;
	}

	public String sendGet(String url, String... query) {
		String queryParams = buildgetData(query);
		return sendGet(url + "?" + queryParams);
	}

	public InputStream getHttpStream(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;

		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		if (!(conn instanceof HttpURLConnection))
			throw new IOException("Not an HTTP connection");

		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			response = httpConn.getResponseCode();

			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
			}
		} catch (Exception e) {
			throw new IOException("Error connecting");
		} // end try-catch

		return in;
	}

	public HttpEntity buildData(Map<String, String> map) throws UnsupportedEncodingException {
		if (null != map && !map.isEmpty()) {
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			for (String name : map.keySet()) {
				parameters.add(new BasicNameValuePair(name, map.get(name)));
			}
			return new UrlEncodedFormEntity(parameters, "UTF-8");
		}
		return null;
	}

	public static String buildgetData(String... list) {
		if (null == list || list.length == 0)
			return null;
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		for (int i = 0; i < list.length - 1; i = i + 2) {
			qparams.add(new BasicNameValuePair(list[i], list[i + 1]));
		}
		return URLEncodedUtils.format(qparams, "UTF-8");

	}
}
