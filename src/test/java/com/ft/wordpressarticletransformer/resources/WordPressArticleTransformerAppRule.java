package com.ft.wordpressarticletransformer.resources;

import com.ft.wordpressarticletransformer.WordPressArticleTransformerApplication;
import com.ft.wordpressarticletransformer.configuration.WordPressArticleTransformerConfiguration;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.dropwizard.testing.junit.DropwizardAppRule;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class WordPressArticleTransformerAppRule
    implements TestRule {
  
  public static int findAvailableWireMockPort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
    catch (IOException e) {
      throw new IllegalStateException("unable to find an available port", e);
    }
  }
  
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NO_HTML_NUMBER_ENTITY = "5c652c7e-c81e-4be7-8669-adeb5a5621da";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NO_HTML_NAME_ENTITY = "5c652c7e-c81e-4be7-8669-adeb5a5621dc";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS = "5c652c7e-c81e-4be7-8669-adeb5a5621dd";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_ERROR_NOT_FOUND = "5c652c7e-c81e-4be7-8669-adeb5a5621de";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_ERROR_UNKNOWN = "5c652c7e-c81e-4be7-8669-adeb5a5621d6";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_STATUS_UNKNOWN = "5c652c7e-c81e-4be7-8669-adeb5a5621d7";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_404 = "5c652c7e-c81e-4be7-8669-adeb5a5621df";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_INVALID_CONTENT_TYPE = "5c652c7e-c81e-4be7-8669-adeb5a5621d0";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NOT_TYPE_POST = "5c652c7e-c81e-4be7-8669-adeb5a5621d1";
    protected static final String UUID_MAP_TO_NO_REQUEST_TO_WORD_PRESS_EXPECTED = "ABC-1234";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_NON_WORD_PRESS_RESPONSE = "5c652c7e-c81e-4be7-8669-adeb5a5621d2";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_500 = "5c652c7e-c81e-4be7-8669-adeb5a5621d3";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_CANNOT_CONNECT = "5c652c7e-c81e-4be7-8669-adeb5a5621d4";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_502 = "5c652c7e-c81e-4be7-8669-adeb5a5621d5";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORDPRESS_NO_APIURL_ON_RESPONSE = "5c652c7e-c81e-4be7-8669-adeb5a5621db";

    private final DropwizardAppRule<WordPressArticleTransformerConfiguration> appRule;

    private final RuleChain ruleChain;

    private WireMockRule wordPressWireMockRule;

    public WordPressArticleTransformerAppRule(String configurationPath, int nativeRWPort) {
      appRule = new DropwizardAppRule<>(WordPressArticleTransformerApplication.class, configurationPath);

      wordPressWireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
          .withRootDirectory("src/test/resources/wordPress")
          .port(nativeRWPort)
          );
      
      ruleChain = RuleChain
              .outerRule(wordPressWireMockRule)
              .around(appRule);
    }
    
    @Override
    public Statement apply(Statement base, Description description) {
      final Statement stmt = ruleChain.apply(base, description);
      
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          try {
            stmt.evaluate();
          }
          finally {
            wordPressWireMockRule.shutdown();
          }
        }
      };
    }

    public int getWordPressArticleTransformerLocalPort() {
        return appRule.getLocalPort();
    }
}
