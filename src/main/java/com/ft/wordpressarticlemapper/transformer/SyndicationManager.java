package com.ft.wordpressarticlemapper.transformer;

import com.ft.content.model.Syndication;
import com.ft.wordpressarticlemapper.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticlemapper.model.BlogApiEndpointMetadata;

import java.util.List;

public class SyndicationManager {

    private final BlogApiEndpointMetadataManager blogApiEndpointMetadataManager;

    public SyndicationManager(BlogApiEndpointMetadataManager blogApiEndpointMetadataManager) {
        this.blogApiEndpointMetadataManager = blogApiEndpointMetadataManager;
    }

    public Syndication getSyndicationByAuthority(String authority) {
        if (authority == null || authority.trim().isEmpty()) {
            return Syndication.VERIFY;
        }

        List<BlogApiEndpointMetadata> blogApiEndpointMetadata = blogApiEndpointMetadataManager.getBlogApiEndpointMetadata();

        for(BlogApiEndpointMetadata metadata : blogApiEndpointMetadata) {
            if (authority.contains(metadata.getId())) {
                Syndication syndication = metadata.getSyndication();
                return syndication != null ? syndication : Syndication.VERIFY;
            }
        }
        return Syndication.VERIFY;
    }
}
