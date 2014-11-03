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
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Sarah to fix")
public class TransformerResourceTest {

	@ClassRule
	public static FastFtTransformerAppRule fastFtTransformerAppRule = new FastFtTransformerAppRule("wordpress-article-transformer-test.yaml");

	private static final Integer SAMPLE_CONTENT_ID = 186672;
	private static final int WILL_RETURN_404_AS_NO_DATA_NODE_IN_JSON = 1866711;
	private static final int WILL_RETURN_404_AS_DATA_NODE_EMPTY_IN_JSON = 1866712;
	private static final int WILL_RETURN_404 = 186673;
	private static final int WILL_RETURN_503 = 186674;
	private static final int WILL_RETURN_500 = 186675;
	private static final int WILL_RETURN_CANT_CONNECT = 186676;
	private static final int WILL_RETURN_200_NOT_FOUND = 18667999;
	private static final int WILL_RETURN_200_UNEXPECTED_STATUS = 186677;
	private static final int WILL_RETURN_200_UNEXPECTED_TITLE = 186678;
    private static final int WILL_RETURN_BROKEN_HTML = 37707001; // that's leet speak for error 1

	private Client client;

	@Before
	public void setup() {
		client = Client.create();
		client.setReadTimeout(5000);
	}

	@Test
	public void shouldReturn200AndCompleteResponseWhenContentFoundInClamo() {
		final URI uri = buildTransformerUrl(SAMPLE_CONTENT_ID);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

		Content receivedContent = clientResponse.getEntity(Content.class);
		assertThat("title", receivedContent.getTitle(), is(equalTo("US durable goods jump in June")));
		assertThat("body", receivedContent.getBody(), is(equalTo(EXPECTED_BODY)));
		assertThat("byline", receivedContent.getByline(), is(nullValue()));
		assertThat("brands", receivedContent.getBrands(), hasItem(new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b54")));
		assertThat("originating identifier", receivedContent.getContentOrigin().getOriginatingIdentifier(), is(equalTo(SAMPLE_CONTENT_ID.toString())));
		assertThat("originating system", receivedContent.getContentOrigin().getOriginatingSystem(), is(equalTo(TransformerResource.ORIGINATING_SYSTEM_FT_WORDPRESS)));
		assertThat("uuid", receivedContent.getUuid(), is(equalTo("ca93067c-6b1d-3b6f-bd54-f4cd5598961a")));
		assertThat("published date", receivedContent.getPublishedDate(), is(new Date(1406291632000L)));
	}

	@Test
	public void shouldReturn404WhenNoDataNodeReturnedFromClamo() {
		final URI uri = buildTransformerUrl(WILL_RETURN_404_AS_NO_DATA_NODE_IN_JSON);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
	}

	@Test
	public void shouldReturn404WhenEmptyDataNodeReturnedFromClamo() {
		final URI uri = buildTransformerUrl(WILL_RETURN_404_AS_DATA_NODE_EMPTY_IN_JSON);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
	}

	@Test
	public void shouldReturn405WhenNoIdSupplied() {
		final URI uri = buildTransformerUrlWithIdMissing();

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(405)));
	}

	@Test
	public void shouldReturn503When404ReturnedFromClamo() {
		final URI uri = buildTransformerUrl(WILL_RETURN_404);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
		String responseJson = clientResponse.getEntity(String.class);
		assertThat("responseJson", responseJson, containsString("Unexpected HTTP status"));
		assertThat("responseJson", responseJson, containsString("404"));
	}

	@Test
	public void shouldReturn503When503ReturnedFromClamo() {
		final URI uri = buildTransformerUrl(WILL_RETURN_503);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
	}

	@Test
	public void shouldReturn500When500ReturnedFromClamo() {
		final URI uri = buildTransformerUrl(WILL_RETURN_500);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
	}

    @Test
    public void shouldReturn500When500ReturnedHtmlIsNotFixable() {
        final URI uri = buildTransformerUrl(WILL_RETURN_BROKEN_HTML);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);

        verify(getRequestedFor(urlMatching("/api/\\?request\\=.*"+WILL_RETURN_BROKEN_HTML+".*")));

        assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
        assertThat("message keywords",clientResponse.getEntity(String.class), containsString("invalid body"));
    }

    @Test
	public void shouldReturn503WhenCannotConnectToClamo() {
		WireMock.stubFor(WireMock.get(WireMock.urlMatching("/api")).willReturn(WireMock.aResponse().withFixedDelay(5000)));
		final URI uri = buildTransformerUrl(WILL_RETURN_CANT_CONNECT);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
	}

	@Test
	public void shouldReturn404WhenContentNotFoundInClamo() {
		final URI uri = buildTransformerUrl(WILL_RETURN_200_NOT_FOUND);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
	}

	@Test
	public void shouldReturn500WhenUnexpectedStatus() {
		final URI uri = buildTransformerUrl(WILL_RETURN_200_UNEXPECTED_STATUS);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
		String responseJson = clientResponse.getEntity(String.class);
		assertThat("responseJson", responseJson, containsString("Unexpected status"));
		assertThat("responseJson", responseJson, containsString("errour"));
	}

	@Test
	public void shouldReturn500WhenUnexpectedTitle() {
		final URI uri = buildTransformerUrl(WILL_RETURN_200_UNEXPECTED_TITLE);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
		String responseJson = clientResponse.getEntity(String.class);
		assertThat("responseJson", responseJson, containsString("Unexpected title"));
		assertThat("responseJson", responseJson, containsString("Record manually removed by Jin"));
	}

    @Test
    public void shouldReturnErrorWhenIdIsNull(){
        final Client client = Client.create();
        URI uri = UriBuilder
                .fromPath("content")
                .path("{contentId}")
                .scheme("http")
                .host("localhost")
                .port(fastFtTransformerAppRule.getFastFtTransformerLocalPort()).build();
        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
    }

    @After
	public void reset() {
		WireMock.resetToDefault();
	}
    
	private URI buildTransformerUrl(int contentId) {
		return UriBuilder
				.fromPath("content")
				.path("{contentId}")
				.scheme("http")
				.host("localhost")
				.port(fastFtTransformerAppRule.getFastFtTransformerLocalPort())
				.build(contentId);
	}

	private URI buildTransformerUrlWithIdMissing() {
		return UriBuilder
				.fromPath("content")
				.scheme("http")
				.host("localhost")
				.port(fastFtTransformerAppRule.getFastFtTransformerLocalPort())
				.build();
	}

	private final static String EXPECTED_BODY = "<body>The question of why corporate America isn't investing much has become one of the most vexed as everyone scours for a potential catalyst to unlock faster economic growth.\n" +
			"\n" +
			"It's why the monthly report from the Commerce Department on durable goods, which are taken as a proxy for business spending, garners some attention from investors.\n" +
			"\n" +
			"The [latest figures](http://www.census.gov/manufacturing/m3/adv/pdf/durgd.pdf) for June show durable goods climbed 0.7 per cent to $239.9bn, up from a 1 per cent decline in May and better than the 0.5 per cent rise forecast.\n" +
			"\n" +
			"Of more interest, a measure of orders excluding aircraft and defence orders rose 1.4 per cent, snapping two months of declines. Economists had predicted a 1.3 per cent jump.\n" +
			"\n" +
			"Companies' orders for machinery drove the overall rise. Machinery orders climbed 2.4 per cent to $37.7bn. \n" +
			"\n" +
			"June's overall increase is at least further confirmation that the economy rebounded last quarter from its contraction at the start of the year. \n" +
			"\n" +
			"\n" +
			"\n" +
			" \n" +
			"\n" +
			"\n" +
			"\n</body>";

}
