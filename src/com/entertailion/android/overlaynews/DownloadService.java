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

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Launcher service to do background tasks
 * 
 * @author leon_nicholls
 * 
 */
public class DownloadService extends Service {

	private static final String LOG_TAG = "DownloadService";
	
	private Downloader downloader;

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		DownloadService getService() {
			return DownloadService.this;
		}
	}

	private final IBinder binder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences prefs = getSharedPreferences(ConfigActivity.PREFS_NAME, Context.MODE_PRIVATE);
		boolean checked = prefs.getBoolean(ConfigActivity.PREFERENCE_ON_OFF, false);
		if (checked) { // ON
			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			Intent alarmIntent = new Intent(this, AlarmReceiver.class);
			alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			if (calendar.get(Calendar.MINUTE) < 30) {
				calendar.set(Calendar.MINUTE, 30);
			} else {
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1);
			}

			// configure the alarm manager to invoke the main activity
			// every 30 mins
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 30, pendingIntent);
		}
		downloader = new Downloader(this);
	}
	
	public void refresh() {
		downloader.getFeeds();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		downloader.destroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LOG_TAG, "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

}