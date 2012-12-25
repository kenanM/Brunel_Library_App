package com.kenan.library;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DownloadBookDetails extends Service {

	public static final String UPDATED_BOOK_DATABASE_INTENT = "com.kenan.library.update";
	private static final String BASE_URL = "http://library.brunel.ac.uk";
	private static final String TAG = "DownloadBookDetails";

	private HttpClient httpClient = new DefaultHttpClient();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String bookDetailsPage;
		if (MainActivity.DEBUG) {
			Log.v(TAG, "Loading from file...");
			bookDetailsPage = loadBookDetailsFromFile();
		} else {
			bookDetailsPage = downloadBookDetails();
		}

		List<Book> books = parse(bookDetailsPage);

		BookDataSource dataSource = new BookDataSource(this);
		dataSource.deleteBooks();
		dataSource.addBooks(books);
		sendBroadcast(new Intent(UPDATED_BOOK_DATABASE_INTENT));

		// Stop the service
		return START_NOT_STICKY;
	}

	private List<Book> parse(String html) {
		Log.v(TAG, "parsing...");

		Document doc = Jsoup.parse(html);

		// Labels contain the author and title of a book. they each have an
		// attribute called "for" which has a value of RENEW{digit}
		Elements labels = doc.getElementsByAttributeValueMatching("for",
				"RENEW\\d");
		Log.v(TAG, "found " + labels.size() + " books");

		Elements strongTags = doc.getElementsByTag("strong");
		int itemsEligibleForRenewal = Integer.parseInt(strongTags.remove(0)
				.text());

		if (labels.size() != itemsEligibleForRenewal) {
			Log.e(TAG, "Parsing disreprenceny!");
		}

		List<Book> books = new LinkedList<Book>();
		for (int i = 0; i < labels.size(); i++) {
			Element label = labels.get(i);
			Element date = strongTags.get(i * 2);
			Element renewalElement = strongTags.get(i * 2 + 1);
			int renewals = Integer.parseInt(renewalElement.text());
			Log.v(TAG, "Label: " + label.text());
			Log.v(TAG, "date: " + date.text());
			Log.v(TAG, "Renewals: " + renewals);
			books.add(new Book(label.text(), date.text(), renewals));
		}
		return books;
	}

	// TODO remove from release build.
	private String loadBookDetailsFromFile() {
		BufferedReader br;
		StringBuilder sb = new StringBuilder();

		try {
			br = new BufferedReader(new FileReader("sdcard/temp.html"));
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			Log.v(TAG, e.toString());
		}
		return sb.toString();
	}

	private String downloadBookDetails() {
		String result = "";
		try {

			// Download the main website library.brunel.ac.urk
			HttpGet get = new HttpGet(BASE_URL);
			HttpResponse response = httpClient.execute(get);

			// Find the redirect url (doesn't use a normal 302)
			String html = EntityUtils.toString(response.getEntity());
			Document doc = Jsoup.parse(html);
			Elements metaTags = doc.select("META");
			String nextLink = metaTags.first().attr("content");
			nextLink = nextLink.substring(nextLink.indexOf("URL=") + 4);
			Log.v(TAG, "Getting: " + BASE_URL + nextLink);
			get = new HttpGet(BASE_URL + nextLink);
			response = httpClient.execute(get);
			html = EntityUtils.toString(response.getEntity());

			// Login
			String postURL = findPostURL(html);
			HttpPost post = new HttpPost(postURL);
			// TODO: Move away from hard coding user_id
			String data = "user_id=68566027&password=";
			post.setEntity(new StringEntity(data));
			response = httpClient.execute(post);
			html = EntityUtils.toString(response.getEntity());

			nextLink = findLinkCalled("My Account", html);
			get = new HttpGet(nextLink);
			response = httpClient.execute(get);
			html = EntityUtils.toString(response.getEntity());

			nextLink = findLinkCalled("Renew My Materials", html);
			get = new HttpGet(nextLink);
			response = httpClient.execute(get);
			html = EntityUtils.toString(response.getEntity());

			Log.v(TAG, html);
			return html;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private String findPostURL(String html) {
		Log.v(TAG, "finding post URL");
		Document doc = Jsoup.parse(html);
		Element form = doc.select("form[method=post]").first();
		String postURL = BASE_URL + form.attr("action");
		Log.v(TAG, "postURL:" + postURL);
		return postURL;
	}

	private String findLinkCalled(String name, String html) {
		Document doc = Jsoup.parse(html);
		Elements hyperlinks = doc.select("a");
		for (Element link : hyperlinks) {
			if (link.text().equals(name)) {
				String redirectURL = BASE_URL + link.attr("href");
				Log.v(TAG, "Redirecting to: " + redirectURL);
				return (redirectURL);
			}
		} // TODO handle error conditions
		Log.e(TAG, "Error: unable to find a link called " + name);
		return "Error";
	}

	public void appendLog(String text) {
		File logFile = new File("sdcard/log.file");
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException();
	}

}
