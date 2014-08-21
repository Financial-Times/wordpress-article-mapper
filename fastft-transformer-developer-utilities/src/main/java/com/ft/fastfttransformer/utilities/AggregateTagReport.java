package com.ft.fastfttransformer.utilities;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class AggregateTagReport {

	private TreeMap<String, Integer> aggregateTagCounts;
	private TreeSet<LinkDetails> allLinks;

	public AggregateTagReport() {
		aggregateTagCounts = new TreeMap<String, Integer>();
		allLinks = new TreeSet<LinkDetails>();
	}

	public void addTagReport(TagReport tagReport) {
		updateAggregateTagCounts(tagReport);
		updateLinks(tagReport);
	}
	
	private void updateAggregateTagCounts(TagReport tagReport) {
		Map<String, Integer> tagCounts = tagReport.getTagCounts();
		for (String name: tagCounts.keySet()) {
			if (!aggregateTagCounts.containsKey(name)) {
				aggregateTagCounts.put(name, 0);
			}
			Integer currentCount = aggregateTagCounts.get(name);
			Integer amountToAdd = tagCounts.get(name);
			aggregateTagCounts.put(name, currentCount+amountToAdd);
		}
	}

	private void updateLinks(TagReport tagReport) {
		allLinks.addAll(tagReport.getLinkDetails());
	}
	
	public String toString() {
		StringBuilder output = new StringBuilder();
		output.append("aggregateTagCounts\n");
		output.append(aggregateTagCounts);
		output.append("\n");
		output.append("allLinks\n");
		for (LinkDetails linkDetails: allLinks) {
			output.append(linkDetails);
			output.append("\n");
		}
		return output.toString();
	}


}
