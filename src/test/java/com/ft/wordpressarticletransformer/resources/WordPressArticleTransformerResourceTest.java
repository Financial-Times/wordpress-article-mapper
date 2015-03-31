package com.ft.wordpressarticletransformer.resources;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;

import com.ft.api.util.transactionid.TransactionIdUtils;
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
import org.junit.Test;

public class WordPressArticleTransformerResourceTest {

    private static final Brand ALPHA_VILLE_BRAND = new Brand("http://api.ft.com/things/89d15f70-640d-11e4-9803-0800200c9a66");
    @ClassRule
	public static WordPressArticleTransformerAppRule wordPressArticleTransformerAppRule = new WordPressArticleTransformerAppRule("wordpress-article-transformer-test.yaml");
	
	private static final String UUID = "5c652c7e-c81e-4be7-8669-adeb5a5621db";
	private static final String URL = "url";
	private DateTime publishedDate = null;


	public static final String WILL_RETURN_200_PATH = "/request_to_word_press_200_ok/?json=1";
	private static final String WILL_RETURN_200 = "http://localhost:15670" + WILL_RETURN_200_PATH;

    private static final String WILL_RETURN_404 = "http://localhost:15670/request_to_word_press_404/?json=1";
    private static final String WILL_RETURN_200_INCORRECT_BLOG_TYPE = "http://localhost:15670/request_to_word_press_200_not_type_post/?json=1";
    private static final String WILL_RETURN_ERROR_NOT_FOUND = "http://localhost:15670/request_to_word_press_error_not_found/?json=1";
    private static final String WILL_RETURN_500 = "http://localhost:15670/request_to_word_press_500/?json=1";
    private static final String WILL_RETURN_NON_WORD_PRESS_RESPONSE = "http://localhost:15670/request_to_word_press_non_word_press_response/?json=1";
    private static final String WILL_RETURN_CANNOT_CONNECT = "http://localhost:15670/request_to_word_press_cannot_connect/?json=1";
    private static final String INVALID_URL = "werhjwekrhjerwkh";
    
    private static final String WILL_FAIL_BEFORE_REQUEST_TO_WORDPRESS = "http://localhost:15670/no_request_to_word_press_expected/?json=1";
   
	private Client client;

	@Before
	public void setup() {
		client = Client.create();
		client.setReadTimeout(50000);
		
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
        assertThat("brands", receivedContent.getBrands(), hasItem(ALPHA_VILLE_BRAND));
		assertThat("identifier authority", receivedContent.getIdentifiers().first().getAuthority(), is(equalTo("http://api.ft.com/system/FT-LABS-WP-1-24")));
		assertThat("identifier value", receivedContent.getIdentifiers().first().getIdentifierValue(), is(equalTo("http://uat.ftalphaville.ft.com/2014/10/21/2014692/the-6am-london-cut-277/")));
		assertThat("uuid", receivedContent.getUuid(), is(equalTo(UUID)));
		assertThat("published date", receivedContent.getPublishedDate(), is(publishedDate.toDate()));
	}

	
	@Test
	// this is what happens for posts that are in status=Pending, status=Draft, or visibility=Private....and deleted?
	public void shouldReturn404WhenWordpressReturnsStatusErrorAndErrorNotFound() {
	    final URI uri = buildTransformerUrl(UUID, WILL_RETURN_ERROR_NOT_FOUND);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
        assertThat("response", clientResponse.getEntity(String.class), containsString("uuid"));
	}


    /**
     * The endpoint generally returns 200, even for errors, so a 404 means we have the wrong URL.
     */
    @Test
	public void shouldReturn500When404ReturnedFromWordpress() {
		final URI uri = buildTransformerUrl(UUID, WILL_RETURN_404);
		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
	}

    @Test
    public void shouldReturn404WithUuidWhenTypeNotPostFromWordpress() {
        final URI uri = buildTransformerUrl(UUID, WILL_RETURN_200_INCORRECT_BLOG_TYPE);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
        assertThat("response", clientResponse.getEntity(String.class), containsString("markets-live"));
    }

    @Test
    public void shouldReturn400WhenUuidIsNotValid() {
        final URI uri = buildTransformerUrl("ABC-1234", WILL_FAIL_BEFORE_REQUEST_TO_WORDPRESS);
        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    }

	@Test
	public void shouldReturn405WhenNoUuidSupplied() {
		final URI uri = buildTransformerUrlWithIdMissing(WILL_FAIL_BEFORE_REQUEST_TO_WORDPRESS);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(405)));
	}
    
    @Test
    public void shouldReturn400WhenNoUrlSupplied() {
       final URI uri = buildTransformerUrlWithUrlMissing(UUID);

       final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
       assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    }

    
    @Test
    public void shouldReturn400WhenUrlIsNotValid() {
       final URI uri = buildTransformerUrl(UUID, INVALID_URL);

       final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
       assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    }

    @Test
    public void shouldReturn400WhenResponseNotAValidWordpressResponse() {
        final URI uri = buildTransformerUrl(UUID, WILL_RETURN_NON_WORD_PRESS_RESPONSE);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    }


	@Test
	public void shouldReturn503When500ReturnedFromWordpress() {
		final URI uri = buildTransformerUrl(UUID, WILL_RETURN_500);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
	}


    @Test
	public void shouldReturn503WhenCannotConnectToWordpress() {
		final URI uri = buildTransformerUrl(UUID, WILL_RETURN_CANNOT_CONNECT);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
	}


	@Test
	public void shouldAddApiKeyToUpstreamRequest() {
		final URI uri = buildTransformerUrl(UUID, WILL_RETURN_200);

        String transactionID = java.util.UUID.randomUUID().toString();

		final ClientResponse clientResponse = client.resource(uri)
                .header(TransactionIdUtils.TRANSACTION_ID_HEADER,transactionID)
                .get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

		String urlWithKeyAdded = WILL_RETURN_200_PATH + "&api_key="+ WP.EXAMPLE_API_KEY + "&cache_buster="+ transactionID;

		WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo(urlWithKeyAdded)));


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
