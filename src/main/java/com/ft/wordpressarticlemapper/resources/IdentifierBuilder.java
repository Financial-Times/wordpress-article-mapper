package com.ft.wordpressarticlemapper.resources;

import com.ft.wordpressarticlemapper.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticlemapper.model.BlogApiEndpointMetadata;
import com.ft.wordpressarticlemapper.model.Identifier;
import com.ft.wordpressarticlemapper.response.Post;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.SortedSet;
import java.util.TreeSet;

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

        String additionalIdentifierValue = buildWordpressAdditionalIdentifier(blogApiEndpointMetadata, post);
        identifiers.add(new Identifier(originatingSystemId, additionalIdentifierValue));

        return identifiers;
    }

    private String buildWordpressAdditionalIdentifier(BlogApiEndpointMetadata blogApiEndpointMetadata, Post post) {

        URI postUri = UriBuilder.fromUri(post.getUrl()).build();
        String host = postUri.getHost();
        String scheme = postUri.getScheme();
        String path = "/";

        String metadataHost = blogApiEndpointMetadata.getHost();
        if (metadataHost.contains("/")) {
            path = metadataHost.substring(metadataHost.indexOf('/')) + path;
        }

        URI wordpressAlternativeUri = UriBuilder.fromPath(path).host(host).scheme(scheme).queryParam("p", post.getId()).build();
        return wordpressAlternativeUri.toASCIIString();
    }


    public Identifier build(URI uri) {
        if (uri == null) {
            return null;
        }
        BlogApiEndpointMetadata blogApiEndpointMetadata = blogApiEndpointMetadataManager.getBlogApiEndpointMetadataByUri(uri);

        if (blogApiEndpointMetadata != null && blogApiEndpointMetadata.getId() != null) {
            String originatingSystemId = String.format(SYSTEM_ID, blogApiEndpointMetadata.getId());
            return new Identifier(originatingSystemId, uri.toASCIIString());
        }

        return null;
    }


    public Identifier build(String identifierValue) {
        if (identifierValue == null) {
            return null;
        }
        return new Identifier(null, identifierValue);
    }
}
