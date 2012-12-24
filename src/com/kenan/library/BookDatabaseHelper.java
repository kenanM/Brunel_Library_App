package com.kenan.library;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class creates and updates the books database and is heavily inspired by:
 * http://www.vogella.com/articles/AndroidSQLite/article.html
 */
public class BookDatabaseHelper extends SQLiteOpenHelper {

	public static final String BOOK_TABLE_NAME = "books";

	public static final String ID_COLUMN_NAME = "_id";
	public static final String DUE_DATE_COLUMN_NAME = "duedate";
	public static final String RENEWALS_COLUMN_NAME = "renewals";
	public static final String TITLE_COLUMN_NAME = "title";

	public static final String DATABASE_NAME = "database.db";
	public static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "create table "
			+ BOOK_TABLE_NAME + "(" + ID_COLUMN_NAME
			+ " integer primary key autoincrement, " + TITLE_COLUMN_NAME
			+ " text not null, " + RENEWALS_COLUMN_NAME + " integer not null, "
			+ DUE_DATE_COLUMN_NAME + " text not null);";

	public BookDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// As there hasn't yet been any updates to the database, this is
		// redundant
	}

}
