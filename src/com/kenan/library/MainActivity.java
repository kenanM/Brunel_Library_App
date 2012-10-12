package com.kenan.library;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import com.kenan.library.R;

public class MainActivity extends Activity {

	TextView closingTimes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		closingTimes = (TextView) findViewById(R.id.closingTimes);
	}

	@Override
	public void onResume() {
		super.onResume();
		new DownloadClosingTimes(closingTimes).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
