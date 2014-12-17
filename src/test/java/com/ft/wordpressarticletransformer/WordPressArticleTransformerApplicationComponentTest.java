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
import static org.hamcrest.CoreMatchers.containsString;
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

    private static final String ADDITIONAL_PROPERTIES_EXAMPLE_BODY = "{\n"+
            "    \"status\": \"error\",\n"+
            "    \"error\": \"error\",\n"+
            "    \"foo\" : \"bar\",\n"+
            "    \"baz\" : {\n"+
            "        \"spong\" : \"bip\"\n"+
            "    }\n"+
            "}";

    private static final String UNUSUAL_ADDITIONAL_PROPERTIES_EXAMPLE_BODY = "{\n"+
            "    \"status\": \"unable to serve content due to an unexpected disaster involving blancmange\",\n"+
            "    \"error\": \"error\",\n"+
            "    \"foo\" : \"bar\",\n"+
            "    \"baz\" : {\n"+
            "        \"spong\" : \"bip\"\n"+
            "    }\n"+
            "}";


	@ClassRule
	public static WordPressArticleTransformerAppRule wordPressArticleTransformerAppRule = new WordPressArticleTransformerAppRule("wordpress-article-transformer-test.yaml");


    @Test
    public void shouldExposeUnexpectedJsonPropertiesInHealthCheckOutput() {

        stubWPHealthCheckEndpoint(ADDITIONAL_PROPERTIES_EXAMPLE_BODY);

        Client client  = Client.create();

        ClientResponse response = excerciseAdvancedHealthCheck(client);

        try {
            String responseContent = response.getEntity(String.class);
            assertAdditionalPropertiesStructureReturned(responseContent);
        } finally {
            response.close();
        }
    }


    @Test
    public void shouldExposeUnexpectedJsonPropertiesInHealthCheckOutputWhenWPReturnsUnknownError() {

        stubWPHealthCheckEndpoint(UNUSUAL_ADDITIONAL_PROPERTIES_EXAMPLE_BODY);

        Client client  = Client.create();

        ClientResponse response = excerciseAdvancedHealthCheck(client);

        try {
            String responseContent = response.getEntity(String.class);

            assertAdditionalPropertiesStructureReturned(responseContent);

        } finally {
            response.close();
        }
    }

    /** Assert two names and three values, including nested ones.
     * The values do not mean anything.
     */
    private void assertAdditionalPropertiesStructureReturned(String responseContent) {

        assertThat(responseContent,containsString("foo"));
        assertThat(responseContent,containsString("bar"));
        assertThat(responseContent,containsString("baz"));
        assertThat(responseContent,containsString("spong"));
        assertThat(responseContent,containsString("bip"));
    }

    private ClientResponse excerciseAdvancedHealthCheck(Client client) {
        URI healthcheckUri = UriBuilder.fromPath("__health")
                .host("localhost")
                .port(wordPressArticleTransformerAppRule.getWordPressArticleTransformerLocalPort()).scheme("http").build();

        return client.resource(healthcheckUri).get(ClientResponse.class);
    }

    @Test
	public void shouldAuthenticateHealthCheckRequests() {


        stubWPHealthCheckEndpoint(MINIMAL_EXAMPLE_POST_LIST_BODY);

        Client client  = Client.create();

        ClientResponse response = excerciseAdvancedHealthCheck(client);

		try {
			assertThat(response.getStatus(),is(200));

			verify(getRequestedFor(urlEqualTo(recentPostsListPathWithApiKey())));
		} finally {
			response.close();
		}

	}

    private void stubWPHealthCheckEndpoint(String body) {
        stubFor(get(urlEqualTo(recentPostsListPathWithApiKey())).willReturn(
                aResponse()
                    .withBody(body)
                    .withHeader("Content-Type", "application/json")));
    }

    private String recentPostsListPathWithApiKey() {
		return "/api/get_recent_posts/?count=1&api_key=" + WP.EXAMPLE_API_KEY;
	}


}
