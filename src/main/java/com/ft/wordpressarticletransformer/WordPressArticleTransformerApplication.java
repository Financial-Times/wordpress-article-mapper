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
import com.ft.wordpressarticletransformer.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticletransformer.configuration.ReaderConfiguration;
import com.ft.wordpressarticletransformer.configuration.UrlResolverConfiguration;
import com.ft.wordpressarticletransformer.configuration.WordPressArticleTransformerConfiguration;
import com.ft.wordpressarticletransformer.health.RemoteServiceDependencyHealthCheck;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.resources.HtmlTransformerResource;
import com.ft.wordpressarticletransformer.resources.IdentifierBuilder;
import com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerExceptionMapper;
import com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerResource;
import com.ft.wordpressarticletransformer.service.NativeReaderClient;
import com.ft.wordpressarticletransformer.service.WordpressContentSourceService;
import com.ft.wordpressarticletransformer.service.WordpressResponseValidator;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformer;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformerFactory;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.sun.jersey.api.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.ws.rs.core.UriBuilder;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;


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

        BlogApiEndpointMetadataManager blogApiEndpointMetadataManager = new BlogApiEndpointMetadataManager(configuration.getHostToBrands());

        WordPressArticleTransformerResource wordPressArticleTransformerResource =
                new WordPressArticleTransformerResource(
                        getBodyProcessingFieldTransformer(videoMatcher, configuration.getUrlResolverConfiguration(), blogApiEndpointMetadataManager),
                        new BrandSystemResolver(blogApiEndpointMetadataManager),
                        new WordpressContentSourceService(
                                new WordpressResponseValidator(),
                                new NativeReaderClient(nativeReaderClient, nativeReaderEndpointConfiguration)
                        ),
                        new IdentifierBuilder(blogApiEndpointMetadataManager)
                );
        environment.jersey().register(wordPressArticleTransformerResource);

        HtmlTransformerResource htmlTransformerResource = new HtmlTransformerResource(
                getBodyProcessingFieldTransformer(videoMatcher, configuration.getUrlResolverConfiguration(), blogApiEndpointMetadataManager),
                new BrandSystemResolver(blogApiEndpointMetadataManager)
        );
        environment.jersey().register(htmlTransformerResource);

        HealthCheckRegistry healthChecks = environment.healthChecks();
        healthChecks.register("Native Reader ping",
                new RemoteServiceDependencyHealthCheck("Native Reader", "nativerw",
                        "Publishing wordpress content won't work",
                        "https://sites.google.com/a/ft.com/technology/systems/dynamic-semantic-publishing/extra-publishing/native-store-reader-writer-run-book",
                        nativeReaderClient, nativeReaderEndpointConfiguration));

        healthChecks.register("Document Store ping",
                new RemoteServiceDependencyHealthCheck("Document Store", "document-store-api",
                        "Links to other FT content will not be resolved during publication, reducing data quality.",
                        "https://sites.google.com/a/ft.com/ft-technology-service-transition/home/run-book-library/documentstoreapi",
                        Client.create(), configuration.getUrlResolverConfiguration().getDocumentStoreConfiguration().getEndpointConfiguration()));

        environment.jersey().register(WordPressArticleTransformerExceptionMapper.class);
        Errors.customise(new WordPressArticleTransformerErrorEntityFactory());
        environment.servlets().addFilter("Transaction ID Filter",
                new TransactionIdFilter()).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/content/*");
        environment.servlets().addFilter("Transaction ID Filter",
                new TransactionIdFilter()).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/transform-html-fragment/*");

    }

    private BodyProcessingFieldTransformer getBodyProcessingFieldTransformer(VideoMatcher videoMatcher,
                                                                             UrlResolverConfiguration configuration,
                                                                             BlogApiEndpointMetadataManager blogApiEndpointMetadataManager) {

        Client resolverClient = Client.create();
        setClientTimeouts(resolverClient, configuration.getResolverConfiguration());

        EndpointConfiguration documentStoreEndpoint = configuration.getDocumentStoreConfiguration().getEndpointConfiguration();
        URI documentStoreBaseURI = UriBuilder.fromPath("/")
                .scheme("http")
                .host(documentStoreEndpoint.getHost())
                .port(documentStoreEndpoint.getPort())
                .build();

        Client documentStoreClient = Client.create();
        setClientTimeouts(documentStoreClient, documentStoreEndpoint.getJerseyClientConfiguration());


        int threadPoolSize = configuration.getThreadPoolSize();
        int maxLinks = threadPoolSize * configuration.getLinksPerThread();
        return (BodyProcessingFieldTransformer) (new BodyProcessingFieldTransformerFactory(videoMatcher,
                configuration.getPatterns(),
                blogApiEndpointMetadataManager,
                resolverClient, threadPoolSize, maxLinks,
                documentStoreClient, documentStoreBaseURI)).newInstance();
    }

    private void setClientTimeouts(Client client, JerseyClientConfiguration config) {
        Duration duration = config.getConnectionTimeout();
        if (duration != null) {
            client.setConnectTimeout((int) duration.toMilliseconds());
        }

        duration = config.getTimeout();
        if (duration != null) {
            client.setReadTimeout((int) duration.toMilliseconds());
        }
    }
}
