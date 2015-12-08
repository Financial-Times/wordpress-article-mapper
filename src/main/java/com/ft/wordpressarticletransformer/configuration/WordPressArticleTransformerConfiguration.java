package com.ft.wordpressarticletransformer.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.bodyprocessing.richcontent.VideoSiteConfiguration;
import com.ft.wordpressarticletransformer.resources.BlogApiEndpointMetadata;
import com.google.common.base.Objects;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class WordPressArticleTransformerConfiguration extends Configuration {

    private final List<BlogApiEndpointMetadata> hostToBrands;

	private final int numberOfConnectionAttempts;
    private final List<VideoSiteConfiguration> videoSiteConfig;
    private final NativeReaderConfiguration nativeReaderConfiguration;

    public WordPressArticleTransformerConfiguration(
                                                    @JsonProperty("blogApiEndpointMetadata")List<BlogApiEndpointMetadata> blogApiEndpointMetadataList,
                                                    @JsonProperty("numberOfConnectionAttempts") int numberOfConnectionAttempts,
                                                    @JsonProperty("videoSiteConfig") List<VideoSiteConfiguration> videoSiteConfig,
                                                    @JsonProperty("nativeReaderConfiguration") final NativeReaderConfiguration nativeReaderConfiguration){
		super();
        this.hostToBrands = blogApiEndpointMetadataList;
		this.numberOfConnectionAttempts = numberOfConnectionAttempts;
        this.videoSiteConfig = videoSiteConfig;
        this.nativeReaderConfiguration = nativeReaderConfiguration;
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
    public NativeReaderConfiguration getNativeReaderConfiguration() {
        return nativeReaderConfiguration;
    }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("super", super.toString())
				.add("numberOfConnectionAttempts", numberOfConnectionAttempts);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }


}
