package com.kenan.library;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.toString();

	// TODO Remove
	public static final boolean DEBUG = true;

	TextView closingTimes;
	ListView list;
	BookDataSource bookDataSource;
	BookAdapter bookAdapter;

	private BroadcastReceiver listViewUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshList();
		}
	};

	private BroadcastReceiver closingTimesUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String updatedTimes = intent.getExtras().getString(
					DownloadClosingTimes.CLOSING_TIMES);
			closingTimes.setText(updatedTimes);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		registerReceiver(listViewUpdateReceiver, new IntentFilter(
				DownloadBookDetails.UPDATED_BOOK_DATABASE_INTENT));

		registerReceiver(closingTimesUpdateReceiver, new IntentFilter(
				DownloadClosingTimes.UPDATED_CLOSING_TIMES_INTENT));

		list = (ListView) findViewById(R.id.list);
		closingTimes = (TextView) findViewById(R.id.closingTimes);

		bookDataSource = new BookDataSource(this);
		Cursor cursor = bookDataSource.getCursor();
		bookAdapter = new BookAdapter(this, cursor);
		list.setAdapter(bookAdapter);

	}

	@Override
	public void onResume() {
		super.onResume();
		startService(new Intent(this, DownloadClosingTimes.class));
		startService(new Intent(this, DownloadBookDetails.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onStop() {
		bookDataSource.close();
	}

	private void refreshList() {
		Log.v(TAG, "refreshing list");
		bookAdapter.changeCursor(bookDataSource.getCursor());
	}
}
