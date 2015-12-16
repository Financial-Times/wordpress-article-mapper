package com.ft.wordpressarticletransformer.resources;

import com.ft.api.jaxrs.errors.ErrorEntity;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

public class WordPressArticleTransformerResourceUnknownBrandTest {

    @ClassRule
    public static WordPressArticleTransformerAppRule wordPressArticleTransformerAppRule = new WordPressArticleTransformerAppRule("wordpress-article-transformer-test-unknown-brand.yaml");

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
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(500)));

        ErrorEntity receivedContent = clientResponse.getEntity(ErrorEntity.class);
        assertThat("title", receivedContent.getMessage(), containsString("Failed to resolve brand for uri "));
    }


    @After
    public void reset() {
        WireMock.resetToDefault();
    }

    private URI buildTransformerUrl(String uuid) {
        return UriBuilder
                .fromPath("content")
                .path("{uuid}")
                .scheme("http")
                .host("localhost")
                .port(wordPressArticleTransformerAppRule.getWordPressArticleTransformerLocalPort())
                .build(uuid);
    }
}
