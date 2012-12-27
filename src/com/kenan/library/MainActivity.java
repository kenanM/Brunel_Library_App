package com.kenan.library;

import static com.kenan.library.DownloadClosingTimes.OPENING_TIMES_KEY;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.toString();
	// TODO Remove
	public static final boolean DEBUG = true;

	TextView openingTimes;
	ListView list;
	Button refreshButton;

	BookDataSource bookDataSource;
	BookAdapter bookAdapter;

	private BroadcastReceiver listViewUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshList();
		}
	};

	private BroadcastReceiver openingTimesUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			setOpeningTimes(intent.getExtras().getString(OPENING_TIMES_KEY));
		}
	};

	private OnClickListener refreshButtonListener = new OnClickListener() {
		public void onClick(View v) {
			launchDownloadBookService();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		registerReceiver(listViewUpdateReceiver, new IntentFilter(
				DownloadBookDetails.UPDATED_BOOK_DATABASE_INTENT));

		registerReceiver(openingTimesUpdateReceiver, new IntentFilter(
				DownloadClosingTimes.UPDATE_OPENING_TIMES_INTENT));

		list = (ListView) findViewById(R.id.list);
		openingTimes = (TextView) findViewById(R.id.closingTimes);
		refreshButton = (Button) findViewById(R.id.refresh_button);
		refreshButton.setOnClickListener(refreshButtonListener);

		bookDataSource = new BookDataSource(this);
		Cursor cursor = bookDataSource.getCursor();
		bookAdapter = new BookAdapter(this, cursor);
		list.setAdapter(bookAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		startService(new Intent(this, DownloadClosingTimes.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onStop() {
		super.onStop();
		bookDataSource.close();
	}

	private void setOpeningTimes(String newOpeningTimes) {
		openingTimes.setText(newOpeningTimes);
	}

	private void launchDownloadBookService() {
		startService(new Intent(this, DownloadBookDetails.class));
	}

	private void refreshList() {
		Log.v(TAG, "refreshing list");
		bookAdapter.changeCursor(bookDataSource.getCursor());
	}
}
