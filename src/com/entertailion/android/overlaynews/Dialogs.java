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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.entertailion.android.overlaynews.database.FeedsTable;
import com.entertailion.android.overlaynews.rss.RssFeed;
import com.entertailion.android.overlaynews.rss.RssFeedAdapter;
import com.entertailion.android.overlaynews.utils.Analytics;
import com.entertailion.android.overlaynews.utils.Utils;

/**
 * Utility class to display various dialogs for the main activity
 * 
 * @author leon_nicholls
 * 
 */
public class Dialogs {

	private static final String LOG_TAG = "Dialogs";

	// Ratings dialog configuration
	public static final String DATE_FIRST_LAUNCHED = "date_first_launched";
	public static final String DONT_SHOW_RATING_AGAIN = "dont_show_rating_again";
	private final static int DAYS_UNTIL_PROMPT = 5;

	/**
	 * Display about dialog to user when invoked from menu option.
	 * 
	 * @param context
	 */
	public static void displayAbout(final ConfigActivity context) {
		final Dialog dialog = new Dialog(context);
		// order is important due to dialog bug
		dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		dialog.setContentView(R.layout.about);
		dialog.setTitle(context.getString(R.string.about_version_title, Utils.getVersion(context)));
		dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.logo);

		Typeface lightTypeface = ((OverlayApplication) context.getApplicationContext()).getLightTypeface(context);

