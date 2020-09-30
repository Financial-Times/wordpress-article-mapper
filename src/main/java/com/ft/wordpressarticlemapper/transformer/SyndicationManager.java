package com.ft.wordpressarticlemapper.transformer;

import com.ft.content.model.Syndication;
import com.ft.wordpressarticlemapper.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticlemapper.model.BlogApiEndpointMetadata;
import java.net.URI;

public class SyndicationManager {

  private final BlogApiEndpointMetadataManager blogApiEndpointMetadataManager;

  public SyndicationManager(BlogApiEndpointMetadataManager blogApiEndpointMetadataManager) {
    this.blogApiEndpointMetadataManager = blogApiEndpointMetadataManager;
  }

  public Syndication getSyndicationByUri(URI uri) {
    if (uri == null) {
      return Syndication.VERIFY;
    }

    if (blogApiEndpointMetadataManager == null) {
      return Syndication.VERIFY;
    }

    BlogApiEndpointMetadata blogApiEndpointMetadata =
        blogApiEndpointMetadataManager.getBlogApiEndpointMetadataByUri(uri);

    if (blogApiEndpointMetadata == null) {
      return Syndication.VERIFY;
    }

    Syndication syndication = blogApiEndpointMetadata.getSyndication();
    return syndication != null ? syndication : Syndication.VERIFY;
  }
}
