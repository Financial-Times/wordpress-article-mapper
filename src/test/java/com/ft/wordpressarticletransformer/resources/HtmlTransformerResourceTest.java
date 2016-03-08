package com.ft.wordpressarticletransformer.resources;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

public class HtmlTransformerResourceTest {

    @ClassRule
    public static WordPressArticleTransformerAppRule wordPressArticleTransformerAppRule = new WordPressArticleTransformerAppRule("wordpress-article-transformer-test.yaml");

    private Client client;

    @Before
    public void setup() {
        client = Client.create();
        client.setReadTimeout(50000);
    }

    private URI buildTransformerURI() {
        return UriBuilder
                .fromPath("transform-html-fragment")
                .scheme("http")
                .host("localhost")
                .port(wordPressArticleTransformerAppRule.getWordPressArticleTransformerLocalPort())
                .build();
    }

    @Test
    public void shouldHandleContentTypeAndAcceptCombinations() {
        String data = "Just some text because we aren't testing the transformation rules here";
        String [] types = {MediaType.TEXT_HTML, MediaType.TEXT_PLAIN };
        String [] accepts = {MediaType.TEXT_HTML, MediaType.APPLICATION_XML };
        for (String type: types) {
            for (String accept : accepts) {
                ClientResponse clientResponse = client.resource(buildTransformerURI())
                        .type(type)
                        .accept(accept)
                        .post(ClientResponse.class, data);
                assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
                String receivedContent = clientResponse.getEntity(String.class);
                assertThat(receivedContent, startsWith("<body>"));
                assertThat(receivedContent, endsWith("</body>"));
            }
        }
    }

    @Test
    public void shouldNotHandleAcceptCombinations() {
        String data = "Just some text because we aren't testing the transformation rules here";
        String [] types = {MediaType.TEXT_HTML, MediaType.TEXT_PLAIN};
        String [] accepts = {MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON };
        for (String type: types) {
            for (String accept : accepts) {
                ClientResponse clientResponse = client.resource(buildTransformerURI())
                        .type(type)
                        .accept(accept)
                        .post(ClientResponse.class, data);
                assertThat("response", clientResponse, hasProperty("status", equalTo(406)));
            }
        }
    }

    @Test
    public void shouldNotHandleContentTypeCombinations() {
        String data = "Just some text because we aren't testing the transformation rules here";
        String [] types = {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON};
        String [] accepts = {MediaType.TEXT_HTML, MediaType.APPLICATION_XML };
        for (String type: types) {
            for (String accept : accepts) {
                ClientResponse clientResponse = client.resource(buildTransformerURI())
                        .type(type)
                        .accept(accept)
                        .post(ClientResponse.class, data);
                assertThat("response", clientResponse, hasProperty("status", equalTo(415)));
            }
        }
    }
}
