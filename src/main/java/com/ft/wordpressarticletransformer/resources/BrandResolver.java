package com.ft.wordpressarticletransformer.resources;

import com.ft.content.model.Brand;

import java.net.URI;
import java.util.List;

public class BrandResolver {


    private final List<HostToBrand> hostToBrandMappings;

    public BrandResolver(List<HostToBrand> hostToBrandMappings) {
        this.hostToBrandMappings = hostToBrandMappings;
    }

    public Brand getBrand(URI requestUri) {

        if (requestUri == null || requestUri.getHost() == null) {

            return null;
        }

        for (HostToBrand hostToBrand : hostToBrandMappings) {
            if (requestUri.getHost().contains(
                    hostToBrand.getHost())) {

                return hostToBrand.getBrand();
            }
        }

        return null;
    }
}
