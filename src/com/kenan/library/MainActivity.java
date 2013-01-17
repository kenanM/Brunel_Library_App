package com.kenan.library;

import static com.kenan.library.DownloadClosingTimes.OPENING_TIMES_KEY;

import com.kenan.library.LibraryBookService.Operation;

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
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.toString();

	public static final String ERROR_BROADCAST = "com.kenan.library.MainActivity.error";
	public static final String CONNECTION_ERROR_BROADCAST = ERROR_BROADCAST
			+ ".connection";
	public static final String LOGIN_ERROR_BROADCAST = ERROR_BROADCAST
			+ ".login";
	public static final String PARSE_ERROR_BROADCAST = ERROR_BROADCAST
			+ ".parse";

	TextView openingTimes;
	ListView list;
	Button refreshButton;
	Button renewButton;
	TextView lastRefresh;

	BookAdapter bookAdapter;
	LocalStorage localStorage;

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

	private OnClickListener refreshButtonListener = new OnClickListener() {
		public void onClick(View v) {
			updateBookDatabase();
		}
	};

	private OnClickListener renewButtonListener = new OnClickListener() {
		public void onClick(View v) {
			renewBooks();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		localStorage = new LocalStorage(this);

		registerReceiver(listViewUpdateReceiver, new IntentFilter(
				LibraryBookService.UPDATED_BOOK_DATABASE_INTENT));

		registerReceiver(openingTimesUpdateReceiver, new IntentFilter(
				DownloadClosingTimes.UPDATE_OPENING_TIMES_INTENT));

		registerReceiver(errorReceiver, new IntentFilter(ERROR_BROADCAST));

		list = (ListView) findViewById(R.id.list);
		openingTimes = (TextView) findViewById(R.id.closingTimes);
		refreshButton = (Button) findViewById(R.id.refresh_button);
		refreshButton.setOnClickListener(refreshButtonListener);
		renewButton = (Button) findViewById(R.id.renew_button);
		renewButton.setOnClickListener(renewButtonListener);
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
		lastRefresh.setText(localStorage.getRefreshDate());
		startService(new Intent(this, DownloadClosingTimes.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void setOpeningTimes(String newOpeningTimes) {
		openingTimes.setText(newOpeningTimes);
	}

	private void updateBookDatabase() {
		Intent intent = new Intent(this, LibraryBookService.class);
		intent.putExtra(Operation.KEY, Operation.REFRESH_BOOK_LIST);
		startService(intent);
	}

	private void renewBooks() {
		Intent intent = new Intent(this, LibraryBookService.class);
		intent.putExtra(Operation.KEY, Operation.RENEW_BOOKS);
		startService(intent);
	}

	private void refreshList() {
		Log.v(TAG, "refreshing list");
		BookDataSource bookDataSource = new BookDataSource(this);
		bookAdapter.changeCursor(bookDataSource.getCursor());
		bookDataSource.close();
		lastRefresh.setText(localStorage.getRefreshDate());
	}
}
