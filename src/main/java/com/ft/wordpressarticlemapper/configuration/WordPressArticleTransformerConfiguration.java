package com.ft.wordpressarticlemapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.bodyprocessing.richcontent.VideoSiteConfiguration;
import com.ft.platform.dropwizard.AppInfo;
import com.ft.platform.dropwizard.ConfigWithAppInfo;
import com.ft.wordpressarticlemapper.model.BlogApiEndpointMetadata;
import com.google.common.base.Objects;
import io.dropwizard.Configuration;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class WordPressArticleTransformerConfiguration extends Configuration
    implements ConfigWithAppInfo {
  @JsonProperty private AppInfo appInfo = new AppInfo();
  private final List<BlogApiEndpointMetadata> hostToBrands;

  private final List<VideoSiteConfiguration> videoSiteConfig;
  private final UrlResolverConfiguration urlResolverConfiguration;
  private final ConsumerConfiguration consumerConfiguration;
  private final ProducerConfiguration producerConfiguration;
  private final String contentUriPrefix;
  private final String canonicalWebUrlTemplate;

  public WordPressArticleTransformerConfiguration(
      @JsonProperty("blogApiEndpointMetadata")
          List<BlogApiEndpointMetadata> blogApiEndpointMetadataList,
      @JsonProperty("videoSiteConfig") List<VideoSiteConfiguration> videoSiteConfig,
      @JsonProperty("urlResolverConfiguration")
          final UrlResolverConfiguration urlResolverConfiguration,
      @JsonProperty("consumer") ConsumerConfiguration consumerConfiguration,
      @JsonProperty("producer") ProducerConfiguration producerConfiguration,
      @JsonProperty("contentUriPrefix") String contentUriPrefix,
      @JsonProperty("canonicalWebUrlTemplate") String canonicalWebUrlTemplate) {

    super();
    this.hostToBrands = blogApiEndpointMetadataList;
    this.videoSiteConfig = videoSiteConfig;
    this.urlResolverConfiguration = urlResolverConfiguration;
    this.consumerConfiguration = consumerConfiguration;
    this.producerConfiguration = producerConfiguration;
    this.contentUriPrefix = contentUriPrefix;
    this.canonicalWebUrlTemplate = canonicalWebUrlTemplate;
  }

  @Valid
  @NotNull
  public List<BlogApiEndpointMetadata> getHostToBrands() {
    return hostToBrands;
  }

  @NotNull
  public List<VideoSiteConfiguration> getVideoSiteConfiguration() {
    return videoSiteConfig;
  }

  public UrlResolverConfiguration getUrlResolverConfiguration() {
    return urlResolverConfiguration;
  }

  protected Objects.ToStringHelper toStringHelper() {
    return Objects.toStringHelper(this)
        .add("super", super.toString())
        .add("hostToBrands", hostToBrands)
        .add("videoSiteConfig", videoSiteConfig)
        .add("urlResolverConfiguration", urlResolverConfiguration);
  }

  @Override
  public String toString() {
    return toStringHelper().toString();
  }

  public ConsumerConfiguration getConsumerConfiguration() {
    return consumerConfiguration;
  }

  public ProducerConfiguration getProducerConfiguration() {
    return producerConfiguration;
  }

  public String getContentUriPrefix() {
    return contentUriPrefix;
  }

  @Override
  public AppInfo getAppInfo() {
    return appInfo;
  }

  @NotNull
  public String getCanonicalWebUrlTemplate() {
    return canonicalWebUrlTemplate;
  }
}
