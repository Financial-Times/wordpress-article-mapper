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

public class RemoteServiceDependencyHealthCheck extends AdvancedHealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteServiceDependencyHealthCheck.class);

    private final EndpointConfiguration endpointConfiguration;
    private final Client client;
    private final String remoteServiceName;
    private final String remoteHost;
    private final String businessImpact;
    private final String panicGuideUrl;
    
    public RemoteServiceDependencyHealthCheck(String remoteServiceName, String remoteServiceHost,
                                              String businessImpact, String panicGuideUrl,
                                              Client client, EndpointConfiguration endpointConfiguration) {
      
        super(String.format("%s is up and running", remoteServiceName));
        this.remoteServiceName = remoteServiceName;
        this.remoteHost = remoteServiceHost;
        this.businessImpact = businessImpact;
        this.panicGuideUrl = panicGuideUrl;
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
                    .header("Host", remoteHost)
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
        return businessImpact;
    }

    @Override
    protected String panicGuideUrl() {
        return panicGuideUrl;
    }

    @Override
    protected int severity() {
        return 1;
    }

    @Override
    protected String technicalSummary() {
        return String.format("Tests that the /__health endpoint for the %s returns 200 HTTP status response", remoteServiceName);
    }
}
