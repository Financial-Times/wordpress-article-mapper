package com.ft.wordpressarticletransformer.resources;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

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

    private final Client client;
	private int numberOfConnectionAttempts;

	private final Timer requests;

    public WordPressResilientClient(Client client, MetricRegistry appMetrics, int numberOfConnectionAttempts) {
        this.client = client;
		this.numberOfConnectionAttempts = numberOfConnectionAttempts;
		this.requests = appMetrics.timer(MetricRegistry.name(TransformerResource.class, "requestToClamo"));
    }

    public ClientResponse getRecentPosts(WordPressConnection wordPressConnection) {

        URI fastFtContentByIdUri = getWordPressRecentPostsUrl(wordPressConnection);

        WebResource webResource = client.resource(fastFtContentByIdUri);
        
        ClientHandlerException lastClientHandlerException = null;
        
        for (int attemptsCount = 1; attemptsCount <= numberOfConnectionAttempts; attemptsCount++) {
            LOGGER.info("[REQUEST STARTED] attempt={} requestUri={}", attemptsCount, fastFtContentByIdUri);
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
                        String.format("Cannot connect to Clamo for url: [%s]", fastFtContentByIdUri)).exception(cause);
        }
        throw lastClientHandlerException;

        
    }


    private URI getWordPressRecentPostsUrl(WordPressConnection wordPressConnection) {
        return UriBuilder.fromPath(wordPressConnection.getPath())
                .scheme("http")
                .host(wordPressConnection.getHostName())
                .port(wordPressConnection.getPort())
                .queryParam("count", 1).build();
    }


	public ClientResponse getContent(URI requestUri) {

	    WebResource webResource = client.resource(requestUri);
        
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
