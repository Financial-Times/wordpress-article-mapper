package com.ft.wordpressarticlemapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.bodyprocessing.richcontent.VideoSiteConfiguration;
import com.ft.wordpressarticlemapper.resources.BlogApiEndpointMetadata;
import com.google.common.base.Objects;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


public class WordPressArticleTransformerConfiguration extends Configuration {

    private final List<BlogApiEndpointMetadata> hostToBrands;

    private final List<VideoSiteConfiguration> videoSiteConfig;
    private final UrlResolverConfiguration urlResolverConfiguration;
    private final ConsumerConfiguration consumerConfiguration;
    private final ProducerConfiguration producerConfiguration;
    private final String contentUriPrefix;

    public WordPressArticleTransformerConfiguration(
            @JsonProperty("blogApiEndpointMetadata") List<BlogApiEndpointMetadata> blogApiEndpointMetadataList,
            @JsonProperty("videoSiteConfig") List<VideoSiteConfiguration> videoSiteConfig,
            @JsonProperty("urlResolverConfiguration") final UrlResolverConfiguration urlResolverConfiguration,
            @JsonProperty("consumer") ConsumerConfiguration consumerConfiguration,
            @JsonProperty("producer") ProducerConfiguration producerConfiguration,
            @JsonProperty("contentUriPrefix") String contentUriPrefix) {

        super();
        this.hostToBrands = blogApiEndpointMetadataList;
        this.videoSiteConfig = videoSiteConfig;
        this.urlResolverConfiguration = urlResolverConfiguration;
        this.consumerConfiguration = consumerConfiguration;
        this.producerConfiguration = producerConfiguration;
        this.contentUriPrefix = contentUriPrefix;
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
}
