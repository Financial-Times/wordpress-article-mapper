package com.ft.wordpressarticletransformer.configuration;

import io.dropwizard.client.JerseyClientConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.wordpressarticletransformer.model.Brand;
import com.google.common.collect.ImmutableSet;

public class UrlResolverConfiguration {
  private final Set<Pattern> patterns;
  private final Map<Pattern,Brand> brandMappings;
  private final JerseyClientConfiguration resolverConfiguration;
  private final ReaderConfiguration documentStoreConfiguration;
  private final int threadPoolSize;
  private final int linksPerThread;
  
  public UrlResolverConfiguration(@JsonProperty("patterns") List<Pattern> patterns,
                                  @JsonProperty("brandMappings") List<BrandMapping> brandMappings,
                                  @JsonProperty("threadPoolSize") int threadPoolSize,
                                  @JsonProperty("linksPerThread") int linksPerThread,
                                  @JsonProperty("resolverConfiguration") JerseyClientConfiguration resolverConfiguration,
                                  @JsonProperty("documentStoreConfiguration") ReaderConfiguration documentStoreConfiguration) {
    
    this.patterns = ImmutableSet.copyOf(patterns);
    this.brandMappings = brandMappings.stream().collect(
      Collectors.toMap(BrandMapping::getPattern, v -> new Brand(v.getBrand().toString())));
    this.threadPoolSize = threadPoolSize;
    this.linksPerThread = linksPerThread;
    this.resolverConfiguration = resolverConfiguration;
    this.documentStoreConfiguration = documentStoreConfiguration;
  }
  
  public Set<Pattern> getPatterns() {
    return patterns;
  }
  
  public Map<Pattern,Brand> getBrandMappings() {
    return brandMappings;
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
