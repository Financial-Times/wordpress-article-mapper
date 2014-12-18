package com.ft.wordpressarticletransformer.health;

import java.util.List;

import com.ft.wordpressarticletransformer.configuration.WordPressConnection;
import com.ft.wordpressarticletransformer.resources.WordPressApiException;
import com.ft.wordpressarticletransformer.resources.WordPressResilientClient;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.ft.wordpressarticletransformer.response.WPFormat;
import com.ft.wordpressarticletransformer.response.WordPressMostRecentPostsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectivityToWordPressHealthCheck extends AdvancedHealthCheck {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectivityToWordPressHealthCheck.class);

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

            try {

                WordPressMostRecentPostsResponse output = client.getRecentPosts(wordPressConnection);

				if(output != null){
                    String status = output.getStatus();
                    if (!WPFormat.STATUS_OK.equals(status)) {
                        return reportError("status field in response not \"" + WPFormat.STATUS_OK + "\", was " + status);
                    }
                    Integer count = output.getCount();
                    if (!EXPECTED_COUNT.equals(count)) {
                        return reportError("count field in response not \"" + EXPECTED_COUNT + "\", was " + count);
                    }

				} else {
                    return reportError(String.format("WordPress returned no data."));
				}
			} catch(WordPressApiException e) {
                return reportError(e.getMessage());
            } catch (Throwable e) {
				LOGGER.warn(getName() + ": Exception during getting most recent content from WordPress", e);
				return AdvancedResult.error(this, e);
			}
		}
        // It may be helpful to add more info to this output, but in fact it is never displayed due to the health check formatter.
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

}