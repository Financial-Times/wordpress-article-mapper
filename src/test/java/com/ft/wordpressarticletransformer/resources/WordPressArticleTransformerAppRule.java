package com.ft.wordpressarticletransformer.resources;

import com.ft.wordpressarticletransformer.WordPressArticleTransformerApplication;
import com.ft.wordpressarticletransformer.configuration.WordPressArticleTransformerConfiguration;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class WordPressArticleTransformerAppRule implements TestRule {

	private static final int NATIVERW_PORT = 8080;
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NO_HTML_NUMBER_ENTITY = "5c652c7e-c81e-4be7-8669-adeb5a5621da";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NO_HTML_NAME_ENTITY = "5c652c7e-c81e-4be7-8669-adeb5a5621dc";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS = "5c652c7e-c81e-4be7-8669-adeb5a5621dd";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_ERROR_NOT_FOUND = "5c652c7e-c81e-4be7-8669-adeb5a5621de";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_404 = "5c652c7e-c81e-4be7-8669-adeb5a5621df";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_INVALID_CONTENT_TYPE = "5c652c7e-c81e-4be7-8669-adeb5a5621d0";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NOT_TYPE_POST = "5c652c7e-c81e-4be7-8669-adeb5a5621d1";
    protected static final String UUID_MAP_TO_NO_REQUEST_TO_WORD_PRESS_EXPECTED = "ABC-1234";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_NON_WORD_PRESS_RESPONSE = "5c652c7e-c81e-4be7-8669-adeb5a5621d2";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_500 = "5c652c7e-c81e-4be7-8669-adeb5a5621d3";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_CANNOT_CONNECT = "5c652c7e-c81e-4be7-8669-adeb5a5621d4";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_502 = "5c652c7e-c81e-4be7-8669-adeb5a5621d5";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORDPRESS_NO_APIURL_ON_RESPONSE = "5c652c7e-c81e-4be7-8669-adeb5a5621db";

    private WireMockClassRule wordPressWireMockRule = new WireMockClassRule(WireMockConfiguration.wireMockConfig()
			.withRootDirectory("src/test/resources/wordPress")
			.port(NATIVERW_PORT)
	);

	private final DropwizardAppRule<WordPressArticleTransformerConfiguration> appRule;

	private final RuleChain ruleChain;

	@Override
	public Statement apply(Statement base, Description description) {
		return ruleChain.apply(base, description);
	}

	public int getWordPressArticleTransformerLocalPort() {
		return appRule.getLocalPort();
	}

	public WordPressArticleTransformerAppRule(String configurationPath) {
		appRule = new DropwizardAppRule<>(WordPressArticleTransformerApplication.class, configurationPath);

		ruleChain = RuleChain
				.outerRule(wordPressWireMockRule)
				.around(appRule);
	}
}
