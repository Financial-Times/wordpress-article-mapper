package com.ft.wordpressarticletransformer;

import java.io.File;
import java.io.FileReader;
import java.util.EnumSet;
import java.util.Properties;

import javax.servlet.DispatcherType;

import com.ft.api.jaxrs.errors.Errors;
import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.api.util.buildinfo.VersionResource;
import com.ft.api.util.transactionid.TransactionIdFilter;
import com.ft.bodyprocessing.richcontent.VideoMatcher;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;
import com.ft.wordpressarticletransformer.configuration.WordPressArticleTransformerConfiguration;
import com.ft.wordpressarticletransformer.health.ConnectivityToWordPressHealthCheck;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerExceptionMapper;
import com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerResource;
import com.ft.wordpressarticletransformer.resources.WordPressResilientClient;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformer;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformerFactory;
import com.sun.jersey.api.client.Client;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration()).build("Health check connection to WordPress");

        environment.jersey().register(new BuildInfoResource());
		environment.jersey().register(new VersionResource());

        VideoMatcher videoMatcher = new VideoMatcher(configuration.getVideoSiteConfiguration());

        Properties credentials = new Properties();
        credentials.load( new FileReader(new File(configuration.getCredentialsPath())));
		
		WordPressResilientClient wordPressResilientClient = new WordPressResilientClient(client, environment.metrics(),
				configuration.getNumberOfConnectionAttempts(), credentials.getProperty("wordpress.contentApi.key"));
		

        environment.jersey().register(new WordPressArticleTransformerResource(getBodyProcessingFieldTransformer(videoMatcher),
				wordPressResilientClient, new BrandSystemResolver(configuration.getHostToBrands())));

		String healthCheckName = "Connectivity to WordPress";
		environment.healthChecks().register(healthCheckName,
				new ConnectivityToWordPressHealthCheck(healthCheckName,
						wordPressResilientClient,
						"https://sites.google.com/a/ft.com/dynamic-publishing-team/", // TODO proper link
						configuration.getWordPressConnections()
				));

        environment.jersey().register(WordPressArticleTransformerExceptionMapper.class);
        Errors.customise(new WordPressArticleTransformerErrorEntityFactory());
		environment.servlets().addFilter("Transaction ID Filter",
				new TransactionIdFilter()).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/content/*");
		
    }

    private BodyProcessingFieldTransformer getBodyProcessingFieldTransformer(VideoMatcher videoMatcher) {
        return (BodyProcessingFieldTransformer) (new BodyProcessingFieldTransformerFactory(videoMatcher)).newInstance();
    }

}
