package com.kenan.library;

import java.text.DateFormat;
import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class LocalStorage {

	private static class OpeningTimes {
		public static final String DAY_OF_UPDATE = "closing_times_update_day";
		public static final String VALUE = "closing_times_value";
	}

	private static class BookDetails {
		public static final String DATE_OF_REFRESH = "date_of_refresh";
	}

	private static class Login {
		public static final String USERNAME = "username";
		public static final String PASSWORD = "password";
	}

	private static Editor getEditor(Context context) {
		return getPreferences(context).edit();
	}

	private static SharedPreferences getPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);

	}

	public static void updateOpeningTimes(Context context, String openingTimes) {
		int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		Editor editor = getEditor(context);
		editor.putInt(OpeningTimes.DAY_OF_UPDATE, today);
		editor.putString(OpeningTimes.VALUE, openingTimes);
		editor.commit();
	}

	public static String getOpeningTimes(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return preferences.getString(OpeningTimes.VALUE, "");
	}

	public static int getDayOfOpeningTimesUpdate(Context context) {
		return getPreferences(context).getInt(OpeningTimes.DAY_OF_UPDATE, -1);
	}

	public static void updateLastRefreshDate(Context context) {
		Calendar today = Calendar.getInstance();
		DateFormat formatter = DateFormat.getDateTimeInstance();
		String time = formatter.format(today.getTime());
		Editor editor = getEditor(context);
		editor.putString(BookDetails.DATE_OF_REFRESH, time);
		editor.commit();
	}

	public static String getRefreshDate(Context context) {
		return getPreferences(context).getString(BookDetails.DATE_OF_REFRESH,
				context.getString(R.string.press_refresh_to_see_your_books));
	}

	public static void setUserNameAndPassword(Context context, String username,
			String password) {
		Editor editor = getEditor(context);
		editor.putString(Login.USERNAME, username);
		editor.putString(Login.PASSWORD, password);
		editor.commit();
	}

	public static String getUserName(Context context) {
		return getPreferences(context).getString(Login.USERNAME, "");
	}

	public static String getPassword(Context context) {
		return getPreferences(context).getString(Login.PASSWORD, "");
	}
}
