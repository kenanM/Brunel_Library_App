package com.kenan.library;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class LocalStorage {

	public static class OpeningTimes {
		public static final String DAY_OF_UPDATE = "closing_times_update_day";
		public static final String VALUE = "closing_times_value";
	}

	private SharedPreferences preferences;
	private Editor editor;

	public LocalStorage(Context context) {
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
}
