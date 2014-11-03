package com.ft.wordpressarticletransformer.configuration;

import com.ft.content.model.Brand;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import io.dropwizard.client.JerseyClientConfiguration;

import java.util.List;

public class WordPressArticleTransformerConfiguration extends Configuration {

	private final List<WordPressConnection> wordPressConnections;
	private final Brand fastFtBrand;
	private final JerseyClientConfiguration jerseyClientConfiguration;
	private final int numberOfConnectionAttempts;

	public WordPressArticleTransformerConfiguration(@JsonProperty("healthCheckWordPressConnections") List<WordPressConnection> wordPressConnections,
													@JsonProperty("fastFtBrandId") String fastFtBrandId,
													@JsonProperty("jerseyClient") JerseyClientConfiguration jerseyClientConfiguration,
													@JsonProperty("numberOfConnectionAttempts") int numberOfConnectionAttempts) {
		super();
		this.wordPressConnections = wordPressConnections;
		this.fastFtBrand = new Brand(fastFtBrandId);
		this.jerseyClientConfiguration = jerseyClientConfiguration;
		this.numberOfConnectionAttempts = numberOfConnectionAttempts;
	}

	@Valid
	@NotNull
	public List<WordPressConnection> getWordPressConnections() {
		return wordPressConnections;
	}

	@Valid
	@NotNull
	public Brand getFastFtBrand() {
		return fastFtBrand;
	}

	@NotNull
	public JerseyClientConfiguration getJerseyClientConfiguration() {
		return jerseyClientConfiguration;
	}

	@NotNull
	public int getNumberOfConnectionAttempts() {
		return numberOfConnectionAttempts;
	}

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("super", super.toString())
                .add("wordPressConnections", wordPressConnections)
				.add("fastFtBrand", fastFtBrand)
				.add("jerseyClient", jerseyClientConfiguration)
				.add("numberOfConnectionAttempts", numberOfConnectionAttempts);
    }
    
    @Override
    public String toString() {
        return toStringHelper().toString();
    }
}