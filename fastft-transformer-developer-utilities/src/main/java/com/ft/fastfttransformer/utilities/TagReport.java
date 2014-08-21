package com.ft.fastfttransformer.utilities;

import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

public class TagReport {
	
	private Map<String, Integer> tagCounts;
	private List<LinkDetails> linkDetails;

	public TagReport(Map<String, Integer> tagCounts, List<LinkDetails> linkDetails) {
		this.tagCounts = tagCounts;
		this.linkDetails = linkDetails;
	}

	public Map<String, Integer> getTagCounts() {
		return tagCounts;
	}

	public List<LinkDetails> getLinkDetails() {
		return linkDetails;
	}
	
	public String toString() {
		 return Objects.toStringHelper(this.getClass())
	                .add("tagCounts", tagCounts)
	                .add("links", linkDetails)
	                .toString();
	}

}
