package com.ft.fastfttransformer.utilities;

import org.joda.time.DateTime;

public class LinkDetails implements Comparable<LinkDetails> {

	private String link;
	private String fastFTUrl;
	private DateTime publishedDate;
	
	public LinkDetails(String link, String fastFTUrl, DateTime publishedDate) {
		super();
		this.link = link;
		this.fastFTUrl = fastFTUrl;
		this.publishedDate = publishedDate;
	}

	public String getLink() {
		return link;
	}

	public String getFastFTUrl() {
		return fastFTUrl;
	}

	public DateTime getPublishedDate() {
		return publishedDate;
	}

	public int compareTo(LinkDetails otherLinkDetails) {
		int result = this.getPublishedDate().compareTo(otherLinkDetails.getPublishedDate());
		if (result == 0) {
			return this.getLink().compareTo(otherLinkDetails.getLink());
		}
		return result;
	}
	
	public String toString() {
		StringBuilder output = new StringBuilder();
		output.append(publishedDate.toString("yyyy-MM-dd"));
		output.append(", ");
		output.append(fastFTUrl);
		output.append(", ");
		output.append(link);
		return output.toString();
	}
}
