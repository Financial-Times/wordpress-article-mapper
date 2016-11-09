package com.ft.wordpressarticlemapper.component;

import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ft.wordpressarticlemapper.WordPressArticleMapperApplication;
import com.ft.wordpressarticlemapper.configuration.WordPressArticleTransformerConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import io.dropwizard.testing.junit.DropwizardAppRule;

import org.junit.ClassRule;
import org.junit.Test;

public class AppSetupComponentTest {
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
  public static DropwizardAppRule<WordPressArticleTransformerConfiguration> appRule =
      new DropwizardAppRule<>(WordPressArticleMapperApplication.class, CONFIG_FILE);

  @SuppressWarnings("rawtypes")
  @Test
  public void thatBuildInfoResourceIsRegisteredAndWorking() {
    Client client = new Client();
    ClientResponse response = client.resource(format("http://localhost:%d%s", appRule.getLocalPort(), "/build-info")).get(ClientResponse.class);
    
    assertThat(response.getStatus(), equalTo(SC_OK));
    Map entity = response.getEntity(Map.class);
    assertThat(entity.keySet(), equalTo(Collections.singleton("buildInfo")));
    @SuppressWarnings("unchecked")
    Map<String, String> buildInfo = (Map) entity.get("buildInfo");
    assertThat(buildInfo, hasEntry("artifact.id", "wordpress-article-transformer"));
  }
}
