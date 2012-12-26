package com.kenan.library;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Sets a textView (given as parameter) to display the opening times for Brunel
 * library
 */
// TODO switch to using a service and away from AsyncTask
public class DownloadClosingTimes extends Service {

	private static final String CLOSING_TIMES_URL = "http://www.brunel.ac.uk/services/library";
	private static final String TAG = DownloadClosingTimes.class.toString();
	
	public static final String UPDATED_CLOSING_TIMES_INTENT = "com.kenan.library.closingtimes.update";
	public static final String CLOSING_TIMES = "closingTimes";

	HttpClient httpClient;

	@Override
	public void onCreate() {
		httpClient = new DefaultHttpClient();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String homePage;
		try {
			HttpGet get = new HttpGet(CLOSING_TIMES_URL);
			HttpResponse response = httpClient.execute(get);
			homePage = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			Log.e(TAG, "Network Exception!");
			homePage = "";
		} catch (ParseException e) {
			Log.e(TAG, "Parsing Exception");
			homePage = "";
		}

		String closingTimes = findClosingTimes(homePage);

		// TODO place closing times as well as time of update in
		// sharedPreference

		Intent broadcastIntent = new Intent(UPDATED_CLOSING_TIMES_INTENT);
		broadcastIntent.putExtra(CLOSING_TIMES, closingTimes);
		sendBroadcast(broadcastIntent);

		// Stop the service
		return START_NOT_STICKY;
	}

	/** Extracts out the Opening-times */
	private String findClosingTimes(String html) {
		Document doc = Jsoup.parse(html);
		Element element = doc.getElementById("opening-hours");
		if (element == null)
			// TODO handle this error condition better
			return "";
		return element.text();
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException();
	}
}
