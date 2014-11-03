package com.ft.wordpressarticletransformer;

import java.util.EnumSet;
import javax.servlet.DispatcherType;

import com.ft.api.jaxrs.errors.RuntimeExceptionMapper;
import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.api.util.buildinfo.VersionResource;
import com.ft.api.util.transactionid.TransactionIdFilter;
import com.ft.wordpressarticletransformer.configuration.WordPressArticleTransformerConfiguration;
import com.ft.wordpressarticletransformer.health.ConnectivityToWordPressHealthCheck;
import com.ft.wordpressarticletransformer.resources.TransformerResource;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformer;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformerFactory;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;
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
		Client client = new JerseyClientBuilder(environment).using(configuration.getWordPressConnections().get(0).getJerseyClientConfiguration()).build("Health check connection to Clamo");

        environment.jersey().register(new BuildInfoResource());
		environment.jersey().register(new VersionResource());
        environment.jersey().register(new TransformerResource(client, getBodyProcessingFieldTransformer(),
				configuration.getFastFtBrand()));

		String healthCheckName = "Connectivity to Clamo";
		environment.healthChecks().register(healthCheckName,
				new ConnectivityToWordPressHealthCheck(healthCheckName,
						client,
						SystemId.systemIdFromCode("wordpress"), // TODO proper name
						"https://sites.google.com/a/ft.com/dynamic-publishing-team/", // TODO proper link
						configuration.getWordPressConnections()
				));

		environment.jersey().register(new RuntimeExceptionMapper());

		environment.servlets().addFilter("Transaction ID Filter", new TransactionIdFilter()).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/content/*");
    }

    private BodyProcessingFieldTransformer getBodyProcessingFieldTransformer() {
        return (BodyProcessingFieldTransformer) (new BodyProcessingFieldTransformerFactory()).newInstance();
    }

}
