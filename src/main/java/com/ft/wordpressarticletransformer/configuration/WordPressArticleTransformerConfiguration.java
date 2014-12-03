package com.ft.wordpressarticletransformer.configuration;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.wordpressarticletransformer.resources.HostToBrand;
import com.google.common.base.Objects;

public class WordPressArticleTransformerConfiguration extends Configuration {

	private final String wordpressApiKey;
	private final List<WordPressConnection> wordPressConnections;
    private final List<HostToBrand> hostToBrands;
	private final JerseyClientConfiguration jerseyClientConfiguration;
	private final int numberOfConnectionAttempts;

	public WordPressArticleTransformerConfiguration(
													@JsonProperty("wordpressApiKey") String wordpressApiKey,
													@JsonProperty("healthCheckWordPressConnections") List<WordPressConnection> wordPressConnections,
                                                    @JsonProperty("hostToBrandMappings")List<HostToBrand> hostToBrands, @JsonProperty("jerseyClient") JerseyClientConfiguration jerseyClientConfiguration,
                                                    @JsonProperty("numberOfConnectionAttempts") int numberOfConnectionAttempts) {
		super();
		this.wordpressApiKey = wordpressApiKey;
		this.wordPressConnections = wordPressConnections;
        this.hostToBrands = hostToBrands;
		this.jerseyClientConfiguration = jerseyClientConfiguration;
		this.numberOfConnectionAttempts = numberOfConnectionAttempts;
	}

	@NotNull @Size(min=16)
	public String getWordpressApiKey() {
		return wordpressApiKey;
	}

	@Valid
	@NotNull
	public List<WordPressConnection> getWordPressConnections() {
		return wordPressConnections;
	}

    @Valid
    @NotNull
    public List<HostToBrand> getHostToBrands() {
        return hostToBrands;
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
				.add("jerseyClient", jerseyClientConfiguration)
				.add("numberOfConnectionAttempts", numberOfConnectionAttempts);
    }
    
    @Override
    public String toString() {
        return toStringHelper().toString();
    }

}
