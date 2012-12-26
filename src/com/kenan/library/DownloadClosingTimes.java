package com.kenan.library;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
//TODO switch to using a service and away from AsyncTask
public class DownloadClosingTimes extends AsyncTask<Void, Void, String> {

	public static final String CLOSING_TIMES_URL = "http://www.brunel.ac.uk/services/library";
	private static final String TAG = "DownloadClosingTimes";

	TextView text;

	public DownloadClosingTimes(TextView text) {
		this.text = text;
	}

	/**
	 * Downloads html from the brunel library webpage containing the closing
	 * times
	 */
	//TODO switch to using httpClient
	private String downloadClosingTimes() {
		InputStream is = null;
		String html = "";
		try {
			URL url = new URL(CLOSING_TIMES_URL);
			is = url.openStream();
			DataInputStream dis = new DataInputStream(new BufferedInputStream(
					is));

			String line = "";
			while ((line = dis.readLine()) != null) {
				html += line;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException ioe) {
				Log.v(TAG, "Unable to close inputStream");
			}
		}
		return html;
	}

	/** Extracts out the Opening-times */
	private String getClosingTimes(String html) {
		Document doc = Jsoup.parse(html);
		Element element = doc.getElementById("opening-hours");
		if (element == null)
			// TODO handle this error condition better
			return "";
		return element.text();
	}

	@Override
	protected String doInBackground(Void... params) {
		return getClosingTimes(downloadClosingTimes());
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
