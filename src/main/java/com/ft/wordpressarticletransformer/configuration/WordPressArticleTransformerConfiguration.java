package com.ft.wordpressarticletransformer.configuration;

import com.ft.content.model.Brand;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.util.List;

public class WordPressArticleTransformerConfiguration extends Configuration {

	private final List<WordPressConnection> wordPressConnections;
	private final Brand fastFtBrand;

	public WordPressArticleTransformerConfiguration(@JsonProperty("healthCheckBlogConnections") List<WordPressConnection> wordPressConnections,
													@JsonProperty("fastFtBrandId") String fastFtBrandId) {
		super();
		this.wordPressConnections = wordPressConnections;
		this.fastFtBrand = new Brand(fastFtBrandId);
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

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("super", super.toString())
                .add("wordPressConnections", wordPressConnections)
				.add("fastFtBrand", fastFtBrand);
    }
    
    @Override
    public String toString() {
        return toStringHelper().toString();
    }
}
