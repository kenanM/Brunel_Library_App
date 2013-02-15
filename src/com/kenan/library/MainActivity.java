package com.kenan.library;

import static com.kenan.library.DownloadClosingTimes.OPENING_TIMES_KEY;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.kenan.library.LibraryBookService.Operation;

//TODO needs to respond to invalid login
public class MainActivity extends SherlockActivity {

	private static final String TAG = MainActivity.class.toString();

	TextView openingTimes;
	ListView list;
	TextView lastRefresh;

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

	private BroadcastReceiver errorReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO do proper error reporting
			Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		registerReceiver(listViewUpdateReceiver, new IntentFilter(
				LibraryBookService.UPDATED_BOOK_DATABASE_INTENT));

		registerReceiver(openingTimesUpdateReceiver, new IntentFilter(
				DownloadClosingTimes.UPDATE_OPENING_TIMES_INTENT));

		registerReceiver(errorReceiver, new IntentFilter(
				LibraryBookService.ERROR_BROADCAST));

		list = (ListView) findViewById(R.id.list);
		openingTimes = (TextView) findViewById(R.id.closingTimes);
		lastRefresh = (TextView) findViewById(R.id.last_refresh);

		BookDataSource bookDataSource = new BookDataSource(this);
		Cursor cursor = bookDataSource.getCursor();
		bookAdapter = new BookAdapter(this, cursor);
		list.setAdapter(bookAdapter);
		bookDataSource.close();
	}

	@Override
	public void onResume() {
		super.onResume();
		lastRefresh.setText(LocalStorage.getRefreshDate(this));
		startService(new Intent(this, DownloadClosingTimes.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void setOpeningTimes(String newOpeningTimes) {
		openingTimes.setText(newOpeningTimes);
	}

	// Warning: Proguard must exempt this method
	public void refreshButtonClicked(MenuItem menuItem) {
		Intent intent = new Intent(this, LibraryBookService.class);
		intent.putExtra(Operation.KEY, Operation.REFRESH_BOOK_LIST);
		startService(intent);
	}

	// Warning: Proguard must exempt this method
	public void renewAllClicked(MenuItem menuItem) {
		Intent intent = new Intent(this, LibraryBookService.class);
		intent.putExtra(Operation.KEY, Operation.RENEW_BOOKS);
		startService(intent);
	}

	private void refreshList() {
		Log.v(TAG, "refreshing list");
		BookDataSource bookDataSource = new BookDataSource(this);
		bookAdapter.changeCursor(bookDataSource.getCursor());
		bookDataSource.close();
		lastRefresh.setText(LocalStorage.getRefreshDate(this));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(listViewUpdateReceiver);
		unregisterReceiver(openingTimesUpdateReceiver);
		unregisterReceiver(errorReceiver);
	}
}
