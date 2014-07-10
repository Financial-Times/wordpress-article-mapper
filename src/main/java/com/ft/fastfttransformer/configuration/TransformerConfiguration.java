package com.ft.fastfttransformer.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

public class TransformerConfiguration extends Configuration {

    @NotNull
    @JsonProperty
    private long slowRequestTimeout;
    @NotNull
    @JsonProperty
    private String slowRequestPattern;

    public long getSlowRequestTimeout() {
        return slowRequestTimeout;
    }

    public String getSlowRequestPattern() {
        return slowRequestPattern;
    }
}