		TextView copyrightTextView = (TextView) dialog.findViewById(R.id.copyright_text);
		copyrightTextView.setTypeface(lightTypeface);
		TextView feedbackTextView = (TextView) dialog.findViewById(R.id.feedback_text);
		feedbackTextView.setTypeface(lightTypeface);
		feedbackTextView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				context.showCover(false);
				dialog.dismiss();
				Intent intent = new Intent(context, EasterEggActivity.class);
				context.startActivity(intent);
				Analytics.logEvent(Analytics.EASTER_EGG);
				return true;
			}

		});
		TextView termsTextView = (TextView) dialog.findViewById(R.id.terms_text);
		termsTextView.setTypeface(lightTypeface);

		((Button) dialog.findViewById(R.id.button_web)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(context.getString(R.string.about_button_web_url)));
				context.startActivity(intent);
				Analytics.logEvent(Analytics.ABOUT_WEB_SITE);
				context.showCover(false);
				dialog.dismiss();
			}

		});

		((Button) dialog.findViewById(R.id.button_privacy_policy)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(context.getString(R.string.about_button_privacy_policy_url)));
				context.startActivity(intent);
				Analytics.logEvent(Analytics.ABOUT_PRIVACY_POLICY);
				context.showCover(false);
				dialog.dismiss();
			}

		});
		((Button) dialog.findViewById(R.id.button_more_apps)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(context.getString(R.string.about_button_more_apps_url)));
				context.startActivity(intent);
				Analytics.logEvent(Analytics.ABOUT_MORE_APPS);
				context.showCover(false);
				dialog.dismiss();
			}

		});
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				context.showCover(false);
			}

		});
		context.showCover(true);
		dialog.show();
		Analytics.logEvent(Analytics.DIALOG_ABOUT);
	}

	/**
	 * Prompt the user to rate the app.
	 * 
	 * @param context
	 */
	public static void displayRating(final ConfigActivity context) {
		SharedPreferences prefs = context.getSharedPreferences(ConfigActivity.PREFS_NAME, Activity.MODE_PRIVATE);

		if (prefs.getBoolean(DONT_SHOW_RATING_AGAIN, false)) {
			return;
		}

		final SharedPreferences.Editor editor = prefs.edit();

		// Get date of first launch
		Long date_firstLaunch = prefs.getLong(DATE_FIRST_LAUNCHED, 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong(DATE_FIRST_LAUNCHED, date_firstLaunch);
		}

		// Wait at least n days before opening
		if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
			final Dialog dialog = new Dialog(context);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.confirmation);

			TextView confirmationTextView = (TextView) dialog.findViewById(R.id.confirmationText);
			confirmationTextView.setText(context.getString(R.string.rating_message));
			Button buttonYes = (Button) dialog.findViewById(R.id.button1);
			buttonYes.setText(context.getString(R.string.dialog_yes));
			buttonYes.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.entertailion.android.overlaynews"));
					context.startActivity(intent);
					if (editor != null) {
						editor.putBoolean(DONT_SHOW_RATING_AGAIN, true);
						editor.commit();
					}
					Analytics.logEvent(Analytics.RATING_YES);
					context.showCover(false);
					dialog.dismiss();
				}

			});
			Button buttonNo = (Button) dialog.findViewById(R.id.button2);
			buttonNo.setText(context.getString(R.string.dialog_no));
			buttonNo.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (editor != null) {
						editor.putBoolean(DONT_SHOW_RATING_AGAIN, true);
						editor.commit();
					}
					Analytics.logEvent(Analytics.RATING_NO);
					context.showCover(false);
					dialog.dismiss();
				}

			});
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					context.showCover(false);
				}

			});
			context.showCover(true);
			dialog.show();
		}

		editor.commit();
	}

	/**
	 * Display the list of feed sites.
	 * 
	 * @param context
	 */
	public static void displaySites(final ConfigActivity context) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.sites_list);

		ListView listView = (ListView) dialog.findViewById(R.id.list);
		ArrayList<RssFeed> feeds = FeedsTable.getFeeds(context);
		Collections.sort(feeds, new Comparator<RssFeed>() {

			@Override
			public int compare(RssFeed lhs, RssFeed rhs) {
				return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
			}

		});
		final RssFeedAdapter feedAdapter = new RssFeedAdapter(context, feeds);
		listView.setAdapter(feedAdapter);
		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				context.showCover(false);
				dialog.dismiss();
			}

		});
		listView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				RssFeed feed = (RssFeed) parent.getAdapter().getItem(position);
				context.showCover(false);
				dialog.dismiss();
				displayDeleteFeed(context, feed);
				return false;
			}

		});
		listView.setDrawingCacheEnabled(true);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				context.showCover(false);
			}

		});
		context.showCover(true);
		dialog.show();
		Analytics.logEvent(Analytics.DIALOG_FEEDS);

	}

	/**
	 * Display a dialog to confirm that a user wants to delete a feed.
	 * 
	 * @param context
	 */
	public static void displayDeleteFeed(final ConfigActivity context, final RssFeed feed) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.confirmation);

		TextView confirmationTextView = (TextView) dialog.findViewById(R.id.confirmationText);
		confirmationTextView.setText(context.getString(R.string.dialog_delete_feed_message));
		Button buttonYes = (Button) dialog.findViewById(R.id.button1);
		buttonYes.setText(context.getString(R.string.dialog_yes));
		buttonYes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					FeedsTable.deleteFeed(context, feed.getId());
				} catch (Exception e) {
					Log.e(LOG_TAG, "displayDeleteFeed", e);
				}
				Analytics.logEvent(Analytics.DELETE_FEED);
				context.showCover(false);
				dialog.dismiss();
			}

		});
		Button buttonNo = (Button) dialog.findViewById(R.id.button2);
		buttonNo.setText(context.getString(R.string.dialog_no));
		buttonNo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				context.showCover(false);
				dialog.dismiss();
			}

		});
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				context.showCover(false);
			}

		});
		context.showCover(true);
		dialog.show();
		Analytics.logEvent(Analytics.DIALOG_DELETE_FEED);
	}

	/**
	 * Add a new feed.
	 * 
	 * @param context
	 */
	public static void displayAddFeed(final ConfigActivity context) {
		// Set an EditText view to get user input
		final EditText input = new EditText(context);
		new AlertDialog.Builder(context).setTitle(context.getString(R.string.dialog_add_feed_title)).setMessage(context.getString(R.string.dialog_add_feed))
				.setView(input).setPositiveButton(context.getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Editable value = input.getText();
						String url = value.toString();
						if (!(url.startsWith("http://") || url.startsWith("https://"))) {
							url = "http://" + url;
						}
						if (!Utils.isValidUrl(url)) {
							AlertDialog alertDialog = new AlertDialog.Builder(context).create();
							alertDialog.setTitle(context.getString(R.string.dialog_add_feed_invalid_url_title));
							alertDialog.setMessage(context.getString(R.string.dialog_add_feed_invalid_url));
							alertDialog.setButton(context.getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
								}
							});
							alertDialog.show();
							return;
						}
						context.addFeed(url);
					}
				}).setNegativeButton(context.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();
	}
}
