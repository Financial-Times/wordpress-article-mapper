package com.ft.wordpressarticletransformer.resources;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static com.ft.wordpressarticletransformer.WordPressArticleTransformerComponentTestConfigHelper.CONFIG_FILE;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;

import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.wordpressarticletransformer.WordPressArticleTransformerComponentTestConfigHelper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class WordPressArticleTransformerResourceUnknownBrandTest {
    static {
        WordPressArticleTransformerComponentTestConfigHelper.setUp();
    }

	@ClassRule
	public static WordPressArticleTransformerAppRule wordPressArticleTransformerAppRule = new WordPressArticleTransformerAppRule(CONFIG_FILE);

	private static final String UUID = "5c652c7e-c81e-4be7-8669-adeb5a5621db";
	private static final String URL = "url";


	private static final String WILL_RETURN_500 = "http://localhost:15670/request_to_word_press_200_ok_success/?json=1";

	private Client client;

	@Before
	public void setup() {
		client = Client.create();
		client.setReadTimeout(50000);
	}

	// The brand is unknown, because the content URL we are passing is going to localhost,
	// but the config yaml file is only aware of content coming from unlocalhost,
	// ergo brand is unknown and cannot be assigned.
	@Test
	public void shouldReturn500WhenUnknownBrand() {
		final URI uri = buildTransformerUrl(UUID, WILL_RETURN_500);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(500)));

		ErrorEntity receivedContent = clientResponse.getEntity(ErrorEntity.class);
		assertThat("title", receivedContent.getMessage(), is(equalTo(String.format("Failed to resolve brand for uri [%s].",
				WILL_RETURN_500))));
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
}
