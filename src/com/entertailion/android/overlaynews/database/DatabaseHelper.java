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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.entertailion.android.overlaynews.R;

/**
 * Database management utility.
 * 
 * 
 * @author leon_nicholls
 * 
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	private static String LOG_TAG = "DatabaseHelper";

	public static final int NO_ID = -1;
	public static final int RSS_TYPE = 0;

	private Context context;

	public final static int CURRENT_DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "overlaynews.db";

	// Feeds table
	public final static String FEEDS_TABLE = "feeds";
	public final static String ID_COLUMN = "_id";
	public final static String TITLE_COLUMN = "title";
	public final static String LINK_COLUMN = "link";
	public final static String DESCRIPTION_COLUMN = "description";
	public final static String DATE_COLUMN = "datum";
	public final static String VIEW_DATE_COLUMN = "viewDatum";
	public final static String LOGO_COLUMN = "logo";
	public final static String IMAGE_COLUMN = "logoImage";
	public final static String TTL_COLUMN = "ttl";
	// maybe support other types in the future?
	public final static String TYPE_COLUMN = "type";

	// Items table
	public final static String ITEMS_TABLE = "items";
	public final static String FEED_COLUMN = "feed";
	public final static String CONTENT_COLUMN = "content";
	
	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		this.context = context;
	}

	/**
	 * @param context
	 */
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, CURRENT_DATABASE_VERSION);
		this.context = context;
	}

	/**
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(LOG_TAG, "create database");
		// create tables
		createFeedsTable(db);
		populateFeedsTable(db);
		createItemsTable(db);
	}

	/**
	 * Delete database
	 */
	private void deleteDatabase() {
		context.deleteDatabase(DATABASE_NAME);
	}

	/**
	 * Create feeds table
	 * 
	 * @param db
	 */
	private void createFeedsTable(SQLiteDatabase db) {
		String TABLE_CREATE = "CREATE TABLE " + FEEDS_TABLE + " (" + ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TITLE_COLUMN + " STRING, "
				+ LINK_COLUMN + " STRING, " + DESCRIPTION_COLUMN + " STRING, " + DATE_COLUMN + ", " + VIEW_DATE_COLUMN + " INTEGER, " + LOGO_COLUMN + " STRING, " + IMAGE_COLUMN + " STRING, " + TYPE_COLUMN + " INTEGER, " + TTL_COLUMN + " INTEGER"
				+ ");";

		db.execSQL(TABLE_CREATE);
		Log.i(LOG_TAG, FEEDS_TABLE + " table was created successfully");
	}

	/**
	 * Initialize the feeds table with default feeds
	 */
	private void populateFeedsTable(SQLiteDatabase db) {
		addFeed(db, context.getString(R.string.feed1_title), context.getString(R.string.feed1_url));
		addFeed(db, context.getString(R.string.feed2_title), context.getString(R.string.feed2_url));
		addFeed(db, context.getString(R.string.feed3_title), context.getString(R.string.feed3_url));
		addFeed(db, context.getString(R.string.feed4_title), context.getString(R.string.feed4_url));
	}

	private void addFeed(SQLiteDatabase db, String title, String link) {
		try {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.TITLE_COLUMN, title);
			values.put(DatabaseHelper.LINK_COLUMN, link);
			values.put(DatabaseHelper.DESCRIPTION_COLUMN, "");
			values.put(DatabaseHelper.DATE_COLUMN, 0);
			values.put(DatabaseHelper.VIEW_DATE_COLUMN, 0);
			values.put(DatabaseHelper.LOGO_COLUMN, "");
			values.put(DatabaseHelper.IMAGE_COLUMN, "");
			values.put(DatabaseHelper.TYPE_COLUMN, DatabaseHelper.RSS_TYPE);
			values.put(DatabaseHelper.TTL_COLUMN, 0);
			db.insertOrThrow(DatabaseHelper.FEEDS_TABLE, DatabaseHelper.TITLE_COLUMN, values);
		} catch (Exception e) {
			Log.e(LOG_TAG, "addFeed", e);
		}
	}

	/**
	 * Create items table
	 * 
	 * @param db
	 */
	private void createItemsTable(SQLiteDatabase db) {
		String TABLE_CREATE = "CREATE TABLE " + ITEMS_TABLE + " (" + ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FEED_COLUMN + " INTEGER, " + TITLE_COLUMN + " STRING, "
				+ LINK_COLUMN + " STRING, " + DESCRIPTION_COLUMN + " STRING, " + CONTENT_COLUMN + " STRING, " + DATE_COLUMN + " INTEGER" + ");";

		db.execSQL(TABLE_CREATE);
		Log.i(LOG_TAG, ITEMS_TABLE + " table was created successfully");
	}
	
	/**
	 * Upgrade database
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase,
	 *      int, int)
	 */
	@Override
	public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == newVersion) {
			Log.i(LOG_TAG, "Database upgrade not needed");
			return;
		}
		Log.i(LOG_TAG, "Database needs to be upgraded from version " + oldVersion + " to version " + newVersion);
		boolean success = doUpgrade(db, oldVersion, newVersion);
		if (success) {
			Log.i(LOG_TAG, "Database was updated from version " + oldVersion + " to version " + newVersion);
		} else {
			Log.w(LOG_TAG, "Database was NOT updated from version " + oldVersion + " to version " + newVersion);
			rebuildTables(db);
		}
	}

	/**
	 * Rebuild tables
	 * 
	 * @param db
	 */
	private void rebuildTables(final SQLiteDatabase db) {
		// something very bad happened...
		deleteDatabase();
		System.exit(1);
	}

	/**
	 * Upgrade database tables
	 * 
	 * @param db
	 * @param oldVersion
	 * @param newVersion
	 * @return
	 */
	private boolean doUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(LOG_TAG, "doUpgrade: " + oldVersion + " to " + newVersion);
		boolean success = true;
		try {
			db.beginTransaction();
			int currentVersion = oldVersion;
			for (int i = currentVersion; i < newVersion; i++) {
				if (currentVersion == 1) {
					upgradeFrom1To2(db);
				}
				currentVersion++;
			}
			db.setVersion(newVersion);
			db.setTransactionSuccessful();
			Log.i(LOG_TAG, "Database upgrade was successful");
		} catch (Exception e) {
			Log.e(LOG_TAG, "Database upgrade was NOT successful", e);
			success = false;
		} finally {
			db.endTransaction();
		}
		return success;
	}

	/**
	 * Clear database tables
	 * 
	 * @param context
	 * @return
	 */
	public static boolean clearTables(Context context) {
		boolean success = true;
		Log.i(LOG_TAG, "Clearing all databases");
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.beginTransaction();

			Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
			if (cursor.moveToFirst()) {
				String tableName = cursor.getString(0);
				Log.d(LOG_TAG, "clearing " + tableName);
				db.delete(tableName, null, null);
			}
			if (null != cursor)
				cursor.close();
			Log.i(LOG_TAG, "Databased tables cleared");
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(LOG_TAG, "Database tables NOT cleared", e);
			success = false;
		} finally {
			db.endTransaction();
			db.close();
		}
		return success;
	}

	/**
	 * Upgrade database from version 1 to version 2
	 * 
	 * @param db
	 */
	private void upgradeFrom1To2(SQLiteDatabase db) {
		// TODO future versions...
	}

}
