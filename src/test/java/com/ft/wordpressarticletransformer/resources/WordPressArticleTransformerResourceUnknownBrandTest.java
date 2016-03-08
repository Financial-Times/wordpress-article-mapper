package com.ft.wordpressarticletransformer.resources;

import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.wordpressarticletransformer.component.ErbTemplatingHelper;
import com.ft.wordpressarticletransformer.model.Brand;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

public class WordPressArticleTransformerResourceUnknownBrandTest {

  private static final String CONFIG_FILE = "config-component-tests.yml";

  static {
    Map<String, Object> hieraData = new HashMap<>();
    hieraData.put("httpPort", "22040");
    hieraData.put("adminPort", "22041");
    hieraData.put("jerseyClientTimeout", "5000ms");
    hieraData.put("nativeReaderPrimaryNodes", "[\"localhost:8080:8080\"]");
    hieraData.put("queryClientTimeout", "5000ms");
    hieraData.put("queryReaderPrimaryNodes", "[\"localhost:14180:14181\"]");
    hieraData.put("alphavilleHost", "unlocalhost"); // in fact any value other than localhost (including the default) would do
    
    try {
      ErbTemplatingHelper.generateConfigFile("ft-wordpress_article_transformer/templates/config.yml.erb", hieraData,
          CONFIG_FILE);
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @ClassRule
  public static WordPressArticleTransformerAppRule wordPressArticleTransformerAppRule = new WordPressArticleTransformerAppRule(CONFIG_FILE);

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
