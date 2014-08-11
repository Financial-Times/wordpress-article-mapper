package com.ft.fastfttransformer.resources;

import com.ft.fastfttransformer.FastFTTransformerApplication;
import com.ft.fastfttransformer.configuration.FastFTTransformerConfiguration;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class FastFtTransformerAppRule implements TestRule {

	private static final int CLAMO_PORT = 11180;

	private WireMockClassRule methodeApiWireMockRule = new WireMockClassRule(WireMockConfiguration.wireMockConfig()
			.withRootDirectory("src/test/resources/clamo")
			.port(CLAMO_PORT)
	);

	private final DropwizardAppRule<FastFTTransformerConfiguration> appRule;

	private final RuleChain ruleChain;

	@Override
	public Statement apply(Statement base, Description description) {
		return ruleChain.apply(base, description);
	}

	public int getFastFtTransformerLocalPort() {
		return appRule.getLocalPort();
	}

	public FastFtTransformerAppRule(String configurationPath) {
		appRule = new DropwizardAppRule<>(FastFTTransformerApplication.class, configurationPath);

		ruleChain = RuleChain
				.outerRule(methodeApiWireMockRule)
				.around(appRule);
	}
}
