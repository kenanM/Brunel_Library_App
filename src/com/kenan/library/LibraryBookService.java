package com.kenan.library;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
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

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class LibraryBookService extends IntentService {

	public static final String UPDATED_BOOK_DATABASE_INTENT = "com.kenan.library.downloadbookdetails.update";

	public static final String ERROR_BROADCAST = "com.kenan.library.MainActivity.error";
	public static final String CONNECTION_ERROR_BROADCAST = ERROR_BROADCAST
			+ ".connection";
	public static final String PARSE_ERROR_BROADCAST = ERROR_BROADCAST
			+ ".parse";
	public static final String INVALID_LOGIN = ERROR_BROADCAST
			+ ".invalidlogin";

	private static final String BASE_URL = "http://library.brunel.ac.uk";
	private static final String TAG = "DownloadBookDetails";

	public static final class Operation {
		public static final String KEY = "key";
		public static final String REFRESH_BOOK_LIST = "refresh_list";
		public static final String RENEW_BOOKS = "renew_books";
	}

	private HttpClient httpClient = new DefaultHttpClient();

	public LibraryBookService() {
		super(LibraryBookService.class.toString());
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		try {
			String bookDetailsPage = downloadBookDetailsPage();
			List<Book> books = parse(bookDetailsPage);

			updateBookDatabase(books);
			sendBroadcast(new Intent(UPDATED_BOOK_DATABASE_INTENT));

			String operation = intent.getExtras().getString(Operation.KEY);
			if (operation.equals(Operation.RENEW_BOOKS)) {
				books = renewBooks(bookDetailsPage);
				updateBookDatabase(books);
				sendBroadcast(new Intent(UPDATED_BOOK_DATABASE_INTENT));
			}

		} catch (NullPointerException e) {
			sendBroadcast(new Intent(PARSE_ERROR_BROADCAST));
			Log.i(TAG, "exception caught");
		} catch (IOException e) {
			sendBroadcast(new Intent(CONNECTION_ERROR_BROADCAST));
			Log.i(TAG, "exception caught");
		} catch (LoginException e) {
			sendBroadcast(new Intent(INVALID_LOGIN));
			LocalStorage.setUserNameAndPassword(this, "", "");
			Log.i(TAG, "exception caught");
		} finally {

		}

		return;
	}

	private void updateBookDatabase(List<Book> books) {
		BookDataSource dataSource = new BookDataSource(this);
		dataSource.deleteBooks();
		dataSource.addBooks(books);
		dataSource.close();
		LocalStorage.updateLastRefreshDate(this);
	}

	private List<Book> parse(String html) throws ParseException {
		Log.v(TAG, "parsing...");

		Document doc = Jsoup.parse(html);

		// Labels contain the author and title of a book. they each have an
		// attribute called "for" which has a value of RENEW{digit}
		Elements labels = doc.getElementsByAttributeValueMatching("for",
				"RENEW\\d");
		Log.v(TAG, "found " + labels.size() + " books");
		if (labels.size() == 0) {
			return new LinkedList<Book>();
		}

		Elements strongTags = doc.getElementsByTag("strong");
		int itemsEligibleForRenewal = Integer.getInteger(strongTags.remove(0)
				.text(), 0);

		if (labels.size() != itemsEligibleForRenewal) {
			Log.e(TAG, "Parsing disreprenceny!");
			// throw new ParseException();
		}

		List<Book> books = new LinkedList<Book>();
		for (int i = 0; i < labels.size(); i++) {
			Element label = labels.get(i);
			Element date = strongTags.get(i * 2);
			Element renewalElement = strongTags.get(i * 2 + 1);
			int renewals = Integer.getInteger(renewalElement.text(), 0);
			Log.v(TAG, "Label: " + label.text());
			Log.v(TAG, "date: " + date.text());
			Log.v(TAG, "Renewals: " + renewals);
			books.add(new Book(label.text(), date.text(), renewals));
		}
		return books;
	}

	private String downloadBookDetailsPage() throws ParseException,
			IOException, LoginException {

		// Download the homepage library.brunel.ac.uk
		HttpGet get = new HttpGet(BASE_URL);
		HttpResponse response = httpClient.execute(get);
		Log.i(TAG, "homePage downloaded");

		// Find and follow the redirect URL
		String html = EntityUtils.toString(response.getEntity());
		Document doc = Jsoup.parse(html);
		Elements metaTags = doc.select("META");
		String nextLink = metaTags.first().attr("content");
		nextLink = nextLink.substring(nextLink.indexOf("URL=") + 4);
		get = new HttpGet(BASE_URL + nextLink);
		response = httpClient.execute(get);
		html = EntityUtils.toString(response.getEntity());

		// Find and submit to the login form's POST URL
		String postURL = findPostURL(html, "new_session");
		HttpPost post = new HttpPost(postURL);

		String data = "user_id=" + LocalStorage.getUserName(this)
				+ "&password=" + LocalStorage.getPassword(this);
		Log.i(TAG, "posting: " + data);
		post.setEntity(new StringEntity(data));
		response = httpClient.execute(post);
		html = EntityUtils.toString(response.getEntity());

		// check for successful login
		if (html.contains("<li>Invalid login</li>")) {
			Log.e(TAG, "Invalid Login");
			throw new LoginException();
		} else {
			Log.i(TAG, html);
		}

		// Click on My Account
		nextLink = findLinkCalled("My Account", html);
		get = new HttpGet(nextLink);
		response = httpClient.execute(get);
		html = EntityUtils.toString(response.getEntity());

		// Click on Renew My Materials
		nextLink = findLinkCalled("Renew My Materials", html);
		get = new HttpGet(nextLink);
		response = httpClient.execute(get);
		html = EntityUtils.toString(response.getEntity());

		// Return the page listing all you books
		return html;

	}

	private String findPostURL(String html, String formName) {
		Log.v(TAG, "finding post URL in form: " + formName);
		Document doc = Jsoup.parse(html);
		Element form = doc.select("form[name=" + formName + "]").first();
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
		}
		Log.e(TAG, "Error: unable to find a link called " + name);
		throw new NullPointerException();
	}

	private List<Book> renewBooks(String bookDetailsPage) throws IOException {
		String postURL = findPostURL(bookDetailsPage, "renewitems");
		HttpPost post = new HttpPost(postURL);
		String data = "user_id=" + LocalStorage.getUserName(this)
				+ "&selection_type=all";
		post.setEntity(new StringEntity(data));
		HttpResponse response = httpClient.execute(post);
		return parseRenewedBooksPage(EntityUtils.toString(response.getEntity()));
	}

	private List<Book> parseRenewedBooksPage(String renewedBooksPage) {
		Document doc = Jsoup.parse(renewedBooksPage);
		Elements listOfDDTags = doc.getElementsByTag("dd");
		/*
		 * Example DD tag<dd> Data structures and algorithms in Java<br/>
		 * Goodrich, Michael T.<br/> QA76.73.J38G66 2004<br/>
		 * 
		 * Due: <strong>9/1/2013,23:59</strong><br/> Date renewed:
		 * <strong>2/1/2013,14:40</strong><br/> Times renewed:
		 * <strong>21</strong><br/> Renewals remaining:
		 * <strong>UNLIMITED</strong><br/> </dd>
		 */

		List<Book> books = new LinkedList<Book>();
		for (Element element : listOfDDTags) {
			// TODO look into TextNodes
			String title = element.text();
			Elements strongTags = element.getElementsByTag("strong");
			String dueDate = strongTags.get(0).text();
			int timesRenewed = Integer.getInteger(strongTags.get(2).text(), 0);
			books.add(new Book(title, dueDate, timesRenewed));
		}
		return books;
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException();
	}
}
