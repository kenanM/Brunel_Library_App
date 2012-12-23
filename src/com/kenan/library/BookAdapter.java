package com.kenan.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookAdapter extends ArrayAdapter<Book> {

	Context context;
	Book[] values;

	public BookAdapter(Context context, Book[] values) {
		super(context, R.layout.row_layout, values);
		this.context = context;
		this.values = values;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Book book = values[position];
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View rowView = inflater.inflate(R.layout.row_layout, parent, false);
		TextView title = (TextView) rowView.findViewById(R.id.title);
		TextView date_due = (TextView) rowView.findViewById(R.id.date_due);
		TextView renewals = (TextView) rowView.findViewById(R.id.renewals);

		title.setText(book.getTitle());
		date_due.setText(context.getText(R.string.date_due) + book.getDateDue());
		renewals.setText(context.getText(R.string.renewals)
				+ book.getRenewals());

		return rowView;

	}

}
