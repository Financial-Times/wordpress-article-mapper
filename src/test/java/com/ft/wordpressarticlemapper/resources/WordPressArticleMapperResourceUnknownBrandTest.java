package com.ft.wordpressarticlemapper.resources;

import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.wordpressarticlemapper.component.ErbTemplatingHelper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.ft.wordpressarticlemapper.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

public class WordPressArticleMapperResourceUnknownBrandTest {

  private static final String CONFIG_FILE = "config-component-tests.yml";
  private static final int NATIVERW_PORT;
  private static final int DOC_STORE_PORT;

  static {
    NATIVERW_PORT = WordPressArticleTransformerAppRule.findAvailableWireMockPort();
    DOC_STORE_PORT = WordPressArticleTransformerAppRule.findAvailableWireMockPort();
    
    Map<String, Object> hieraData = new HashMap<>();
    hieraData.put("httpPort", "22040");
    hieraData.put("adminPort", "22041");
    hieraData.put("jerseyClientTimeout", "5000ms");
    hieraData.put("nativeReaderPrimaryNodes", String.format("[\"localhost:%s:%s\"]", NATIVERW_PORT, NATIVERW_PORT));
    hieraData.put("queryClientTimeout", "5000ms");
    hieraData.put("queryReaderPrimaryNodes", String.format("[\"localhost:%s:%s\"]", DOC_STORE_PORT, DOC_STORE_PORT));
    hieraData.put("alphavilleHost", "unlocalhost"); // in fact any value other than localhost (including the default) would do
    
    try {
      ErbTemplatingHelper.generateConfigFile("ft-wordpress_article_transformer/templates/config.yml.erb", hieraData,
          CONFIG_FILE);
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @ClassRule
  public static WordPressArticleTransformerAppRule wordPressArticleTransformerAppRule =
    new WordPressArticleTransformerAppRule(CONFIG_FILE, NATIVERW_PORT, DOC_STORE_PORT);

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
