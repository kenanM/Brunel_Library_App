package com.kenan.library;

import static com.kenan.library.StudentDatabaseHelper.ALL_COLUMNS;
import static com.kenan.library.StudentDatabaseHelper.ID_COLUMN_NAME;
import static com.kenan.library.StudentDatabaseHelper.LAST_REFRESH_COLUMN_NAME;
import static com.kenan.library.StudentDatabaseHelper.LAST_UPDATE_OPENING_TIMES_COLUMN_NAME;
import static com.kenan.library.StudentDatabaseHelper.OPENING_TIMES_COLUMN_NAME;
import static com.kenan.library.StudentDatabaseHelper.PIN_COLUMN_NAME;
import static com.kenan.library.StudentDatabaseHelper.STUDENT_TABLE_NAME;
import static com.kenan.library.StudentDatabaseHelper.USER_COLUMN_NAME;

import java.text.DateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class StudentDataSource {

	private static final String TAG = StudentDataSource.class.toString();

	private SQLiteDatabase database;
	private StudentDatabaseHelper helper;

	public StudentDataSource(Context context) {
		helper = new StudentDatabaseHelper(context);
		open();
	}

	public void open() throws SQLException {
		database = helper.getWritableDatabase();
	}

	public void close() {
		helper.close();
	}

	public void setUsernameAndPIN(String username, String pin) {
		Log.v(TAG, "setting username and password");
		ContentValues values = new ContentValues();
		values.put(USER_COLUMN_NAME, username);
		values.put(PIN_COLUMN_NAME, pin);
		update(values);
	}

	public String getUsername() {
		return getColumnStringValue(USER_COLUMN_NAME);
	}

	public String getPIN() {
		return getColumnStringValue(PIN_COLUMN_NAME);
	}

	public void logOut() {
		Log.v(TAG, "deleting everything (apart from the id field)");
		ContentValues contentValues = new ContentValues();
		for (String columnName : ALL_COLUMNS) {
			if (columnName == ID_COLUMN_NAME)
				continue;
			contentValues.put(columnName, "");
		}
		update(contentValues);
	}

	public void updateOpeningTimes(String openingTimes) {
		int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		ContentValues contentValues = new ContentValues();
		contentValues.put(OPENING_TIMES_COLUMN_NAME, openingTimes);
		contentValues.put(LAST_UPDATE_OPENING_TIMES_COLUMN_NAME, today);
		update(contentValues);
	}

	public int getDayOfOpeningTimesUpdate() {
		Cursor cursor = getCursor();
		return cursor.getInt(cursor
				.getColumnIndex(LAST_UPDATE_OPENING_TIMES_COLUMN_NAME));
	}

	public String getOpeningTimes() {
		return getColumnStringValue(OPENING_TIMES_COLUMN_NAME);
	}

	public void updateLastRefreshDate() {
		Calendar today = Calendar.getInstance();
		DateFormat formatter = DateFormat.getDateTimeInstance();
		String time = formatter.format(today.getTime());
		ContentValues contentValues = new ContentValues();
		contentValues.put(LAST_REFRESH_COLUMN_NAME, time);
		update(contentValues);
	}

	public String getLastRefreshDate() {
		return getColumnStringValue(LAST_REFRESH_COLUMN_NAME);
	}

	/**
	 * Update the first row of the database
	 * 
	 * @param contentValues
	 *            should be the pairs of column names and their new values
	 */
	private void update(ContentValues contentValues) {
		database.update(STUDENT_TABLE_NAME, contentValues, ID_COLUMN_NAME
				+ "=?", new String[] { "1" });
	}

	/**
	 * Returns a cursor of a select all statement. It is important to note that
	 * we only use one row as this database is not being used conventionally.
	 */
	public Cursor getCursor() {
		String orderBy = ID_COLUMN_NAME + " ASC";
		String limit = " 1";
		Cursor cursor = database.query(STUDENT_TABLE_NAME, ALL_COLUMNS, null,
				null, null, null, orderBy, limit);
		cursor.moveToFirst();
		return cursor;
	}

	/** Extract a string from a column */
	private String getColumnStringValue(String columnName) {
		Cursor cursor = getCursor();
		return cursor.getString(cursor.getColumnIndex(columnName));
	}
}
