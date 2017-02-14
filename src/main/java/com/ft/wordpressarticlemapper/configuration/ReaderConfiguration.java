package com.ft.wordpressarticlemapper.configuration;

import com.ft.jerseyhttpwrapper.config.EndpointConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReaderConfiguration {
    private final EndpointConfiguration endpointConfiguration;
    private final int numberOfConnectionAttempts;
    private final int timeoutMultiplier;

    public ReaderConfiguration(@JsonProperty("endpointConfiguration") final EndpointConfiguration endpointConfiguration,
                               @JsonProperty("numberOfConnectionAttempts") int numberOfConnectionAttempts,
                               @JsonProperty("timeoutMultiplier") int timeoutMultiplier) {
        this.endpointConfiguration = endpointConfiguration;
        this.numberOfConnectionAttempts = numberOfConnectionAttempts;
        this.timeoutMultiplier = timeoutMultiplier;
    }

    public EndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    public int getNumberOfConnectionAttempts() {
        return numberOfConnectionAttempts;
    }

    public int getTimeoutMultiplier() {
        return timeoutMultiplier;
    }

}
