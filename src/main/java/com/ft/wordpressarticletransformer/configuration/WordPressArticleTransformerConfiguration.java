package com.ft.wordpressarticletransformer.configuration;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.wordpressarticletransformer.resources.BlogApiEndpointMetadata;
import com.google.common.base.Objects;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

public class WordPressArticleTransformerConfiguration extends Configuration {

	private final List<WordPressConnection> wordPressConnections;
    private final List<BlogApiEndpointMetadata> hostToBrands;
	private final JerseyClientConfiguration jerseyClientConfiguration;
	private final int numberOfConnectionAttempts;
    private String credentialsPath;

    public WordPressArticleTransformerConfiguration(
													@JsonProperty("credentialsPath") String credentialsPath,
													@JsonProperty("healthCheckWordPressConnections") List<WordPressConnection> wordPressConnections,
                                                    @JsonProperty("blogApiEndpointMetadata")List<BlogApiEndpointMetadata> blogApiEndpointMetadataList,
                                                    @JsonProperty("jerseyClient") JerseyClientConfiguration jerseyClientConfiguration,
                                                    @JsonProperty("numberOfConnectionAttempts") int numberOfConnectionAttempts) {
		super();
		this.credentialsPath = credentialsPath;
		this.wordPressConnections = wordPressConnections;
        this.hostToBrands = blogApiEndpointMetadataList;
		this.jerseyClientConfiguration = jerseyClientConfiguration;
		this.numberOfConnectionAttempts = numberOfConnectionAttempts;
	}

    @Valid @NotNull
    public String getCredentialsPath() {
        return credentialsPath;
    }


	@Valid
	@NotNull
	public List<WordPressConnection> getWordPressConnections() {
		return wordPressConnections;
	}

    @Valid
    @NotNull
    public List<BlogApiEndpointMetadata> getHostToBrands() {
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
