package com.ft.wordpressarticlemapper;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.Errors;
import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.api.util.buildinfo.VersionResource;
import com.ft.api.util.transactionid.TransactionIdFilter;
import com.ft.bodyprocessing.richcontent.VideoMatcher;
import com.ft.jerseyhttpwrapper.ResilientClientBuilder;
import com.ft.jerseyhttpwrapper.config.EndpointConfiguration;
import com.ft.message.consumer.MessageListener;
import com.ft.message.consumer.MessageQueueConsumerInitializer;
import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messagequeueproducer.QueueProxyProducer;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;
import com.ft.wordpressarticlemapper.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticlemapper.configuration.ConsumerConfiguration;
import com.ft.wordpressarticlemapper.configuration.ProducerConfiguration;
import com.ft.wordpressarticlemapper.configuration.UrlResolverConfiguration;
import com.ft.wordpressarticlemapper.configuration.WordPressArticleTransformerConfiguration;
import com.ft.wordpressarticlemapper.health.CanConnectToMessageQueueProducerProxyHealthcheck;
import com.ft.wordpressarticlemapper.health.RemoteServiceDependencyHealthCheck;
import com.ft.wordpressarticlemapper.messaging.MessageProducingContentMapper;
import com.ft.wordpressarticlemapper.messaging.NativeCmsPublicationEventsListener;
import com.ft.wordpressarticlemapper.resources.BrandSystemResolver;
import com.ft.wordpressarticlemapper.resources.HtmlTransformerResource;
import com.ft.wordpressarticlemapper.resources.IdentifierBuilder;
import com.ft.wordpressarticlemapper.resources.WordPressArticleMapperResource;
import com.ft.wordpressarticlemapper.resources.WordPressArticleTransformerExceptionMapper;
import com.ft.wordpressarticlemapper.transformer.BodyProcessingFieldTransformer;
import com.ft.wordpressarticlemapper.transformer.BodyProcessingFieldTransformerFactory;
import com.ft.wordpressarticlemapper.transformer.WordPressBlogPostContentMapper;
import com.ft.wordpressarticlemapper.transformer.WordPressLiveBlogContentMapper;
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


