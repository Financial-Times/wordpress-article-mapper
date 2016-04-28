package com.ft.wordpressarticletransformer.resources;

import com.ft.wordpressarticletransformer.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticletransformer.model.Brand;

import java.net.URI;
import java.util.Set;

public class BrandSystemResolver {

    private final BlogApiEndpointMetadataManager blogApiEndpointMetadataManager;

    public BrandSystemResolver(BlogApiEndpointMetadataManager blogApiEndpointMetadataManager) {
        this.blogApiEndpointMetadataManager = blogApiEndpointMetadataManager;
    }

    public Set<Brand> getBrand(URI requestUri) {

        BlogApiEndpointMetadata blogApiEndpointMetadata = blogApiEndpointMetadataManager.getBlogApiEndpointMetadataByUri(requestUri);
        if (blogApiEndpointMetadata == null) {
            return null;
        }

        return blogApiEndpointMetadata.getBrands();

    }


}
