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

	private Context context;
	private SharedPreferences preferences;
	private Editor editor;

	public LocalStorage(Context context) {
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		editor = preferences.edit();
	}

	public void updateOpeningTimes(String openingTimes) {
		int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		editor.putInt(OpeningTimes.DAY_OF_UPDATE, today);
		editor.putString(OpeningTimes.VALUE, openingTimes);
		editor.commit();
	}

	public String getOpeningTimes() {
		return preferences.getString(OpeningTimes.VALUE, "");
	}

	public int getDayOfOpeningTimesUpdate() {
		return preferences.getInt(OpeningTimes.DAY_OF_UPDATE, -1);
	}

	public void updateLastRefreshDate() {
		Calendar today = Calendar.getInstance();
		DateFormat formatter = DateFormat.getDateTimeInstance();
		String time = formatter.format(today.getTime());
		editor.putString(BookDetails.DATE_OF_REFRESH, time);
		editor.commit();
	}

	public String getLastRefreshDate() {
		return preferences.getString(BookDetails.DATE_OF_REFRESH,
				context.getString(R.string.press_refresh_to_see_your_books));
	}
}
