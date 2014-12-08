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
import com.ft.wordpressarticletransformer.response.Post;
import com.ft.wordpressarticletransformer.response.WordPressResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.UUID;

public class WordPressResilientClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(WordPressResilientClient.class);
	public static final String API_KEY_NAME = "api_key";

    private static final String STATUS_ERROR = "error";
    private static final String ERROR_NOT_FOUND = "Not found.";

	private final Client client;
	private int numberOfConnectionAttempts;
	private String wordpressApiKey;
    private WordPressResponse wordPressResponse = null;
    private ClientResponse response = null;

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
        return UriBuilder.fromPath(wordPressConnection.getPath())
                .scheme("http")
                .host(wordPressConnection.getHostName())
                .port(wordPressConnection.getPort())
                .queryParam("count", 1)
				.queryParam(API_KEY_NAME,wordpressApiKey).build();
    }


	public Post getContent(URI requestUri, UUID uuid) {

	    WebResource webResource = client.resource(requestUri).queryParam(API_KEY_NAME,wordpressApiKey);
        
        RuntimeException lastException = null;
        
        for (int attemptsCount = 1; attemptsCount <= numberOfConnectionAttempts; attemptsCount++) {
            LOGGER.info("[REQUEST STARTED] attempt={} requestUri={}", attemptsCount, requestUri);
            Timer.Context requestsTimer = requests.time();
            long startTime = System.currentTimeMillis();

            Post post;
            
            try {
                response = webResource.accept("application/json").get(ClientResponse.class);
                post = processResponse(response);
                return post;
            } catch (RuntimeException e) {
                lastException = e;
                LOGGER.warn("[REQUEST FAILED] attempt={} exception={}", attemptsCount, e.getMessage());
            } finally {
                if (response != null) {
                    response.close();
                }
                long endTime = System.currentTimeMillis();
                long timeTakenMillis = (endTime - startTime);
                requestsTimer.stop();
                LOGGER.info("[REQUEST FINISHED] attempt={} time_ms={}", attemptsCount, timeTakenMillis);
            }
        }
        
        Throwable cause = lastException.getCause();
        if(cause instanceof IOException) {
            throw ServerError.status(503).context(webResource).error(
                        String.format("Cannot connect to WordPress for url: [%s]", requestUri)).exception(cause);
        }
        throw lastException;

	}

    private Post processResponse(ClientResponse response) {
        int responseStatusCode = response.getStatus();
        int responseStatusFamily = responseStatusCode / 100;

        if (responseStatusFamily == 2) {

            try {
                wordPressResponse = response.getEntity(WordPressResponse.class);
            } catch (RuntimeException e) {
                throw new InvalidResponseException(response);
            }
            if (wordPressResponse.getStatus() == null) {
                throw new InvalidResponseException(response);
            }
            if (wordPressResponse.getPost() != null && !wordPressResponse.getPost().getType().equals("post")) { // markets live
                throw new InvalidPostException(wordPressResponse);
            }
            if (STATUS_ERROR.equals(wordPressResponse.getStatus())) {
                String error = wordPressResponse.getError();
                if (ERROR_NOT_FOUND.equals(error)) {
                    throw new ContentNotFoundException();
                } else {
                    // It says it's an error, but we don't understand this kind of error
                    throw new UnexpectedStatusException(responseStatusCode, response);
                }
            }
            return wordPressResponse.getPost();
        } else if (responseStatusFamily == 4) {
            throw new PostNotFoundException();
        } else {
            throw new RequestFailedException();
        }
    }



}
