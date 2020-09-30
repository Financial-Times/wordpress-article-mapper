package com.ft.wordpressarticlemapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.jerseyhttpwrapper.config.EndpointConfiguration;

public class ReaderConfiguration {
  private final EndpointConfiguration endpointConfiguration;
  private final int numberOfConnectionAttempts;
  private final int timeoutMultiplier;
  private final String hostHeader;

  public ReaderConfiguration(
      @JsonProperty("endpointConfiguration") final EndpointConfiguration endpointConfiguration,
      @JsonProperty("numberOfConnectionAttempts") int numberOfConnectionAttempts,
      @JsonProperty("timeoutMultiplier") int timeoutMultiplier,
      @JsonProperty("hostHeader") String hostHeader) {
    this.endpointConfiguration = endpointConfiguration;
    this.numberOfConnectionAttempts = numberOfConnectionAttempts;
    this.timeoutMultiplier = timeoutMultiplier;
    this.hostHeader = hostHeader;
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

  public String getHostHeader() {
    return hostHeader;
  }
}