public class WordPressArticleMapperApplication extends Application<WordPressArticleTransformerConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordPressArticleMapperApplication.class);

    public static void main(final String[] args) throws Exception {
        new WordPressArticleMapperApplication().run(args);
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
        final ObjectMapper objectMapper = environment.getObjectMapper();

        BlogApiEndpointMetadataManager blogApiEndpointMetadataManager = new BlogApiEndpointMetadataManager(configuration.getHostToBrands());

        WordPressArticleMapperResource wordPressArticleTransformerResource =
                new WordPressArticleMapperResource(
                        getBodyProcessingFieldTransformer(videoMatcher, configuration.getUrlResolverConfiguration(), blogApiEndpointMetadataManager),
                        new BrandSystemResolver(blogApiEndpointMetadataManager),
                        new IdentifierBuilder(blogApiEndpointMetadataManager)
                );
        environment.jersey().register(wordPressArticleTransformerResource);

        HtmlTransformerResource htmlTransformerResource = new HtmlTransformerResource(
                getBodyProcessingFieldTransformer(videoMatcher, configuration.getUrlResolverConfiguration(), blogApiEndpointMetadataManager)
        );
        environment.jersey().register(htmlTransformerResource);

        Client consumerClient = getConsumerClient(environment, configuration.getConsumerConfiguration());
        final MessageProducer producer = configureMessageProducer(environment, configuration.getProducerConfiguration());
        final UriBuilder contentUriBuilder = UriBuilder.fromUri(configuration.getContentUriPrefix()).path("{uuid}");

        BodyProcessingFieldTransformer bodyProcessingFieldTransformer = getBodyProcessingFieldTransformer(
                videoMatcher,
                configuration.getUrlResolverConfiguration(),
                blogApiEndpointMetadataManager);

        BrandSystemResolver brandSystemResolver = new BrandSystemResolver(blogApiEndpointMetadataManager);

        IdentifierBuilder identifierBuilder = new IdentifierBuilder(blogApiEndpointMetadataManager);

        MessageProducingContentMapper contentMapper = new MessageProducingContentMapper(
                new WordPressBlogPostContentMapper(brandSystemResolver, bodyProcessingFieldTransformer, identifierBuilder),
                new WordPressLiveBlogContentMapper(brandSystemResolver, identifierBuilder),
                objectMapper,
                configuration.getConsumerConfiguration().getSystemCode(),
                producer,
                contentUriBuilder);

        String systemCode = configuration.getConsumerConfiguration().getSystemCode();
        MessageListener listener = new NativeCmsPublicationEventsListener(contentMapper, objectMapper, systemCode);

        startListener(environment, listener, configuration.getConsumerConfiguration(), consumerClient);

        HealthCheckRegistry healthChecks = environment.healthChecks();

        healthChecks.register("Document Store ping",
                new RemoteServiceDependencyHealthCheck("Document Store",
                        "Links to other FT content will not be resolved during publication, reducing data quality.",
                        "https://sites.google.com/a/ft.com/ft-technology-service-transition/home/run-book-library/documentstoreapi",
                        Client.create(), configuration.getUrlResolverConfiguration().getDocumentStoreConfiguration().getEndpointConfiguration()));

        environment.jersey().register(WordPressArticleTransformerExceptionMapper.class);
        Errors.customise(new WordPressArticleMapperErrorEntityFactory());
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

        EndpointConfiguration contentReadEndpoint = configuration.getContentReadConfiguration().getEndpointConfiguration();
        URI contentReadBaseURI = UriBuilder.fromPath(contentReadEndpoint.getPath())
                .scheme("http")
                .host(contentReadEndpoint.getHost())
                .port(contentReadEndpoint.getPort())
                .build();

        Client contentReadClient = Client.create();
        setClientTimeouts(contentReadClient, contentReadEndpoint.getJerseyClientConfiguration());

        int threadPoolSize = configuration.getThreadPoolSize();
        int maxLinks = threadPoolSize * configuration.getLinksPerThread();
        return (BodyProcessingFieldTransformer) (new BodyProcessingFieldTransformerFactory(
                videoMatcher,
                configuration.getPatterns(),
                blogApiEndpointMetadataManager,
                resolverClient,
                threadPoolSize,
                maxLinks,
                documentStoreClient,
                documentStoreBaseURI,
                contentReadClient,
                contentReadBaseURI
        )).newInstance();
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

    private Client getConsumerClient(Environment environment, ConsumerConfiguration config) {
        JerseyClientConfiguration jerseyConfig = config.getJerseyClientConfiguration();
        jerseyConfig.setGzipEnabled(false);
        jerseyConfig.setGzipEnabledForRequests(false);

        return ResilientClientBuilder.in(environment)
                .using(jerseyConfig)
                .usingDNS()
                .named("consumer-client")
                .build();
    }

    protected void startListener(Environment environment, MessageListener listener, ConsumerConfiguration config, Client consumerClient) {
        final MessageQueueConsumerInitializer messageQueueConsumerInitializer =
                new MessageQueueConsumerInitializer(config.getMessageQueueConsumerConfiguration(),
                        listener, consumerClient);

        HealthCheckRegistry healthchecks = environment.healthChecks();
        healthchecks.register("KafkaProxyConsumer",
                messageQueueConsumerInitializer.buildPassiveConsumerHealthcheck(
                        config.getHealthcheckConfiguration(), environment.metrics()
                ));
        environment.lifecycle().manage(messageQueueConsumerInitializer);
    }

    protected MessageProducer configureMessageProducer(Environment environment, ProducerConfiguration config) {
        JerseyClientConfiguration jerseyConfig = config.getJerseyClientConfiguration();
        jerseyConfig.setGzipEnabled(false);
        jerseyConfig.setGzipEnabledForRequests(false);

        Client producerClient = ResilientClientBuilder.in(environment)
                .using(jerseyConfig)
                .usingDNS()
                .named("producer-client")
                .build();

        final QueueProxyProducer.BuildNeeded queueProxyBuilder = QueueProxyProducer.builder()
                .withJerseyClient(producerClient)
                .withQueueProxyConfiguration(config.getMessageQueueProducerConfiguration());

        final QueueProxyProducer producer = queueProxyBuilder.build();

        environment.healthChecks().register("KafkaProxyProducer",
                new CanConnectToMessageQueueProducerProxyHealthcheck(queueProxyBuilder.buildHealthcheck(),
                        config.getHealthcheckConfiguration(), environment.metrics()));

        return producer;
    }
}
