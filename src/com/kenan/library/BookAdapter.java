package com.kenan.library;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class BookAdapter extends CursorAdapter {

	Context context;

	public BookAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		this.context = context;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		Book book = BookDataSource.cursorToBook(cursor);

		TextView title = (TextView) view.findViewById(R.id.title);
		TextView date_due = (TextView) view.findViewById(R.id.date_due);
		TextView renewals = (TextView) view.findViewById(R.id.renewals);

		title.setText(book.getTitle());
		date_due.setText(context.getText(R.string.date_due) + book.getDateDue());
		renewals.setText(context.getText(R.string.renewals).toString()
				+ book.getRenewals());
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.row_layout, parent, false);
	}
}
