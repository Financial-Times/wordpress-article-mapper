package com.ft.fastfttransformer.configuration;

import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class FastFTTransformerConfiguration extends Configuration {

	private final ClamoConnection clamoConnection;
	
	public FastFTTransformerConfiguration(@JsonProperty("clamoConnection") ClamoConnection clamoConnection) {
		super();
		this.clamoConnection = clamoConnection;
	}

	@Valid
	@NotNull
	public ClamoConnection getClamoConnection() {
		return clamoConnection;
	}

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("super", super.toString())
                .add("clamoConnection", clamoConnection);
    }
    
    @Override
    public String toString() {
        return toStringHelper().toString();
    }

	
}
