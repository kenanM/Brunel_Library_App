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

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

/**
 * Sets a textView (given as parameter) to display the opening times for Brunel
 * library
 */
// TODO switch to using a service and away from AsyncTask
public class DownloadClosingTimes extends AsyncTask<Void, Void, String> {

	private static final String CLOSING_TIMES_URL = "http://www.brunel.ac.uk/services/library";
	private static final String TAG = DownloadClosingTimes.class.toString();

	TextView text;
	HttpClient httpClient;

	public DownloadClosingTimes(TextView text) {
		this.text = text;
		httpClient = new DefaultHttpClient();
	}

	/**
	 * Downloads HTML from the brunel library web page containing the closing
	 * times
	 */
	private String downloadHomePage() throws ParseException, IOException {
		HttpGet get = new HttpGet(CLOSING_TIMES_URL);
		HttpResponse response = httpClient.execute(get);
		return EntityUtils.toString(response.getEntity());
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
	protected String doInBackground(Void... params) {
		String homePage;
		try {
			homePage = downloadHomePage();
		} catch (IOException e) {
			Log.e(TAG, "Network Exception!");
			return "";
		} catch (ParseException e) {
			Log.e(TAG, "Parsing Exception");
			return "";
		}
		return findClosingTimes(homePage);
	}

	@Override
	protected void onPostExecute(String result) {
		if (result.equals("")) {
			// TODO handle this error condition better
			text.setText("Unable to download opening-times");
		} else {
			text.setText(result);
		}
	}
}
