package com.ft.wordpressarticletransformer.resources;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.wordpressarticletransformer.configuration.WordPressConnection;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class WordPressResilientClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(WordPressResilientClient.class);
	public static final String API_KEY_NAME = "api_key";

	private final Client client;
	private int numberOfConnectionAttempts;
	private String wordpressApiKey;

	private final Timer requests;

    public WordPressResilientClient(Client client, MetricRegistry appMetrics, int numberOfConnectionAttempts, String wordpressApiKey) {

        Preconditions.checkArgument(!Strings.isNullOrEmpty(wordpressApiKey),"No WordPress API key provided");

        this.client = client;
		this.numberOfConnectionAttempts = numberOfConnectionAttempts;
		this.wordpressApiKey = wordpressApiKey;
		this.requests = appMetrics.timer(MetricRegistry.name(WordPressArticleTransformerResource.class, "requestToWordPress"));
    }

    public ClientResponse getRecentPosts(WordPressConnection wordPressConnection) {

        URI wordPressRecentPostsUrl = getWordPressRecentPostsUrl(wordPressConnection);

        WebResource webResource = client.resource(wordPressRecentPostsUrl);
        
        ClientHandlerException lastClientHandlerException = null;
        
        for (int attemptsCount = 1; attemptsCount <= numberOfConnectionAttempts; attemptsCount++) {
            LOGGER.info("[REQUEST STARTED] attempt={} requestUri={}", attemptsCount, wordPressRecentPostsUrl);
            Timer.Context requestsTimer = requests.time();
            long startTime = System.currentTimeMillis();
            

            ClientResponse response;
            
            try {
                response = webResource.accept("application/json").get(ClientResponse.class);
                return response; 
            } catch (ClientHandlerException che) {
                lastClientHandlerException = che; 
                LOGGER.warn("[REQUEST FAILED] attempt={} exception={}", attemptsCount, che.getMessage());
            } finally {
                long endTime = System.currentTimeMillis();
                long timeTakenMillis = (endTime - startTime);
                requestsTimer.stop();
                LOGGER.info("[REQUEST FINISHED] attempt={} time_ms={}", attemptsCount, timeTakenMillis);
            }
        }

        Throwable cause = lastClientHandlerException.getCause();
        if(cause instanceof IOException) {
            throw ServerError.status(503).context(webResource).error(
                        String.format("Cannot connect to WordPress for url: [%s]", wordPressRecentPostsUrl)).exception(cause);
        }
        throw lastClientHandlerException;

        
    }

    private URI getWordPressRecentPostsUrl(WordPressConnection wordPressConnection) {
        return templateUrl(wordPressConnection)
				.queryParam(API_KEY_NAME, wordpressApiKey).build();
    }

    public UriBuilder templateUrl(WordPressConnection wordPressConnection) {
        return UriBuilder.fromPath(wordPressConnection.getPath())
                .scheme("http")
                .host(wordPressConnection.getHostName())
                .port(wordPressConnection.getPort())
                .queryParam("count", 1);
    }

    public ClientResponse getContent(URI requestUri) {

	    WebResource webResource = client.resource(requestUri).queryParam(API_KEY_NAME,wordpressApiKey);
        
        ClientHandlerException lastClientHandlerException = null;
        
        for (int attemptsCount = 1; attemptsCount <= numberOfConnectionAttempts; attemptsCount++) {
            LOGGER.info("[REQUEST STARTED] attempt={} requestUri={}", attemptsCount, requestUri);
            Timer.Context requestsTimer = requests.time();
            long startTime = System.currentTimeMillis();
            
            ClientResponse response = null;
            
            try {
                response = webResource.accept("application/json").get(ClientResponse.class);
                return response; 
            } catch (ClientHandlerException che) {
                lastClientHandlerException = che; 
                LOGGER.warn("[REQUEST FAILED] attempt={} exception={}", attemptsCount, che.getMessage());
            } finally {
                long endTime = System.currentTimeMillis();
                long timeTakenMillis = (endTime - startTime);
                requestsTimer.stop();
                LOGGER.info("[REQUEST FINISHED] attempt={} time_ms={}", attemptsCount, timeTakenMillis);
            }
        }
        
        Throwable cause = lastClientHandlerException.getCause();
        if(cause instanceof IOException) {
            throw ServerError.status(503).context(webResource).error(
                        String.format("Cannot connect to WordPress for url: [%s]", requestUri)).exception(cause);
        }
        throw lastClientHandlerException;

	}
}
