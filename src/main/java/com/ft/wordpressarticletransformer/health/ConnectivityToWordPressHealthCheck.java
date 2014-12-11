package com.ft.wordpressarticletransformer.health;

import java.net.URI;
import java.util.List;
import javax.ws.rs.core.UriBuilder;

import com.ft.wordpressarticletransformer.configuration.WordPressConnection;
import com.ft.wordpressarticletransformer.resources.ErrorCodeNotFoundException;
import com.ft.wordpressarticletransformer.resources.InvalidResponseException;
import com.ft.wordpressarticletransformer.resources.RequestFailedException;
import com.ft.wordpressarticletransformer.resources.UnexpectedStatusCodeException;
import com.ft.wordpressarticletransformer.resources.UnexpectedStatusFieldException;
import com.ft.wordpressarticletransformer.resources.UnknownErrorCodeException;
import com.ft.wordpressarticletransformer.resources.WordPressResilientClient;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.ft.wordpressarticletransformer.response.WordPressMostRecentPostsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectivityToWordPressHealthCheck extends AdvancedHealthCheck {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectivityToWordPressHealthCheck.class);

    private static final String STATUS_OK = "ok";
    private static final String STATUS_ERROR = "error";
    private static final int SUCCESSFUL_RESPONSE_CODE = 200;
	private static final Integer EXPECTED_COUNT = 1;

	private final String panicGuideUrl;
	private final List<WordPressConnection> wordPressConnections;
	private final WordPressResilientClient client;
	private final SystemId systemId;

	public ConnectivityToWordPressHealthCheck(final String healthCheckName, final WordPressResilientClient client, SystemId systemId,
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

            WordPressMostRecentPostsResponse output = client.getRecentPosts(wordPressConnection);

			try {

				if(output != null){
                    String status = output.getStatus();
                    if (!STATUS_OK.equals(status)) {
                        return reportError("status field in response not \"" + STATUS_OK + "\", was " + status);
                    }
                    Integer count = output.getCount();
                    if (!EXPECTED_COUNT.equals(count)) {
                        return reportError("count field in response not \"" + EXPECTED_COUNT + "\", was " + count);
                    }

				} else {
                    return reportError(String.format("WordPress returned no data. Status code [%d]", output.getStatus()));
				}
			} catch(InvalidResponseException e) {
                return reportError("status field in response not \"" + STATUS_OK + "\", was " + e.getResponse());
            } catch(ErrorCodeNotFoundException e) {
                return reportError("error code in response not \"" + STATUS_ERROR + "\", was " + e.getError());
            } catch(UnknownErrorCodeException e) {
                return reportError("error code in response not \"" + STATUS_ERROR + "\", was " + e.getError());
            } catch(UnexpectedStatusFieldException e) {
                return reportError("status field in response not \"" + STATUS_OK + "\", was " + e.getStatus());
            } catch(UnexpectedStatusCodeException e) {
                return reportError("expected response code \"" + SUCCESSFUL_RESPONSE_CODE + "\", received " + e.getResponseStatusCode());
            } catch(RequestFailedException e) {
                return reportError("expected response code \"" + SUCCESSFUL_RESPONSE_CODE + "\", received " + e.getResponseStatusCode());
            } catch (Throwable e) {
				LOGGER.warn(getName() + ": Exception during getting most recent content from WordPress", e);
				return AdvancedResult.error(this, e);
			}
		}
		return AdvancedResult.healthy("All is ok");
	}

    private AdvancedResult reportError(String message) {
        AdvancedResult result = AdvancedResult.error(this, message);
        LOGGER.warn(result.checkOutput());
        return result;
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

}