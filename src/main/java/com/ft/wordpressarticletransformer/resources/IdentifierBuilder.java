package com.ft.wordpressarticletransformer.resources;

import com.ft.wordpressarticletransformer.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticletransformer.model.Identifier;
import com.ft.wordpressarticletransformer.response.Post;

import java.net.URI;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.core.UriBuilder;

public class IdentifierBuilder {

    private final BlogApiEndpointMetadataManager blogApiEndpointMetadataManager;

    public IdentifierBuilder(BlogApiEndpointMetadataManager blogApiEndpointMetadataManager) {
        this.blogApiEndpointMetadataManager = blogApiEndpointMetadataManager;
    }

    private static final String SYSTEM_ID = "http://api.ft.com/system/%s";


    public SortedSet<Identifier> buildIdentifiers(URI requestUri, Post post) {

        if (requestUri == null || post == null) {
            return null;
        }

        SortedSet<Identifier> identifiers = new TreeSet<>();
        BlogApiEndpointMetadata blogApiEndpointMetadata = blogApiEndpointMetadataManager.getBlogApiEndpointMetadataByUri(requestUri);
        if (blogApiEndpointMetadata == null) {
            return null;
        }

        String originatingSystemId = String.format(SYSTEM_ID, blogApiEndpointMetadata.getId());
        if (originatingSystemId == null) {
            return null;
        }

        identifiers.add(new Identifier(originatingSystemId, post.getUrl()));

        String additionalIdentifierValue = buildWordpressAdditionalIdentifier(blogApiEndpointMetadata, post.getId());
        identifiers.add(new Identifier(originatingSystemId, additionalIdentifierValue));

        return identifiers;
    }

    private String buildWordpressAdditionalIdentifier(BlogApiEndpointMetadata blogApiEndpointMetadata, Integer id) {
        String actualHost;
        String path = "/";

        String metadataHost = blogApiEndpointMetadata.getHost();

        if (metadataHost.contains("/")) {
            actualHost = metadataHost.substring(0, metadataHost.indexOf('/'));
            path = metadataHost.substring(metadataHost.indexOf('/')) + path;
        } else {
            actualHost = metadataHost;
        }
        URI wordpressAlternativeUri = UriBuilder.fromPath(path).host(actualHost).scheme("http").queryParam("p", id).build();
        return wordpressAlternativeUri.toASCIIString();
    }
}
