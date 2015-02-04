package com.ft.wordpressarticletransformer.resources;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.content.model.Brand;

public class BlogApiEndpointMetadata {
    private final String host;
    private final Brand brand;
    private final String id;

    public BlogApiEndpointMetadata(@JsonProperty("host") String host,
                                   @JsonProperty("brand") String brandUri,
                                   @JsonProperty("id") String id){
        super();
        this.host = host;
        this.brand = new Brand(brandUri);
        this.id = id;
    }

    @NotNull
    public String getHost() {
        return host;
    }

    @NotNull
    public Brand getBrand() {
        return brand;
    }

    @NotNull
    public String getId() {
        return id;
    }
}
