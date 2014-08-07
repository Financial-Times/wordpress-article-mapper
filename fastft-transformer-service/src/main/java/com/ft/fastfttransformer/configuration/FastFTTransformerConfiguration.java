package com.ft.fastfttransformer.configuration;

import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class FastFTTransformerConfiguration extends Configuration {

	private final ClamoConnection clamoConnection;
	private final int clamoContentId;

	public FastFTTransformerConfiguration(@JsonProperty("clamoConnection") ClamoConnection clamoConnection,
										  @JsonProperty("clamoContentId") int clamoContentId) {
		super();
		this.clamoConnection = clamoConnection;
		this.clamoContentId = clamoContentId;
	}

	@Valid
	@NotNull
	public ClamoConnection getClamoConnection() {
		return clamoConnection;
	}

	@Valid
	@NotNull
	public int getClamoContentId() {
		return clamoContentId;
	}

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("super", super.toString())
                .add("clamoConnection", clamoConnection)
				.add("clamoContentId", clamoContentId);
    }
    
    @Override
    public String toString() {
        return toStringHelper().toString();
    }
}
