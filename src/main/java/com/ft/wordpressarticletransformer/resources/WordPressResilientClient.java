package com.ft.wordpressarticletransformer.resources;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.ft.wordpressarticletransformer.response.WordPressMostRecentPostsResponse;
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

    private static final String STATUS_OK = "ok";
    private static final String STATUS_ERROR = "error";
    private static final String ERROR_NOT_FOUND = "Not found";
    private static final String POST_TYPE_POST = "post";

	private final Client client;
	private final int numberOfConnectionAttempts;
	private final String wordpressApiKey;

	private final Timer requests;

    public WordPressResilientClient(Client client, MetricRegistry appMetrics, int numberOfConnectionAttempts, String wordpressApiKey) {

        Preconditions.checkArgument(!Strings.isNullOrEmpty(wordpressApiKey),"No WordPress API key provided");

        this.client = client;
		this.numberOfConnectionAttempts = numberOfConnectionAttempts;
		this.wordpressApiKey = wordpressApiKey;
		this.requests = appMetrics.timer(MetricRegistry.name(WordPressArticleTransformerResource.class, "requestToWordPress"));
    }

    public WordPressMostRecentPostsResponse getRecentPosts(WordPressConnection wordPressConnection) {

        ClientResponse response = null;

        URI wordPressRecentPostsUrl = getWordPressRecentPostsUrl(wordPressConnection);

        WebResource webResource = client.resource(wordPressRecentPostsUrl);

        RuntimeException lastException = null;

        for (int attemptsCount = 1; attemptsCount <= numberOfConnectionAttempts; attemptsCount++) {
            LOGGER.info("[REQUEST STARTED] attempt={} requestUri={}", attemptsCount, wordPressRecentPostsUrl);
            Timer.Context requestsTimer = requests.time();
            long startTime = System.currentTimeMillis();
            WordPressMostRecentPostsResponse wordPressMostRecentPostsResponse;

            try {
                response = webResource.accept("application/json").get(ClientResponse.class);
                wordPressMostRecentPostsResponse = processListResponse(response, wordPressRecentPostsUrl);
                return wordPressMostRecentPostsResponse;
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
                    String.format("Cannot connect to WordPress for url: [%s]", wordPressRecentPostsUrl)).exception(cause);
        }
        throw lastException;
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

        ClientResponse response = null;

	    WebResource webResource = client.resource(requestUri).queryParam(API_KEY_NAME,wordpressApiKey);
        
        RuntimeException lastException = null;
        
        for (int attemptsCount = 1; attemptsCount <= numberOfConnectionAttempts; attemptsCount++) {
            LOGGER.info("[REQUEST STARTED] attempt={} requestUri={}", attemptsCount, requestUri);
            Timer.Context requestsTimer = requests.time();
            long startTime = System.currentTimeMillis();
            Post post;
            
            try {
                response = webResource.accept("application/json").get(ClientResponse.class);
                post = processPostResponse(response, uuid, requestUri);
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

    private Post processPostResponse(ClientResponse response, UUID uuid, URI requestUri) {
        WordPressResponse wordPressResponse;

        int responseStatusCode = response.getStatus();
        int responseStatusFamily = responseStatusCode / 100;

        if (responseStatusFamily == 2) {

            wordPressResponse = response.getEntity(WordPressResponse.class);
            String status = wordPressResponse.getStatus();

            if (status == null) {
                throw new InvalidResponseException(response);
            }else if (STATUS_OK.equals(status)) {
                if (wordPressResponse.getPost() != null && !wordPressResponse.getPost().getType().equals(POST_TYPE_POST)) { // markets live
                    throw new UnsupportedPostTypeException(wordPressResponse.getPost().getType(), uuid, POST_TYPE_POST);
                }
                return wordPressResponse.getPost();
            } else if (STATUS_ERROR.equals(status)) {
                String error = wordPressResponse.getError();
                if (wordPressResponse.getPost() != null && !wordPressResponse.getPost().getType().equals(POST_TYPE_POST)) { // markets live
                    throw new UnsupportedPostTypeException(wordPressResponse.getPost().getType(), uuid, POST_TYPE_POST);
                }
                if (ERROR_NOT_FOUND.equals(error)) {
                    throw new ErrorCodeNotFoundException(wordPressResponse.getError(), uuid);
                } else {
                    // It says it's an error, but we don't understand this kind of error
                    throw new UnknownStatusErrorCodeException(wordPressResponse.getError(), uuid);
                }
            } else {
                throw new UnexpectedStatusFieldException(status, uuid);
            }

        } else if (responseStatusFamily == 4) {
            throw new UnexpectedStatusCodeException(requestUri, responseStatusCode);
        } else {
            throw new RequestFailedException(requestUri, responseStatusCode);
        }
    }

    private WordPressMostRecentPostsResponse processListResponse(ClientResponse response, URI requestUri) {

        WordPressMostRecentPostsResponse output;

        int responseStatusCode = response.getStatus();
        int responseStatusFamily = responseStatusCode / 100;

        if (responseStatusFamily == 2) {

            output = response.getEntity(WordPressMostRecentPostsResponse.class);
            String status = output.getStatus();

            if (status == null) {
                throw new InvalidResponseException(response);
            }else if (STATUS_OK.equals(status)) {
                return output;
            } else if (STATUS_ERROR.equals(status)) {
                String error = output.getError();
                if (ERROR_NOT_FOUND.equals(error)) {
                    throw new ErrorCodeNotFoundException(output.getError());
                } else {
                    // It says it's an error, but we don't understand this kind of error
                    throw new UnknownStatusErrorCodeException(output.getError());
                }
            } else {
                throw new UnexpectedStatusFieldException(status);
            }

        } else if (responseStatusFamily == 4) {
            throw new UnexpectedStatusCodeException(requestUri, responseStatusCode);
        } else {
            throw new RequestFailedException(requestUri, responseStatusCode);
        }
    }

}
