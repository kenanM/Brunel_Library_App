package com.kenan.library;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class creates and updates the books database and is heavily inspired by:
 * http://www.vogella.com/articles/AndroidSQLite/article.html
 */
public class StudentDatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = StudentDatabaseHelper.class.toString();

	public static final String STUDENT_TABLE_NAME = "student_data";

	public static final String ID_COLUMN_NAME = "_id";
	public static final String LAST_REFRESH_COLUMN_NAME = "last_refresh_date";
	public static final String PIN_COLUMN_NAME = "pin";
	public static final String USER_COLUMN_NAME = "user";
	public static final String OPENING_TIMES_COLUMN_NAME = "closing_times";
	public static final String LAST_UPDATE_OPENING_TIMES_COLUMN_NAME = "last_update";

	public static final String[] ALL_COLUMNS = { ID_COLUMN_NAME,
			LAST_REFRESH_COLUMN_NAME, PIN_COLUMN_NAME, USER_COLUMN_NAME,
			OPENING_TIMES_COLUMN_NAME, LAST_UPDATE_OPENING_TIMES_COLUMN_NAME };

	public static final String DATABASE_NAME = "student_database";
	public static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "create table "
			+ STUDENT_TABLE_NAME + "(" + ID_COLUMN_NAME
			+ " integer primary key autoincrement, " + USER_COLUMN_NAME
			+ " text, " + PIN_COLUMN_NAME + " text, "
			+ LAST_REFRESH_COLUMN_NAME + " text, " + OPENING_TIMES_COLUMN_NAME
			+ " text, " + LAST_UPDATE_OPENING_TIMES_COLUMN_NAME + " integer);";

	public StudentDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		// Create the table
		database.execSQL(DATABASE_CREATE);
		// Insert an empty row
		insertEmptyRow(database);
	}

	private void insertEmptyRow(SQLiteDatabase database) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(ID_COLUMN_NAME, 1);
		contentValues.put(LAST_REFRESH_COLUMN_NAME, "");
		contentValues.put(PIN_COLUMN_NAME, "");
		contentValues.put(USER_COLUMN_NAME, "");
		contentValues.put(OPENING_TIMES_COLUMN_NAME, "");
		contentValues.put(LAST_UPDATE_OPENING_TIMES_COLUMN_NAME, -1);
		long temp = database.insert(STUDENT_TABLE_NAME, null, contentValues);
		Log.v(TAG, "database insert complete result(should be 1): " + temp);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// As there hasn't yet been any updates to the database, this is
		// redundant
	}
}
