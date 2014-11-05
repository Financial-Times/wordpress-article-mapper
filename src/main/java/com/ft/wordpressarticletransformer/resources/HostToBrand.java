package com.ft.wordpressarticletransformer.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.content.model.Brand;

import javax.validation.constraints.NotNull;

public class HostToBrand {
    private final String host;
    private final Brand brand;

    public HostToBrand (@JsonProperty("host") String host,
                        @JsonProperty("brand") String brandUri){
        super();
        this.host = host;
        this.brand = new Brand(brandUri);
    }

    @NotNull
    public String getHost() {
        return host;
    }

    @NotNull
    public Brand getBrand() {
        return brand;
    }
}
