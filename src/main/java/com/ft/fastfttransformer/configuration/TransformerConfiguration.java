package com.ft.fastfttransformer.configuration;

import io.dropwizard.Configuration;

import java.net.URL;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransformerConfiguration extends Configuration {

	@NotNull
	@JsonProperty
	private long slowRequestTimeout;
	@NotNull
	@JsonProperty
	private String slowRequestPattern;

	@NotNull
	@JsonProperty
	private URL clamoBaseURL;

	public long getSlowRequestTimeout() {
		return slowRequestTimeout;
	}

	public String getSlowRequestPattern() {
		return slowRequestPattern;
	}

	public URL getClamoBaseURL() {
		return clamoBaseURL;
	}
}
