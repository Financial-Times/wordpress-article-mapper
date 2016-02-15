package com.ft.wordpressarticletransformer.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.bodyprocessing.richcontent.VideoSiteConfiguration;
import com.ft.wordpressarticletransformer.resources.BlogApiEndpointMetadata;
import com.google.common.base.Objects;

import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.List;


public class WordPressArticleTransformerConfiguration extends Configuration {

    private final List<BlogApiEndpointMetadata> hostToBrands;

    private final List<VideoSiteConfiguration> videoSiteConfig;
    private final ReaderConfiguration nativeReaderConfiguration;
    private final UrlResolverConfiguration urlResolverConfiguration;
    
    public WordPressArticleTransformerConfiguration(
            @JsonProperty("blogApiEndpointMetadata") List<BlogApiEndpointMetadata> blogApiEndpointMetadataList,
            @JsonProperty("videoSiteConfig") List<VideoSiteConfiguration> videoSiteConfig,
            @JsonProperty("nativeReaderConfiguration") final ReaderConfiguration nativeReaderConfiguration,
            @JsonProperty("urlResolverConfiguration") final UrlResolverConfiguration urlResolverConfiguration) {
      
        super();
        this.hostToBrands = blogApiEndpointMetadataList;
        this.videoSiteConfig = videoSiteConfig;
        this.nativeReaderConfiguration = nativeReaderConfiguration;
        this.urlResolverConfiguration = urlResolverConfiguration;
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

    @NotNull
    public ReaderConfiguration getNativeReaderConfiguration() {
        return nativeReaderConfiguration;
    }
    
    public UrlResolverConfiguration getUrlResolverConfiguration() {
      return urlResolverConfiguration;
    }
    
    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("super", super.toString())
                .add("nativeReaderConfiguration", nativeReaderConfiguration)
                .add("hostToBrands", hostToBrands)
                .add("videoSiteConfig", videoSiteConfig)
                .add("urlResolverConfiguration", urlResolverConfiguration);
    }
    
    @Override
    public String toString() {
        return toStringHelper().toString();
    }
}
