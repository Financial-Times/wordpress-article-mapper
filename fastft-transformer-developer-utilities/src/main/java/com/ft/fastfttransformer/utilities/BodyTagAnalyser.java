package com.ft.fastfttransformer.utilities;

import static com.jayway.restassured.path.json.JsonPath.from;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLInputFactory2;
import org.joda.time.DateTime;

import com.ft.bodyprocessing.xml.XMLEventReaderFactory;
import com.sun.jersey.api.client.Client;

public class BodyTagAnalyser {

	private static final String CLAMO_HOST = "clamo.ftdata.co.uk";
	private static final int CLAMO_PORT = 80;
	private static final String CLAMO_PATH = "/api";

	private static final int NUMBER_OF_THREADS = 10;
	private static final int BATCH_SIZE = 500;
	
	private static final String JSON_QUERY = "[{" +
			"\"arguments\":" +
				"{\"outputfields\":" +
					"{\"metadata\":false,\"status\":false,\"attachments\":false,\"authoravatar\":false,\"authorpseudonym\":false,"+
					"\"tags\":false,\"id\":false,\"title\":true,\"content\":\"html\",\"abstract\":false,\"contentlength\":false,"+
					"\"abstractlength\":false,\"currentversion\":false,\"issticky\":false" +
					"},"+
				"\"query\":\"status:live%20AND%20to:<date>\"," +
				"\"offset\":0," +
				"\"limit\":"+BATCH_SIZE +"}," +
			"\"action\":\"search\"}]";
	
	
	private XMLEventReaderFactory xmlEventReaderFactory;
	
	public BodyTagAnalyser() {
		xmlEventReaderFactory = new XMLEventReaderFactory((XMLInputFactory2) XMLInputFactory2.newInstance());
	}
	
	
	public AggregateTagReport analyseBodyTags() throws Exception {
		
		AggregateTagReport aggregateTagReport = new AggregateTagReport();
		
		URI fastFTSearchUri = getClamoBaseUrl();
		
		Client client = Client.create();
		
		DateTime latest = DateTime.now(); // start now, we're going back through the content
		// if you want to test just a small subset, need to set latest to around 20130530 so leaving this line here to save time :)
		//DateTime latest = new DateTime().withYear(2013).withMonthOfYear(5).withDayOfMonth(30);
		
		int i = 0; // just to avoid an infinite loop...
		while(latest != null && i < 100) {
			latest = processBatch(latest, fastFTSearchUri, client, aggregateTagReport);		
			i++;
		}	
		
		return aggregateTagReport;
	}

	private DateTime processBatch(DateTime latest, URI fastFTSearchUri, Client client, AggregateTagReport aggregateTagReport) throws Exception {
		
		System.out.println("Looking for results up to date=" + latest.toString("yyyyMMdd"));
		
		String encodedQueryString = buildSearchRequest(latest);
		
		String searchResults = client.resource(fastFTSearchUri).queryParam("request", encodedQueryString)
					.accept(MediaType.APPLICATION_JSON)
					.get(String.class);
		
		int numberOfResults = from(searchResults).getList("[0].data.results").size();
		
		if (numberOfResults == 0) {
			return null;
		}
		
		// check the earliest date in the batch
		long secondsSinceEpoch =  from(searchResults).getLong("[0].data.results["+ (numberOfResults - 1) +"].datepublished");
		DateTime justBeforeEarliestDateInBatch = new DateTime((secondsSinceEpoch-1) * 1000);
		
		// print out the uuids, just for sanity check
		System.out.println("Processing ids=[" + from(searchResults).getList("[0].data.results.uuidv3") + "]");
		
		//grab all the content items
		List<Map<String,Object>> contentFields = from(searchResults).getList("[0].data.results");
		
		ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		
		// for each one, create a future
		List<Future<TagReport>> futures = new ArrayList<Future<TagReport>>();
		for(Map<String,Object> contentItem: contentFields) {
			DateTime publishedDate = new DateTime(Long.parseLong((String)contentItem.get("datepublished")) * 1000);
			futures.add((Future<TagReport>) executorService.submit(new MakeFastFTRequestAndAnalyseBody(
					(String) contentItem.get("content"),
					(String) contentItem.get("url"),
					publishedDate)));
		}
		

		
		// All tasks have been submitted, we can begin the shutdown of our executor
        System.out.println("All bodies queued, starting shutdown...");
        executorService.shutdown();
		
		// Make sure all the bodies have been processed before start processing errors
		// Every ten seconds we print our progress
		int numberSeconds = 0;
        while (!executorService.isTerminated()) {
        	try {
        		executorService.awaitTermination(10, TimeUnit.SECONDS);
        		numberSeconds++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	System.out.println("Been processing for " + numberSeconds + "0 seconds");
        }
        
        System.out.println("ExecutorService has terminated");        
		
		// process all the futures and output a tag report
        for(Future<TagReport> future: futures) {
        	TagReport tagReport = future.get();
        	aggregateTagReport.addTagReport(tagReport);
        }
        
        return justBeforeEarliestDateInBatch;
	}


	private URI getClamoBaseUrl() {
		return UriBuilder.fromPath(CLAMO_PATH)
                .scheme("http")
                .host(CLAMO_HOST)
                .port(CLAMO_PORT)
                .build();
	}

	private String buildSearchRequest(DateTime now) throws UnsupportedEncodingException {
		String dateAsString = now.toString("yyyy-MM-dd'T'HH:mm:ss");
		return URLEncoder.encode(JSON_QUERY.replace("<date>", dateAsString), "UTF-8");
	}
	
	private class MakeFastFTRequestAndAnalyseBody implements Callable<TagReport>{
		
		private String htmlBody;
		private String fastFTUrl;
		private DateTime publishedDate;

		public MakeFastFTRequestAndAnalyseBody(String htmlBody, String fastFTUrl, DateTime publishedDate) throws Exception {
			this.htmlBody = htmlBody;
			this.fastFTUrl = fastFTUrl;
			this.publishedDate = publishedDate;
		}

		public TagReport call() throws Exception {
			Map<String,Integer> tagCount = new HashMap<String,Integer>();
			List<LinkDetails> links = new ArrayList<LinkDetails>();
			
			XMLEventReader xmlEventReader = xmlEventReaderFactory.createXMLEventReader(htmlBody);
			while (xmlEventReader.hasNext()) {
				XMLEvent event = xmlEventReader.nextEvent();
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					String name = startElement.getName().getLocalPart();
					updateTagCount(tagCount, name);
					if (name.equals("a")) {
						updateLinks(links, startElement);
					}
				}
			}

			return new TagReport(tagCount, links);
		}

		private void updateTagCount(Map<String, Integer> tagCount, String name) {
			if (!tagCount.containsKey(name)) {
				tagCount.put(name, 0);
			}
			Integer currentCount = tagCount.get(name);
			tagCount.put(name, currentCount+1);
		}

		private void updateLinks(List<LinkDetails> links, StartElement startElement) {
			links.add(new LinkDetails(getTagDetails(startElement), fastFTUrl, publishedDate));
		}

		private String getTagDetails(StartElement startElement) {
			StringBuilder output = new StringBuilder();
			output.append("<");
			output.append(startElement.getName().getLocalPart());
			@SuppressWarnings("unchecked")
			Iterator<Attribute> actualAttributesIterator = startElement.getAttributes();
			while (actualAttributesIterator.hasNext()) {
				Attribute attribute = actualAttributesIterator.next();
				output.append(" " + attribute.getName() + "=\"" + attribute.getValue() + "\"");
			}
			output.append(">");
			return output.toString();
		}
		
	}
	

}
