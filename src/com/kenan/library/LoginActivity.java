package com.kenan.library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

public class LoginActivity extends SherlockActivity {

	TextView username;
	TextView password;
	TextView info;
	Button loginButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		username = (TextView) findViewById(R.id.username);
		password = (TextView) findViewById(R.id.password);
		info = (TextView) findViewById(R.id.info);

		LocalStorage localStorage = new LocalStorage(this);
		username.setText(localStorage.getUserName());
		password.setText(localStorage.getPassword());

		loginButton = (Button) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new LoginClickListener());

		registerReceiver(fail, new IntentFilter(
				LibraryBookService.INVALID_LOGIN));
		registerReceiver(success, new IntentFilter(
				LibraryBookService.UPDATED_BOOK_DATABASE_INTENT));

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(fail);
		unregisterReceiver(success);
	}

	public void attemptLogin() {
		if (username.getText().length() == 0) {
			info.setText(getString(R.string.missing_id));
		} else {
			info.setText(getString(R.string.attempting_login));
			LocalStorage localStorage = new LocalStorage(LoginActivity.this);
			localStorage.setUserNameAndPassword(username.getText().toString(),
					password.getText().toString());
			startService(new Intent(LoginActivity.this,
					LibraryBookService.class));
		}
	}

	private BroadcastReceiver success = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			info.setText(getString(R.string.logged_in));
			startActivity(new Intent(LoginActivity.this, MainActivity.class));
		}
	};

	private BroadcastReceiver fail = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			info.setText(getString(R.string.login_invalid));
		}
	};

	private class LoginClickListener implements OnClickListener {
		public void onClick(View v) {
			startActivity(new Intent(LoginActivity.this, MainActivity.class));
			attemptLogin();
		}
	}

}
