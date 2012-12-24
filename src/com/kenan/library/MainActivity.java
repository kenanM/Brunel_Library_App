package com.kenan.library;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final boolean DEBUG = true;

	TextView closingTimes;
	ListView list;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		list = (ListView) findViewById(R.id.list);
		closingTimes = (TextView) findViewById(R.id.closingTimes);
		
		list.setAdapter(new BookAdapter(this, new Book[0]));
		
	}

	@Override
	public void onResume() {
		super.onResume();
		// new DownloadClosingTimes(closingTimes).execute();
		new DownloadBookDetails(this).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
