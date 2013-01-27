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

import com.entertailion.android.overlaynews.rss.RssItem;

/**
 * Track the items for each feed.
 * 
 * @author leon_nicholls
 * 
 */
public class ItemsTable {
	private static String LOG_TAG = "ItemsTable";

	public static synchronized long insertItem(Context context, int feed, String title, String link, String description, String content, long date) throws Exception {
		Log.d(LOG_TAG, "insertItem");

		DatabaseHelper databaseHelper = new DatabaseHelper(context);
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		db.beginTransaction();
		long id = DatabaseHelper.NO_ID;
		try {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.FEED_COLUMN, feed);
			values.put(DatabaseHelper.TITLE_COLUMN, title);
			values.put(DatabaseHelper.LINK_COLUMN, link);
			values.put(DatabaseHelper.DESCRIPTION_COLUMN, description);
			values.put(DatabaseHelper.CONTENT_COLUMN, content);
			values.put(DatabaseHelper.DATE_COLUMN, date);
			id = db.insertOrThrow(DatabaseHelper.ITEMS_TABLE, DatabaseHelper.TITLE_COLUMN, values);
			db.setTransactionSuccessful();
			Log.d(LOG_TAG, "insertItem: success");
		} catch (Exception e) {
			Log.e(LOG_TAG, "insertItem: failed", e);
			throw new Exception(e);
		} finally {
			db.endTransaction();
			db.close();
		}
		return id;
	}

	public static synchronized ArrayList<RssItem> getItems(Context context, int feed) {
		Log.d(LOG_TAG, "getItems: " + feed);
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = null;
		ArrayList<RssItem> items = null;
		try {
			cursor = db.rawQuery("SELECT " + DatabaseHelper.ID_COLUMN + ", " + DatabaseHelper.TITLE_COLUMN + ", " + DatabaseHelper.LINK_COLUMN + ", "
					+ DatabaseHelper.DESCRIPTION_COLUMN + ", " + DatabaseHelper.CONTENT_COLUMN + ", " + DatabaseHelper.DATE_COLUMN + " FROM "
					+ DatabaseHelper.ITEMS_TABLE + " WHERE " + DatabaseHelper.FEED_COLUMN + "=" + feed + " ORDER BY " + DatabaseHelper.ID_COLUMN, null);
			if (cursor.moveToFirst()) {
				items = new ArrayList<RssItem>();
				do {
					RssItem itemInfo = new RssItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4),
							new Date(cursor.getLong(5)));
					items.add(itemInfo);
				} while ((cursor.moveToNext()));
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "getItems failed", e);
		} finally {
			if (null != cursor)
				cursor.close();
			db.close();
		}
		return items;
	}

	public static synchronized void updateItem(Context context, int id, int feed, String title, String link, String description, String content, long date) throws Exception {
		Log.d(LOG_TAG, "updateItem");

		DatabaseHelper databaseHelper = new DatabaseHelper(context);
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.FEED_COLUMN, feed);
			values.put(DatabaseHelper.TITLE_COLUMN, title);
			values.put(DatabaseHelper.LINK_COLUMN, link);
			values.put(DatabaseHelper.DESCRIPTION_COLUMN, description);
			values.put(DatabaseHelper.CONTENT_COLUMN, content);
			values.put(DatabaseHelper.DATE_COLUMN, date);
			db.update(DatabaseHelper.ITEMS_TABLE, values, DatabaseHelper.ID_COLUMN + "=?", new String[] { String.valueOf(id) });
			db.setTransactionSuccessful();
			Log.d(LOG_TAG, "updateItem: success");
		} catch (Exception e) {
			Log.e(LOG_TAG, "updateItem: failed", e);
			throw new Exception(e);
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	public static synchronized void deleteItem(Context context, int id) throws Exception {
		Log.d(LOG_TAG, "deleteItem");

		DatabaseHelper databaseHelper = new DatabaseHelper(context);
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			db.delete(DatabaseHelper.ITEMS_TABLE, DatabaseHelper.ID_COLUMN + "=?", new String[] { String.valueOf(id) });
			db.setTransactionSuccessful();
			Log.d(LOG_TAG, "deleteItem: success");
		} catch (Exception e) {
			Log.e(LOG_TAG, "deleteItem: failed", e);
			throw new Exception(e);
		} finally {
			db.endTransaction();
			db.close();
		}
	}
	
	public static synchronized void deleteItems(Context context, int feed) throws Exception {
		Log.d(LOG_TAG, "deleteItems");

		DatabaseHelper databaseHelper = new DatabaseHelper(context);
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			db.delete(DatabaseHelper.ITEMS_TABLE, DatabaseHelper.FEED_COLUMN + "=?", new String[] { String.valueOf(feed) });
			db.setTransactionSuccessful();
			Log.d(LOG_TAG, "deleteItems: success");
		} catch (Exception e) {
			Log.e(LOG_TAG, "deleteItems: failed", e);
			throw new Exception(e);
		} finally {
			db.endTransaction();
			db.close();
		}
	}

}
