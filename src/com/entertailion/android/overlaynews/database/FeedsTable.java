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
package com.entertailion.android.overlaynews.database;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.entertailion.android.overlaynews.rss.RssFeed;

/**
 * Track the feeds.
 * 
 * @author leon_nicholls
 * 
 */
public class FeedsTable {
	private static String LOG_TAG = "FeedsTable";

	public static synchronized long insertFeed(Context context, String title, String link,
			String description, long date, long viewDate, String logo, String image, int ttl)
			throws Exception {
		Log.d(LOG_TAG, "insertFeed: " + title);

		long id = DatabaseHelper.NO_ID;
		DatabaseHelper databaseHelper = new DatabaseHelper(context);
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.TITLE_COLUMN, title);
			values.put(DatabaseHelper.LINK_COLUMN, link);
			values.put(DatabaseHelper.DESCRIPTION_COLUMN, description);
			values.put(DatabaseHelper.DATE_COLUMN, date);
			values.put(DatabaseHelper.VIEW_DATE_COLUMN, viewDate);
			values.put(DatabaseHelper.LOGO_COLUMN, logo);
			values.put(DatabaseHelper.IMAGE_COLUMN, image);
			values.put(DatabaseHelper.TYPE_COLUMN, DatabaseHelper.RSS_TYPE);
			values.put(DatabaseHelper.TTL_COLUMN, ttl);
			id = db.insertOrThrow(DatabaseHelper.FEEDS_TABLE,
					DatabaseHelper.TITLE_COLUMN, values);
			db.setTransactionSuccessful();
			Log.d(LOG_TAG, "insertFeed: success: "+id);
		} catch (Exception e) {
			Log.e(LOG_TAG, "insertFeed: failed", e);
			throw new Exception(e);
		} finally {
			db.endTransaction();
			db.close();
		}
		return id;
	}

	public static synchronized ArrayList<RssFeed> getFeeds(Context context) {
		Log.d(LOG_TAG, "getFeeds");
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = null;
		ArrayList<RssFeed> feeds = null;
		try {
			cursor = db.rawQuery("SELECT " + DatabaseHelper.ID_COLUMN + ", "
					+ DatabaseHelper.TITLE_COLUMN + ", "
					+ DatabaseHelper.LINK_COLUMN + ", "
					+ DatabaseHelper.DESCRIPTION_COLUMN + ", "
					+ DatabaseHelper.DATE_COLUMN + ", "
					+ DatabaseHelper.VIEW_DATE_COLUMN + ", "
					+ DatabaseHelper.LOGO_COLUMN + ", "
					+ DatabaseHelper.IMAGE_COLUMN + ", "
					+ DatabaseHelper.TYPE_COLUMN + ", "
					+ DatabaseHelper.TTL_COLUMN + " FROM "
					+ DatabaseHelper.FEEDS_TABLE + " ORDER BY "
					+ DatabaseHelper.ID_COLUMN, null);
			if (cursor.moveToFirst()) {
				feeds = new ArrayList<RssFeed>();
				do {
					RssFeed row = new RssFeed(cursor.getInt(0),
							cursor.getString(1), cursor.getString(2),
							cursor.getString(3), new Date(cursor.getLong(4)), new Date(cursor.getLong(5)),
							cursor.getString(6), cursor.getString(7), null,
							cursor.getInt(9));
					feeds.add(row);
				} while ((cursor.moveToNext()));
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "getFeeds failed", e);
		} finally {
			if (null != cursor)
				cursor.close();
			db.close();
		}
		return feeds;
	}

	public static synchronized void updateFeed(Context context, int id, String title,
			String link, String description, long date, long viewDate, String logo, String image, int ttl)
			throws Exception {
		Log.d(LOG_TAG, "updateFeed: "+id);

		DatabaseHelper databaseHelper = new DatabaseHelper(context);
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.TITLE_COLUMN, title);
			values.put(DatabaseHelper.LINK_COLUMN, link);
			values.put(DatabaseHelper.DESCRIPTION_COLUMN, description);
			values.put(DatabaseHelper.DATE_COLUMN, date);
			values.put(DatabaseHelper.VIEW_DATE_COLUMN, viewDate);
			values.put(DatabaseHelper.LOGO_COLUMN, logo);
			values.put(DatabaseHelper.IMAGE_COLUMN, image);
			values.put(DatabaseHelper.TYPE_COLUMN, DatabaseHelper.RSS_TYPE);
			values.put(DatabaseHelper.TTL_COLUMN, ttl);
			int num = db.update(DatabaseHelper.FEEDS_TABLE, values,
					DatabaseHelper.ID_COLUMN + "=?",
					new String[] { String.valueOf(id) });
			db.setTransactionSuccessful();
			Log.d(LOG_TAG, "updateFeed: success: "+num);
		} catch (Exception e) {
			Log.e(LOG_TAG, "updateFeed: failed", e);
			throw new Exception(e);
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	public static synchronized void deleteFeed(Context context, int id) throws Exception {
		Log.d(LOG_TAG, "deleteFeed");

		DatabaseHelper databaseHelper = new DatabaseHelper(context);
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			db.delete(DatabaseHelper.FEEDS_TABLE, DatabaseHelper.ID_COLUMN
					+ "=?", new String[] { String.valueOf(id) });
			db.setTransactionSuccessful();
			Log.d(LOG_TAG, "deleteFeed: success");
		} catch (Exception e) {
			Log.e(LOG_TAG, "deleteFeed: failed", e);
			throw new Exception(e);
		} finally {
			db.endTransaction();
			db.close();
		}
	}

}
