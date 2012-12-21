package com.kenan.library;

import java.io.Serializable;

public class Book implements Serializable {

	private static final long serialVersionUID = 1L;
	String title, date, renewals;

	public Book(String title, String date, String renewals) {
		this.title = title;
		this.date = date;
		this.renewals = renewals;
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
