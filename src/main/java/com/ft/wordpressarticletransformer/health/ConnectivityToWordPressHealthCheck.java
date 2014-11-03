package com.ft.wordpressarticletransformer.health;

import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;

import com.ft.wordpressarticletransformer.configuration.WordPressConnection;
import com.ft.wordpressarticletransformer.response.Data;
import com.ft.wordpressarticletransformer.response.FastFTResponse;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectivityToWordPressHealthCheck extends AdvancedHealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectivityToWordPressHealthCheck.class);

    private static final String STATUS_OK = "ok";

    private final String panicGuideUrl;
	private final List<WordPressConnection> wordPressConnections;
	private final Client client;
	private final SystemId systemId;

    public ConnectivityToWordPressHealthCheck(final String healthCheckName, final Client client, SystemId systemId,
											  String panicGuideUrl, List<WordPressConnection> wordPressConnections) {
        super(healthCheckName);
		this.client = client;
		this.systemId = systemId;
		this.panicGuideUrl = panicGuideUrl;
		this.wordPressConnections = wordPressConnections;
	}

    @Override
    protected AdvancedResult checkAdvanced() throws Exception {

		for (WordPressConnection wordPressConnection: wordPressConnections) {
			ClientResponse response = null;
			try {
				response = client.resource(getWordPressMostRecentUrl(wordPressConnection))
						.accept("application/json").get(ClientResponse.class);

				if (response.getStatus() == 200) {
					FastFTResponse[] output = response.getEntity(FastFTResponse[].class);
					if(output[0] != null){
						String status = output[0].getStatus();
						if (!STATUS_OK.equals(status)) {
							return AdvancedResult.error(this, "status field in response not \"" + STATUS_OK + "\"");
						}
						Data data = output[0].getData();
						if (data != null) {
							Map<String, Object> dataMap = data.getAdditionalProperties();
							if(dataMap.get("id") instanceof Integer){
								Integer id = (Integer)dataMap.get("id");
								if((Integer.valueOf(99999)).equals(id)){
									continue;
								}
							}
						}
					}
					return AdvancedResult.error(this, "Status code 200 was received from Clamo but content id did not match");

				} else {
					String message = String.format("Status code [%d] received when receiving content from WordPress.",
							response.getStatus());
					LOGGER.warn(message);
					return AdvancedResult.error(this, message);
				}
			} catch (Throwable e) {
				LOGGER.warn(getName() + ": " + "Exception during getting most recent content from Clamo", e);
				return AdvancedResult.error(this, e);
			} finally {
				if (response != null) {
					response.close();
				}
			}
		}
		return AdvancedResult.healthy("All is ok");
	}

    @Override
    protected int severity() {
        return 2;
    }

    @Override
    protected String businessImpact() {
        return "Publishes made in WordPress may not be able to be processed.";
    }

    @Override
    protected String technicalSummary() {
        return systemId + " is unable to transform WordPress content.";
    }

    @Override
    protected String panicGuideUrl() {
        return panicGuideUrl;
    }

	private URI getWordPressMostRecentUrl(WordPressConnection wordPressConnection) {
		return UriBuilder.fromPath(wordPressConnection.getPath())
				.scheme("http")
				.host(wordPressConnection.getHostName())
				.port(wordPressConnection.getPort())
				.build();
	}

    // #boring
    private static final String IMPOSSIBLE_MISSING_ENCODING_ERROR_MSG = "JVM Capability missing: UTF-8 encoding";

}