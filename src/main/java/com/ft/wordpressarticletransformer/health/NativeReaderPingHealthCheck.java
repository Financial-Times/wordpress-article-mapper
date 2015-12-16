package com.ft.wordpressarticletransformer.health;

import com.ft.jerseyhttpwrapper.config.EndpointConfiguration;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class NativeReaderPingHealthCheck extends AdvancedHealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeReaderPingHealthCheck.class);

    private final EndpointConfiguration endpointConfiguration;
    private final Client client;

    public NativeReaderPingHealthCheck(Client client, EndpointConfiguration endpointConfiguration) {
        super("Native Reader is up and running");
        this.endpointConfiguration = endpointConfiguration;
        this.client = client;
    }

    @Override
    protected AdvancedResult checkAdvanced() throws Exception {

        URI pingUri = UriBuilder.fromPath("/__health")
                .scheme("http")
                .host(endpointConfiguration.getHost())
                .port(endpointConfiguration.getPort())
                .build();

        ClientResponse response = null;
        try {
            response = client.resource(pingUri)
                    .header("Host", "nativerw")
                    .get(ClientResponse.class);

            if (response.getStatus() != 200) {
                String message = String.format("Unexpected status : %s", response.getStatus());
                return reportUnhealthy(message);
            }

            return AdvancedResult.healthy();

        } catch (Throwable e) {
            String message = getName() + ": " + "Exception during ping, " + e.getLocalizedMessage();
            return reportUnhealthy(message);
        } finally {
            if (response != null) {
                response.close();
            }
        }

    }

    private AdvancedResult reportUnhealthy(String message) {
        LOGGER.warn(getName() + ": " + message);
        return AdvancedResult.error(this, message);
    }

    @Override
    protected String businessImpact() {
        return "Publishing wordpress content won't work";
    }

    @Override
    protected String panicGuideUrl() {
        return "https://sites.google.com/a/ft.com/technology/systems/dynamic-semantic-publishing/extra-publishing/native-store-reader-writer-run-book";
    }

    @Override
    protected int severity() {
        return 1;
    }

    @Override
    protected String technicalSummary() {
        return "Tests that the /__health endpoint for the Native Reader returns 200 HTTP status response";
    }

}