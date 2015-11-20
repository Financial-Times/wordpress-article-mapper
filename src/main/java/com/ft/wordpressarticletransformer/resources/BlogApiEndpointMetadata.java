package com.ft.wordpressarticletransformer.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.wordpressarticletransformer.model.Brand;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public class BlogApiEndpointMetadata {
    private final String host;
    private final Set<Brand> brands;
    private final String id;

    public BlogApiEndpointMetadata(@JsonProperty("host") String host,
                                   @JsonProperty("brand") String brands,
                                   @JsonProperty("id") String id) {
        super();
        this.host = host;
        this.brands = new HashSet<>();
        String[] brandUris = brands.split(",");

        for (String brandUri : brandUris) {
            Brand brand = new Brand(brandUri);
            this.brands.add(brand);
        }
        this.id = id;
    }

    @NotNull
    public String getHost() {
        return host;
    }

    @NotNull
    public Set<Brand> getBrands() {
        return brands;
    }

    @NotNull
    public String getId() {
        return id;
    }
}
