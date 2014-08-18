package com.ft.fastfttransformer.health;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import javax.ws.rs.core.UriBuilder;

import com.ft.api.jaxrs.errors.ServerError;
import com.ft.fastfttransformer.configuration.ClamoConnection;
import com.ft.fastfttransformer.resources.TransformerResource;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectivityToClamoHealthCheck extends AdvancedHealthCheck {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectivityToClamoHealthCheck.class);

	private final String panicGuideUrl;
	private final ClamoConnection clamoConnection;
	private int contentId;
	private final Client client;
	private final SystemId systemId;

    public ConnectivityToClamoHealthCheck(final String healthCheckName, final Client client, SystemId systemId, String panicGuideUrl, ClamoConnection clamoConnection,
										  int contentId) {
        super(healthCheckName);
		this.client = client;
		this.systemId = systemId;
		this.panicGuideUrl = panicGuideUrl;
		this.clamoConnection = clamoConnection;
		this.contentId = contentId;
	}

    @Override
    protected AdvancedResult checkAdvanced() throws Exception {

        String eq = null;
        try {
            String queryStringValue = TransformerResource.CLAMO_QUERY_JSON_STRING.replace("<postId>", Integer.toString(contentId));
            eq = URLEncoder.encode(queryStringValue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // should never happen, UTF-8 is part of the Java spec
            throw ServerError.status(503).error("JVM Capability missing: UTF-8 encoding").exception();
        }

		ClientResponse response = null;
		try {
			response = client.resource(getClamoBaseUrl(contentId)).queryParam("request", eq)
					.accept("application/json").get(ClientResponse.class);

			if (response.getStatus() == 200) {
				return AdvancedResult.healthy("All is ok");
			} else {
                String message = String.format("Status code [%d] received when receiving content from Clamo.", response.getStatus());
                LOGGER.warn(message);
				return AdvancedResult.error(this, message);
			}
		} catch (Throwable e) {
			LOGGER.warn(getName() + ": " + "Exception during getting sample content from Clamo", e);
			return AdvancedResult.error(this, e);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

    @Override
    protected int severity() {
        return 2;
    }

    @Override
    protected String businessImpact() {
        return "Publishes made in FastFT may not be able to be processed.";
    }

    @Override
    protected String technicalSummary() {
        return systemId + " is unable to transform FastFT content.";
    }

    @Override
    protected String panicGuideUrl() {
        return panicGuideUrl;
    }

	private URI getClamoBaseUrl(int id) {
		return UriBuilder.fromPath(clamoConnection.getPath())
				.scheme("http")
				.host(clamoConnection.getHostName())
				.port(clamoConnection.getPort())
				.build(id);
	}

}