package com.ft.wordpressarticletransformer.resources;

import com.ft.wordpressarticletransformer.component.ErbTemplatingHelper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

public class HtmlTransformerResourceTest {
  private static final String CONFIG_FILE = "config-component-tests.yml";

  static {
    Map<String, Object> hieraData = new HashMap<>();
    hieraData.put("httpPort", "22040");
    hieraData.put("adminPort", "22041");
    hieraData.put("jerseyClientTimeout", "5000ms");
    hieraData.put("nativeReaderPrimaryNodes", "[\"localhost:8080:8080\"]");
    hieraData.put("queryClientTimeout", "5000ms");
    hieraData.put("queryReaderPrimaryNodes", "[\"localhost:14180:14181\"]");
    
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
