package com.kenan.library;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final boolean DEBUG = true;

	TextView closingTimes;
	WebView webView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.v("MainActivity", "MainActivity onCreate");
		webView = (WebView) findViewById(R.id.webView);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});

		closingTimes = (TextView) findViewById(R.id.closingTimes);

	}

	@Override
	public void onResume() {
		super.onResume();
		// new DownloadClosingTimes(closingTimes).execute();
		new DownloadBookDetails(webView).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
