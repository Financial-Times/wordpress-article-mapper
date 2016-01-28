package com.ft.wordpressarticletransformer;

import com.ft.api.jaxrs.errors.Errors;
import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.api.util.buildinfo.VersionResource;
import com.ft.api.util.transactionid.TransactionIdFilter;
import com.ft.bodyprocessing.richcontent.VideoMatcher;
import com.ft.jerseyhttpwrapper.ResilientClientBuilder;
import com.ft.jerseyhttpwrapper.config.EndpointConfiguration;
import com.ft.jerseyhttpwrapper.continuation.ExponentialBackoffContinuationPolicy;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;
import com.ft.wordpressarticletransformer.configuration.ReaderConfiguration;
import com.ft.wordpressarticletransformer.configuration.UrlResolverConfiguration;
import com.ft.wordpressarticletransformer.configuration.WordPressArticleTransformerConfiguration;
import com.ft.wordpressarticletransformer.health.NativeReaderPingHealthCheck;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerExceptionMapper;
import com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerResource;
import com.ft.wordpressarticletransformer.service.NativeReaderClient;
import com.ft.wordpressarticletransformer.service.WordpressContentSourceService;
import com.ft.wordpressarticletransformer.service.WordpressResponseValidator;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformer;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformerFactory;
import com.sun.jersey.api.client.Client;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.EnumSet;


public class WordPressArticleTransformerApplication extends Application<WordPressArticleTransformerConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordPressArticleTransformerApplication.class);

    public static void main(final String[] args) throws Exception {
        new WordPressArticleTransformerApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<WordPressArticleTransformerConfiguration> bootstrap) {
        bootstrap.addBundle(new AdvancedHealthCheckBundle());
    }

    @Override
    public void run(final WordPressArticleTransformerConfiguration configuration, final Environment environment) throws Exception {
        LOGGER.info("running with configuration: {}", configuration);

        environment.jersey().register(new BuildInfoResource());
        environment.jersey().register(new VersionResource());

        VideoMatcher videoMatcher = new VideoMatcher(configuration.getVideoSiteConfiguration());

        ReaderConfiguration nativeReaderConfiguration = configuration.getNativeReaderConfiguration();
        EndpointConfiguration nativeReaderEndpointConfiguration = nativeReaderConfiguration.getEndpointConfiguration();
        Client nativeReaderClient = ResilientClientBuilder.in(environment).using(nativeReaderEndpointConfiguration).withContinuationPolicy(
                new ExponentialBackoffContinuationPolicy(
                        nativeReaderConfiguration.getNumberOfConnectionAttempts(),
                        nativeReaderConfiguration.getTimeoutMultiplier()
                )
        ).build();
        
        WordPressArticleTransformerResource wordPressArticleTransformerResource =
                new WordPressArticleTransformerResource(
                        getBodyProcessingFieldTransformer(videoMatcher, configuration.getUrlResolverConfiguration()),
                        new BrandSystemResolver(configuration.getHostToBrands()),
                        new WordpressContentSourceService(
                                new WordpressResponseValidator(),
                                new NativeReaderClient(nativeReaderClient, nativeReaderEndpointConfiguration)
                        )
                );
        environment.jersey().register(wordPressArticleTransformerResource);

        environment.healthChecks().register("Native Reader ping", new NativeReaderPingHealthCheck(nativeReaderClient,
                nativeReaderEndpointConfiguration));
        environment.jersey().register(WordPressArticleTransformerExceptionMapper.class);
        Errors.customise(new WordPressArticleTransformerErrorEntityFactory());
        environment.servlets().addFilter("Transaction ID Filter",
                new TransactionIdFilter()).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/content/*");

    }

    private BodyProcessingFieldTransformer getBodyProcessingFieldTransformer(VideoMatcher videoMatcher, UrlResolverConfiguration configuration) {
      
      Client resolverClient = Client.create();
      setClientTimeouts(resolverClient, configuration.getResolverConfiguration());
      
      EndpointConfiguration queryEndpoint = configuration.getDocumentStoreQueryConfiguration().getEndpointConfiguration();
      URI documentStoreQueryURI = UriBuilder.fromPath(queryEndpoint.getPath())
                                            .scheme("http")
                                            .host(queryEndpoint.getHost())
                                            .port(queryEndpoint.getPort())
                                            .build();
      
      Client documentStoreQueryClient = Client.create();
      setClientTimeouts(documentStoreQueryClient, configuration.getDocumentStoreQueryConfiguration().getEndpointConfiguration().getJerseyClientConfiguration());
      
        return (BodyProcessingFieldTransformer) (new BodyProcessingFieldTransformerFactory(videoMatcher,
          configuration.getPatterns(),
          configuration.getBrandMappings(),
          resolverClient,
          documentStoreQueryClient, documentStoreQueryURI)).newInstance();
    }
    
    private void setClientTimeouts(Client client, JerseyClientConfiguration config) {
      Duration duration = config.getConnectionTimeout();
      if (duration != null) {
        client.setConnectTimeout((int)duration.toMilliseconds());
      }
      
      duration = config.getTimeout();
      if (duration != null) {
        client.setReadTimeout((int)duration.toMilliseconds());
      }
    }
}
