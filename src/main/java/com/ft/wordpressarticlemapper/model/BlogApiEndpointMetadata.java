package com.ft.wordpressarticlemapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.content.model.Syndication;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public class BlogApiEndpointMetadata {
    private final String host;
    private final Set<Brand> brands;
    private final String id;
    private final Syndication syndication;

    public BlogApiEndpointMetadata(@JsonProperty("host") String host,
                                   @JsonProperty("brands") Set<String> brands,
                                   @JsonProperty("id") String id,
                                   @JsonProperty("syndication") String syndication) {
        super();
        this.host = host;
        this.brands = new HashSet<>();

        for (String brandUri : brands) {
            Brand brand = new Brand(brandUri);
            this.brands.add(brand);
        }
        this.id = id;
        if (syndication == null) {
            this.syndication = Syndication.VERIFY;
        } else {
            this.syndication = Syndication.fromString(syndication);
        }
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

    public Syndication getSyndication() {
        return syndication;
    }
}
