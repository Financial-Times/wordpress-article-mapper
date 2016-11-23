package com.ft.wordpressarticlemapper.configuration;

import com.ft.wordpressarticlemapper.model.BlogApiEndpointMetadata;

import java.net.URI;
import java.util.List;

public class BlogApiEndpointMetadataManager {

    private final List<BlogApiEndpointMetadata> blogApiEndpointMetadata;

    public BlogApiEndpointMetadataManager(List<BlogApiEndpointMetadata> blogApiEndpointMetadata) {
        this.blogApiEndpointMetadata = blogApiEndpointMetadata;
    }

    public BlogApiEndpointMetadata getBlogApiEndpointMetadataByUri(URI requestUri) {
        if (requestUri == null || requestUri.getHost() == null) {
            return null;
        }

        for (BlogApiEndpointMetadata blogApiEndpointMetadata : this.blogApiEndpointMetadata) {
            if (requestUri.getHost().concat(requestUri.getPath()).contains(blogApiEndpointMetadata.getHost())) {
                return blogApiEndpointMetadata;
            }
        }
        return null;
    }
}
