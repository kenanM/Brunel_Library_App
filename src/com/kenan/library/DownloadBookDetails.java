package com.kenan.library;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

public class DownloadBookDetails extends AsyncTask<Void, Void, String> {

	public static final String BASE_URL = "http://library.brunel.ac.uk";

	private static final String TAG = "DownloadBookDetails";

	private HttpClient httpClient = new DefaultHttpClient();

	WebView webView;

	public DownloadBookDetails(WebView bookDetails) {
		this.webView = bookDetails;
	}

	@Override
	protected String doInBackground(Void... params) {
		return download();
	}

	private String download() {
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

			//Login
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

			appendLog(html);

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

	@Override
	protected void onPostExecute(String data) {
		webView.loadData(data, "html/text", "UTF-8");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
