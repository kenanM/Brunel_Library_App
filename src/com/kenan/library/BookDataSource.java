package com.kenan.library;

import static com.kenan.library.BookDatabaseHelper.BOOK_TABLE_NAME;
import static com.kenan.library.BookDatabaseHelper.DUE_DATE_COLUMN_NAME;
import static com.kenan.library.BookDatabaseHelper.ID_COLUMN_NAME;
import static com.kenan.library.BookDatabaseHelper.RENEWALS_COLUMN_NAME;
import static com.kenan.library.BookDatabaseHelper.TITLE_COLUMN_NAME;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

public class BookDataSource {

	private SQLiteDatabase database;
	private BookDatabaseHelper helper;

	private static final String[] allColumns = { ID_COLUMN_NAME,
			DUE_DATE_COLUMN_NAME, RENEWALS_COLUMN_NAME, TITLE_COLUMN_NAME };

	public BookDataSource(Context context) {
		helper = new BookDatabaseHelper(context);
		open();
	}

	public void open() throws SQLException {
		database = helper.getWritableDatabase();
	}
	
	public void close() {
		helper.close();
	}

	public void addBooks(List<Book> books) {
		for (Book book : books) {
			addBook(book);
		}
	}

	private void addBook(Book book) {
		Log.v(BookDataSource.class.toString(),
				"adding book: " + book.toString());
		ContentValues values = new ContentValues();
		values.put(TITLE_COLUMN_NAME, book.getTitle());
		values.put(RENEWALS_COLUMN_NAME, book.getRenewals());
		values.put(DUE_DATE_COLUMN_NAME, book.getDateDue());
		database.insert(BOOK_TABLE_NAME, null, values);
	}

	public void deleteBooks() {
		database.delete(BOOK_TABLE_NAME, null, null);
	}

	/** Returns a cursor of a select all statement */
	public Cursor getCursor() {
		return database.query(BOOK_TABLE_NAME, allColumns, null, null, null,
				null, null);
	}

	public List<Book> getAllBooks() {
		List<Book> books = new LinkedList<Book>();

		Cursor cursor = getCursor();

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Book book = cursorToBook(cursor);
			books.add(book);
			cursor.moveToNext();
		}

		cursor.close();
		return books;
	}

	public static Book cursorToBook(Cursor cursor) {
		String title = cursor.getString(cursor
				.getColumnIndex(TITLE_COLUMN_NAME));
		int renewals = cursor.getInt(cursor
				.getColumnIndex(RENEWALS_COLUMN_NAME));
		String dueDate = cursor.getString(cursor
				.getColumnIndex(DUE_DATE_COLUMN_NAME));
		return new Book(title, dueDate, renewals);
	}
}
