package com.ft.wordpressarticletransformer.configuration;

import com.ft.content.model.Brand;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class WordPressArticleTransformerConfiguration extends Configuration {

	private final ClamoConnection clamoConnection;
	private final int clamoContentId;
	private final Brand fastFtBrand;

	public WordPressArticleTransformerConfiguration(@JsonProperty("clamoConnection") ClamoConnection clamoConnection,
													@JsonProperty("clamoContentId") int clamoContentId,
													@JsonProperty("fastFtBrandId") String fastFtBrandId) {
		super();
		this.clamoConnection = clamoConnection;
		this.clamoContentId = clamoContentId;
		this.fastFtBrand = new Brand(fastFtBrandId);
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

	@Valid
	@NotNull
	public Brand getFastFtBrand() {
		return fastFtBrand;
	}

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("super", super.toString())
                .add("clamoConnection", clamoConnection)
				.add("clamoContentId", clamoContentId)
				.add("fastFtBrand", fastFtBrand);
    }
    
    @Override
    public String toString() {
        return toStringHelper().toString();
    }
}
