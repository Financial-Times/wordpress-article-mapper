package com.ft.fastfttransformer.health;

import com.ft.fastfttransformer.configuration.ClamoConnection;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

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
		// TODO this can be done nicer.
		String eq = "%5B%7B%22arguments%22%3A%20%7B%22outputfields%22%3A%20%7B%22title%22%3A%20true%2C%22content%22%20%3A%20%22text%22%7D%2C%22id%22%3A%20"
				+ Integer.toString(contentId)
				+ "%7D%2C%22action%22%3A%20%22getPost%22%20%7D%5D%0A";

		ClientResponse response = null;
		try {
			response = client.resource(getClamoBaseUrl(contentId)).queryParam("request", eq)
					.accept("application/json").get(ClientResponse.class);

			if (response.getStatus() == 200) {
				return AdvancedResult.healthy("All is ok");
			} else {
				return AdvancedResult.error(this, String.format("Status code [%d] received when receiving content from Clamo.", response.getStatus()));
			}
		} catch (Throwable e) {
			LOGGER.warn(getName() + ": " + "Exception during ping", e);
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
				.path("{uuid}")
				.scheme("http")
				.host(clamoConnection.getHostName())
				.port(clamoConnection.getPort())
				.build(id);
	}

}