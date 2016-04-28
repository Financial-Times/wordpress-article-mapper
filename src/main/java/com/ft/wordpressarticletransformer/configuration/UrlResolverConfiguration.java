package com.ft.wordpressarticletransformer.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import io.dropwizard.client.JerseyClientConfiguration;

public class UrlResolverConfiguration {
  private final Set<Pattern> patterns;
  private final JerseyClientConfiguration resolverConfiguration;
  private final ReaderConfiguration documentStoreConfiguration;
  private final int threadPoolSize;
  private final int linksPerThread;
  
  public UrlResolverConfiguration(@JsonProperty("patterns") List<Pattern> patterns,
                                  @JsonProperty("threadPoolSize") int threadPoolSize,
                                  @JsonProperty("linksPerThread") int linksPerThread,
                                  @JsonProperty("resolverConfiguration") JerseyClientConfiguration resolverConfiguration,
                                  @JsonProperty("documentStoreConfiguration") ReaderConfiguration documentStoreConfiguration) {
    
    this.patterns = ImmutableSet.copyOf(patterns);
    this.threadPoolSize = threadPoolSize;
    this.linksPerThread = linksPerThread;
    this.resolverConfiguration = resolverConfiguration;
    this.documentStoreConfiguration = documentStoreConfiguration;
  }
  
  public Set<Pattern> getPatterns() {
    return patterns;
  }
  
  public JerseyClientConfiguration getResolverConfiguration() {
    return resolverConfiguration;
  }
  
  public int getThreadPoolSize() {
    return threadPoolSize;
  }
  
  public int getLinksPerThread() {
    return linksPerThread;
  }
  
  public ReaderConfiguration getDocumentStoreConfiguration() {
    return documentStoreConfiguration;
  }
}
