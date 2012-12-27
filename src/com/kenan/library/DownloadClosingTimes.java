package com.kenan.library;

import java.util.Calendar;

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
public class DownloadClosingTimes extends Service {

	private static final String HOME_PAGE_URL = "http://www.brunel.ac.uk/services/library";
	private static final String TAG = DownloadClosingTimes.class.toString();
	public static final String UPDATE_OPENING_TIMES_INTENT = "com.kenan.library.closingtimes.update";
	public static final String OPENING_TIMES_KEY = "closing times";

	String openingTimes;
	LocalStorage localStorage;
	int today;

	@Override
	public void onCreate() {
		localStorage = new LocalStorage(this);
	}

	/**
	 * Decide whether to get closingTimes from the Internet or from shared
	 * preferences
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String openingTimes = localStorage.getOpeningTimes();
		int dayOfLastUpdate = localStorage.getDayOfOpeningTimesUpdate();
		today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

		if (dayOfLastUpdate != today || openingTimes.equals("")) {
			Log.v(TAG, "Downloading opening times");
			broadcast(getString(R.string.gettingOpeningTimes));
			downloadOpeningTimes();
		}

		openingTimes = localStorage.getOpeningTimes();

		if (openingTimes.equals("")) {
			broadcast(getString(R.string.closing_times_error));
		} else {
			broadcast(openingTimes);
		}

		// Stop the service
		return START_NOT_STICKY;
	}

	private void broadcast(String message) {
		Intent broadcastIntent = new Intent(UPDATE_OPENING_TIMES_INTENT);
		broadcastIntent.putExtra(OPENING_TIMES_KEY, message);
		sendBroadcast(broadcastIntent);
	}

	private void downloadOpeningTimes() {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet get = new HttpGet(HOME_PAGE_URL);
		try {
			HttpResponse response = httpClient.execute(get);
			String homePage = EntityUtils.toString(response.getEntity());
			openingTimes = parseHomePage(homePage);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			openingTimes = "";
		}
		localStorage.updateOpeningTimes(openingTimes);
	}

	/** Extracts out the Opening-times */
	private String parseHomePage(String homePage) throws ParseException {
		Document doc = Jsoup.parse(homePage);
		Element element = doc.getElementById("opening-hours");
		if (element == null)
			throw new ParseException();
		return element.text();
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException();
	}
}
