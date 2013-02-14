package com.kenan.library;

import static com.kenan.library.LibraryBookService.CONNECTION_ERROR_BROADCAST;
import static com.kenan.library.LibraryBookService.PARSE_ERROR_BROADCAST;

import java.io.IOException;
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

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Sets a textView (given as parameter) to display the opening times for Brunel
 * library
 */
public class DownloadClosingTimes extends IntentService {

	private static final String HOME_PAGE_URL = "http://www.brunel.ac.uk/services/library";
	private static final String TAG = DownloadClosingTimes.class.toString();
	public static final String UPDATE_OPENING_TIMES_INTENT = "com.kenan.library.closingtimes.update";
	public static final String OPENING_TIMES_KEY = "closing times";

	String openingTimes;
	LocalStorage localStorage;
	int today;

	public DownloadClosingTimes() {
		super(DownloadClosingTimes.class.toString());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		localStorage = new LocalStorage(this);
	}

	/**
	 * Decide whether to get closingTimes from the Internet or from shared
	 * preferences
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

		String openingTimes = localStorage.getOpeningTimes();
		int dayOfLastUpdate = localStorage.getDayOfOpeningTimesUpdate();
		today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

		if (dayOfLastUpdate != today || openingTimes.equals("")) {
			Log.v(TAG, "Downloading opening times");
			broadcast(getString(R.string.gettingOpeningTimes));
			try {
				downloadOpeningTimes();
			} catch (IOException e) {
				sendBroadcast(new Intent(CONNECTION_ERROR_BROADCAST));
			} catch (ParseException e) {
				sendBroadcast(new Intent(PARSE_ERROR_BROADCAST));
			}
		}

		openingTimes = localStorage.getOpeningTimes();

		if (openingTimes.equals("")) {
			broadcast(getString(R.string.closing_times_error));
		} else {
			broadcast(openingTimes);
		}
		return;
	}

	private void broadcast(String message) {
		Intent broadcastIntent = new Intent(UPDATE_OPENING_TIMES_INTENT);
		broadcastIntent.putExtra(OPENING_TIMES_KEY, message);
		sendBroadcast(broadcastIntent);
	}

	private void downloadOpeningTimes() throws IOException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet get = new HttpGet(HOME_PAGE_URL);
		HttpResponse response = httpClient.execute(get);
		String homePage = EntityUtils.toString(response.getEntity());
		openingTimes = parseHomePage(homePage);

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
