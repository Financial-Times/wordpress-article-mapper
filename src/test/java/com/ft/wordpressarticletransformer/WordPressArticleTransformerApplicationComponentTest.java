package com.ft.wordpressarticletransformer;

import com.ft.wordpressarticletransformer.resources.WP;
import com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * WordPressArticleTransformerApplicationComponentTest
 *
 * @author Simon
 */
public class WordPressArticleTransformerApplicationComponentTest {

	private static final String MINIMAL_EXAMPLE_POST_LIST_BODY = "{\n" +
			"\"status\" : \"ok\",\n" +
			"\"count\": 1,\n" +
			"\"count_total\" : 665,\n" +
			"\"pages\": 665,\n" +
			"\"posts\": [ ] }";


	@ClassRule
	public static WordPressArticleTransformerAppRule wordPressArticleTransformerAppRule = new WordPressArticleTransformerAppRule("wordpress-article-transformer-test.yaml");


	@Test
	public void shouldAuthenticateHealthCheckRequests() {


		stubFor(get(urlEqualTo(recentPostsListPathWithApiKey())).willReturn(
				aResponse()
					.withBody(MINIMAL_EXAMPLE_POST_LIST_BODY)
					.withHeader("Content-Type", "application/json")));

		Client client  = Client.create();

		URI healthcheckUri = UriBuilder.fromPath("__health")
				.host("localhost")
				.port(wordPressArticleTransformerAppRule.getWordPressArticleTransformerLocalPort()).scheme("http").build();

		ClientResponse response = client.resource(healthcheckUri).get(ClientResponse.class);

		try {
			assertThat(response.getStatus(),is(200));

			verify(getRequestedFor(urlEqualTo(recentPostsListPathWithApiKey())));
		} finally {
			response.close();
		}

	}

	private String recentPostsListPathWithApiKey() {
		return "/api/get_recent_posts/?count=1&api_key=" + WP.EXAMPLE_API_KEY;
	}


}
