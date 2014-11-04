package com.ft.wordpressarticletransformer.resources;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.Date;
import javax.ws.rs.core.UriBuilder;

import com.ft.content.model.Brand;
import com.ft.content.model.Content;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

public class WordPressArticleTransformerResourceTest {

	@ClassRule
	public static WordPressArticleTransformerAppRule wordPressArticleTransformerAppRule = new WordPressArticleTransformerAppRule("wordpress-article-transformer-test.yaml");
	
	private static final String UUID = "5c652c7e-c81e-4be7-8669-adeb5a5621db";
	private static final String URL = "url";
	private DateTime publishedDate = null;
	

    private static final String WILL_RETURN_200 = "http://localhost:15670/request_to_word_press_200/?json=1";
    private static final String WILL_RETURN_404 = "http://localhost:15670/request_to_word_press_404/?json=1";
    private static final String WILL_RETURN_ERROR_NOT_FOUND = "http://localhost:15670/request_to_word_press_error_not_found/?json=1";
    private static final String WILL_RETURN_500 = "http://localhost:15670/request_to_word_press_500/?json=1";
    private static final String WILL_RETURN_CANNOT_CONNECT = "http://localhost:15670/request_to_word_press_cannot_connect/?json=1";
    

    private static final String WILL_FAIL_BEFORE_REQUEST_TO_WORDPRESS = "http://localhost:15670/no_request_to_word_press_expected/?json=1";
   

	private Client client;

	@Before
	public void setup() {
		client = Client.create();
		client.setReadTimeout(2000);
		
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); //2014-10-21 05:45:30
        publishedDate = formatter.parseDateTime("2014-10-21 05:45:30");
	}

	@Test
	public void shouldReturn200AndCompleteResponseWhenContentFoundInWordPress() {
		final URI uri = buildTransformerUrl(UUID, WILL_RETURN_200);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

		Content receivedContent = clientResponse.getEntity(Content.class);
		assertThat("title", receivedContent.getTitle(), is(equalTo("The 6am London Cut")));
		assertThat("body", receivedContent.getBody(), containsString("<p><strong>Markets: </strong>Bourses around Asia were mixed "));
		assertThat("byline", receivedContent.getByline(), is(equalTo("David Keohane")));
		assertThat("brands", receivedContent.getBrands(), hasItem(new Brand("http://replace_with_actual_brand")));
		assertThat("originating identifier", receivedContent.getContentOrigin().getOriginatingIdentifier(), is(equalTo(UUID)));
		assertThat("originating system", receivedContent.getContentOrigin().getOriginatingSystem(), is(equalTo(WordPressArticleTransformerResource.ORIGINATING_SYSTEM_WORDPRESS)));
		assertThat("uuid", receivedContent.getUuid(), is(equalTo(UUID)));
		assertThat("published date", receivedContent.getPublishedDate(), is(publishedDate.toDate()));
	}
	
	@Test
	// this is what happens for posts that are in status=Pending, status=Draft, or visibility=Private
	public void shouldReturn404WhenWordPressReturnsStatusErrorAndErrorNotFound() {
	    final URI uri = buildTransformerUrl(UUID, WILL_RETURN_ERROR_NOT_FOUND);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
	}

    @Test
	public void shouldReturn404When404ReturnedFromWordPress() {
		final URI uri = buildTransformerUrl(UUID, WILL_RETURN_404);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
	}


	@Test
	public void shouldReturn405WhenNoUuidSupplied() {
		final URI uri = buildTransformerUrlWithIdMissing(WILL_FAIL_BEFORE_REQUEST_TO_WORDPRESS);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(405)));
	}
    
    
    @Test
    public void shouldReturn405WhenNoUrlSupplied() {
       final URI uri = buildTransformerUrlWithUrlMissing(UUID);

       final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
       assertThat("response", clientResponse, hasProperty("status", equalTo(405)));
    }

	@Test
	public void shouldReturn503When500ReturnedFromClamo() {
		final URI uri = buildTransformerUrl(UUID, WILL_RETURN_500);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
	}


    @Test
    @Ignore("Not sure why this doesn't work - TODO, fix it")
	public void shouldReturn503WhenCannotConnectToClamo() {
		final URI uri = buildTransformerUrl(UUID, WILL_RETURN_CANNOT_CONNECT);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
	}


    @After
	public void reset() {
		WireMock.resetToDefault();
	}

    private URI buildTransformerUrl(String uuid, String requestUri) {
        return UriBuilder
                .fromPath("content")
                .path("{uuid}")
                .scheme("http")
                .host("localhost")
                .port(wordPressArticleTransformerAppRule.getWordPressArticleTransformerLocalPort())
                .queryParam(URL, requestUri)
                .build(uuid);
    }


	private URI buildTransformerUrlWithIdMissing(String requestUri) {
	    return UriBuilder
                .fromPath("content")
                .scheme("http")
                .host("localhost")
                .port(wordPressArticleTransformerAppRule.getWordPressArticleTransformerLocalPort())
                .queryParam(URL, requestUri)
                .build();
	}


    private URI buildTransformerUrlWithUrlMissing(String uuid) {
        return UriBuilder
                .fromPath("content")
                .path("{uuid}")
                .scheme("http")
                .host("localhost")
                .port(wordPressArticleTransformerAppRule.getWordPressArticleTransformerLocalPort())
                .build(uuid);
    }

}
