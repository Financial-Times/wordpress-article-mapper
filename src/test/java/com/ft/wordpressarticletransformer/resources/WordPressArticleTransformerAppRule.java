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
