package com.kenan.library;

import java.io.Serializable;

public class Book implements Serializable {

	private static final long serialVersionUID = 1L;
	private String title, date, renewals;

	public Book(String title, String date, String renewals) {
		this.title = title;
		this.date = date;
		this.renewals = renewals;
	}

	public String getTitle() {
		return title;
	}

	public String getDateDue() {
		return date;
	}

	public String getRenewals() {
		return renewals;
	}

	public String toJson() {
		return String.format(
				"{\"title\":\"&s\", \"date\":\"&s\",\"renewals\":\"s\"}",
				title, date, renewals);
	}

	public String toString() {
		return title + " " + date + " " + renewals;
	}
}
