package com.kenan.library;

import java.io.Serializable;

public class Book implements Serializable {

	private static final long serialVersionUID = 1L;
	private String title, dueDate; 
	private int renewals;

	public Book(String title, String dueDate, int renewals) {
		this.title = title;
		this.dueDate = dueDate;
		this.renewals = renewals;
	}

	public String getTitle() {
		return title;
	}

	public String getDateDue() {
		return dueDate;
	}

	public int getRenewals() {
		return renewals;
	}

	public String toJson() {
		return String.format(
				"{\"title\":\"&s\", \"date\":\"&s\",\"renewals\":\"s\"}",
				title, dueDate, renewals);
	}

	public String toString() {
		return title + " " + dueDate + " " + renewals;
	}
}
