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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.entertailion.android.overlaynews.database.FeedsTable;
import com.entertailion.android.overlaynews.utils.Analytics;
import com.entertailion.android.overlaynews.utils.Utils;

/**
 * Main UI to let the user configure the app
 * 
 */

public class ConfigActivity extends Activity {
	public static final String LOG_TAG = "ConfigActivity";
	public static final String PREFS_NAME = "preferences";
	public static final String PREFERENCE_TIMING = "preference.timing";
	public static final int PREFERENCE_TIMING_DEFAULT = 60;
	public static final String PREFERENCE_DURATION = "preference.duration";
	public static final int PREFERENCE_DURATION_DEFAULT = 30;
	public static final String PREFERENCE_DELAY = "preference.delay";
	public static final int PREFERENCE_DELAY_DEFAULT = 10;
	public static final String PREFERENCE_ON_OFF = "preference.onoff";
	public static final int CONFIG_COUNT = 20;
	public static final String LAST_TIME_RUN = "last.time.run";
	private Spinner durationSpinner;
	private Spinner timingSpinner;
	private Spinner delaySpinner;
	private ToggleButton toggleButton;
	private int width, height;
	private Handler handler = new Handler();
	private boolean changed; // track configuration changes

	private ServiceConnection downloadServiceConnection;
	private DownloadService downloadService;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		connectToDownloadService();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.config);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		final SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

		timingSpinner = (Spinner) findViewById(R.id.spinnerTiming);
		List<String> timingList = new ArrayList<String>();
		timingList.add(getString(R.string.timing_1));
		timingList.add(getString(R.string.timing_2));
		timingList.add(getString(R.string.timing_3));
		ArrayAdapter<String> timingDataAdapter = new ArrayAdapter<String>(ConfigActivity.this, android.R.layout.simple_spinner_item, timingList);
		timingDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		timingSpinner.setAdapter(timingDataAdapter);
		timingSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				changed = true;
				int timing = 60;
				switch (pos) {
				case 0: // 30 mins
					timing = 30;
					break;
				case 1: // 1 hours
					timing = 60;
					break;
				case 2: // 2 hours
					timing = 120;
					break;
				default:
					break;
				}
				SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putInt(PREFERENCE_TIMING, timing);
				edit.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});
		int timing = preferences.getInt(PREFERENCE_TIMING, PREFERENCE_TIMING_DEFAULT);
		switch (timing) {
		case 30: // 30 mins
			timingSpinner.setSelection(0);
			break;
		case 60: // 1 hours
			timingSpinner.setSelection(1);
			break;
		case 120: // 2 hours
			timingSpinner.setSelection(2);
			break;
		default:
			break;
		}

		durationSpinner = (Spinner) findViewById(R.id.spinnerDuration);
		List<String> amountList = new ArrayList<String>();
		amountList.add(getString(R.string.duration_1));
		amountList.add(getString(R.string.duration_2));
		amountList.add(getString(R.string.duration_3));
		amountList.add(getString(R.string.duration_4));
		ArrayAdapter<String> amountDataAdapter = new ArrayAdapter<String>(ConfigActivity.this, android.R.layout.simple_spinner_item, amountList);
		amountDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		durationSpinner.setAdapter(amountDataAdapter);
		durationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				changed = true;
				int duration = 60;
				switch (pos) {
				case 0:
					duration = 15;
					break;
				case 1:
					duration = 30;
					break;
				case 2:
					duration = 45;
					break;
				case 3:
					duration = 60;
					break;
				default:
					break;
				}
				SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putInt(PREFERENCE_DURATION, duration);
				edit.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});
		int duration = preferences.getInt(PREFERENCE_DURATION, PREFERENCE_DURATION_DEFAULT);
		switch (duration) {
		case 15:
			durationSpinner.setSelection(0);
			break;
		case 30:
			durationSpinner.setSelection(1);
			break;
		case 45:
			durationSpinner.setSelection(2);
			break;
		case 60:
			durationSpinner.setSelection(2);
			break;
		default:
			break;
		}

		delaySpinner = (Spinner) findViewById(R.id.spinnerDelay);
		List<String> delayList = new ArrayList<String>();
		delayList.add(getString(R.string.delay_1));
		delayList.add(getString(R.string.delay_2));
		delayList.add(getString(R.string.delay_3));
		delayList.add(getString(R.string.delay_4));
		delayList.add(getString(R.string.delay_5));
		delayList.add(getString(R.string.delay_6));
		delayList.add(getString(R.string.delay_7));
		delayList.add(getString(R.string.delay_8));
		delayList.add(getString(R.string.delay_9));
		delayList.add(getString(R.string.delay_10));
		ArrayAdapter<String> delayDataAdapter = new ArrayAdapter<String>(ConfigActivity.this, android.R.layout.simple_spinner_item, delayList);
		amountDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		delaySpinner.setAdapter(delayDataAdapter);
		delaySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				changed = true;
				SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putInt(PREFERENCE_DELAY, pos + 1);
				edit.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});
		int delay = preferences.getInt(PREFERENCE_DELAY, PREFERENCE_DELAY_DEFAULT);
		delaySpinner.setSelection(delay - 1);

		((Button) findViewById(R.id.button_add)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Dialogs.displayAddFeed(ConfigActivity.this);
			}

		});
		((Button) findViewById(R.id.button_view)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Dialogs.displaySites(ConfigActivity.this);
			}

		});

		toggleButton = (ToggleButton) findViewById(R.id.onOffButton);
		toggleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean checked = toggleButton.isChecked();
				SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putBoolean(PREFERENCE_ON_OFF, checked);
				edit.commit();

				AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
				Intent alarmIntent = new Intent(ConfigActivity.this, AlarmReceiver.class);
				alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(ConfigActivity.this, 0, alarmIntent, 0);
				if (checked) { // ON
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

					int timing = preferences.getInt(PREFERENCE_TIMING, PREFERENCE_TIMING_DEFAULT);
					// set the default for first time run
					edit.putLong(ConfigActivity.LAST_TIME_RUN, System.currentTimeMillis() - (timing + 1) * 1000 * 60);
					edit.commit();
				} else {
					alarmManager.cancel(pendingIntent);
					pendingIntent.cancel();
				}
			}

		});
		boolean onOff = preferences.getBoolean(PREFERENCE_ON_OFF, false);
		toggleButton.setChecked(onOff);

		((ImageView) findViewById(R.id.about)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Dialogs.displayAbout(ConfigActivity.this);
			}

		});

		handleIntent(getIntent());

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
			Intent intent = new Intent(ConfigActivity.this, DownloadService.class);
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

	private void handleIntent(Intent intent) {
		try {
			Log.d(LOG_TAG, "handleIntent: " + intent);
			if (intent != null && intent.getAction() != null) {
				if (Intent.ACTION_SEND.equalsIgnoreCase(intent.getAction())) {
					// Chrome: Menu/Share page
					String text = intent.getStringExtra(Intent.EXTRA_TEXT);
					Log.d(LOG_TAG, "handleIntent: send text=" + text);
					if (text != null) {
						Uri uri = Uri.parse(text);
						if (uri != null) {
							if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
								addFeed(uri.toString());
								showSiteAddedMessage(uri.toString());
								Log.d(LOG_TAG, "url=" + uri);
							} else {
								Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_SHORT).show();
							}
						} else {
							text = "http://" + text;
							uri = Uri.parse(text);
							if (uri != null) {
								addFeed(uri.toString());
								showSiteAddedMessage(uri.toString());
								Log.d(LOG_TAG, "url=" + uri);
							} else {
								Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_SHORT).show();
							}
						}
					} else {
						// custom
						if (intent.getExtras() != null && intent.getExtras().containsKey(Intent.EXTRA_STREAM)) {
							Uri uri = (Uri) intent.getExtras().getParcelable(Intent.EXTRA_STREAM);
							Log.d(LOG_TAG, "handleIntent: uri=" + uri);
							if (uri != null) {
								addFeed(uri.toString());
								showSiteAddedMessage(uri.toString());
								Log.d(LOG_TAG, "url=" + uri);
							} else {
								Log.w(LOG_TAG, "Null URI to handle");
							}
						} else {
							Log.w(LOG_TAG, "No URI to handle");
						}
					}
				} else if (Intent.ACTION_VIEW.equalsIgnoreCase(intent.getAction())) {
					// Chrome: RSS link
					String data = intent.getDataString();
					addFeed(data);
					showSiteAddedMessage(data);
					Log.d(LOG_TAG, "handleIntent: view data=" + data);
				}
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "handleIntent", e);
		}
	}

	private void showSiteAddedMessage(String url) {
		Uri uri = Uri.parse(url);
		String message = this.getResources().getString(R.string.site_added, uri.getHost());
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	/**
	 * @see android.app.Activity#onPause()
	 */
	public void onPause() {
		if (changed && toggleButton.isChecked()) {
			// show the main activity if the user changed the config and goes to
			// live TV
			handler.post(new Runnable() {
				public void run() {
					AlarmReceiver.startMainActivity(ConfigActivity.this);
				}
			});
		}
		super.onPause();
	}

	/**
	 * @see android.app.Activity#onResume()
	 */
	public void onResume() {
		super.onResume();
		changed = false;

		Dialogs.displayRating(this);

		Analytics.logEvent(Analytics.OVERLAY_NEWS_CONFIG);
	}

	public void showCover(boolean state) {

	}

	public void refreshFeeds() {
		if (downloadService != null) {
			downloadService.refresh();
		}
	}

	public void addFeed(String url) {
		try {
			Uri uri = Uri.parse(url);
			FeedsTable.insertFeed(this, uri.getHost(), url, null, 0, 0, null, null, -1);
		} catch (Exception e) {
			Log.e(LOG_TAG, "displayAddFeed", e);
		}
		refreshFeeds();
	}
}
