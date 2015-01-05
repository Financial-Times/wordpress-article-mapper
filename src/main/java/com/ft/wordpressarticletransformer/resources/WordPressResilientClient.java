package com.ft.wordpressarticletransformer.resources;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.ft.wordpressarticletransformer.response.WPFormat;
import com.ft.wordpressarticletransformer.response.WordPressMostRecentPostsResponse;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.ft.wordpressarticletransformer.configuration.WordPressConnection;
import com.ft.wordpressarticletransformer.response.Post;
import com.ft.wordpressarticletransformer.response.WordPressResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.UUID;

public class WordPressResilientClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(WordPressResilientClient.class);
	public static final String API_KEY_NAME = "api_key";

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
                //Add API Key to URI here so that we don't log it in this class
                response = webResource.queryParam(API_KEY_NAME,wordpressApiKey).accept("application/json").get(ClientResponse.class);
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
            throw new CannotConnectToWordPressException(wordPressRecentPostsUrl, cause);
        }
        throw lastException;
    }

    private URI getWordPressRecentPostsUrl(WordPressConnection wordPressConnection) {
        return UriBuilder.fromPath(wordPressConnection.getPath())
                .scheme("http")
                .host(wordPressConnection.getHostName())
                .port(wordPressConnection.getPort())
                .queryParam("count", 1)
				.build();
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
            } catch (UnsupportedPostTypeException | PostNotFoundException e) {
                LOGGER.info("[REQUEST FAILED] attempt={} exception={}", attemptsCount, e.getMessage());
                // we don't expect a different response if we retry requests that failed like this so short circuit
                throw e;
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
            throw new CannotConnectToWordPressException(requestUri, cause);
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
                throw new InvalidResponseException(response, requestUri);
            }else if (WPFormat.STATUS_OK.equals(status)) {
                if (isMarketsLive(wordPressResponse)) {
                    throw new UnsupportedPostTypeException(requestUri, wordPressResponse.getPost().getType(), uuid, WPFormat.POST_TYPE_POST);
                }
                return wordPressResponse.getPost();
            } else if (WPFormat.STATUS_ERROR.equals(status)) {
                String error = wordPressResponse.getError();
                if (isMarketsLive(wordPressResponse)) {
                    throw new UnsupportedPostTypeException(requestUri, wordPressResponse.getPost().getType(), uuid, WPFormat.POST_TYPE_POST);
                }
                if (WPFormat.ERROR_NOT_FOUND.equals(error)) {
                    throw new PostNotFoundException(requestUri, wordPressResponse.getError(), uuid);
                } else {
                    // It says it's an error, but we don't understand this kind of error
                    throw new UnexpectedErrorCodeException(requestUri, wordPressResponse.getError(), uuid);
                }
            } else {
                throw new UnexpectedStatusFieldException(requestUri, status, uuid);
            }

        } else if (responseStatusFamily == 4) {
            throw new UnexpectedStatusCodeException(requestUri, responseStatusCode);
        } else {
            throw new RequestFailedException(requestUri, responseStatusCode);
        }
    }

    private boolean isMarketsLive(WordPressResponse wordPressResponse) {
        return wordPressResponse.getPost() != null && !wordPressResponse.getPost().getType().equals(WPFormat.POST_TYPE_POST);
    }

    private WordPressMostRecentPostsResponse processListResponse(ClientResponse response, URI requestUri) {

        WordPressMostRecentPostsResponse output;

        int responseStatusCode = response.getStatus();
        int responseStatusFamily = responseStatusCode / 100;

        if (responseStatusFamily == 2) {

            output = response.getEntity(WordPressMostRecentPostsResponse.class);
            if (output == null) {
                throw new InvalidResponseException(response, requestUri);
            }
            String status = output.getStatus();

            if (status == null) {
                throw new InvalidResponseException(response, requestUri);
            } else if (WPFormat.STATUS_OK.equals(status)) {
                return output;
            } else if (WPFormat.STATUS_ERROR.equals(status)) {
                // we expect not to get any error!
                throw new UnexpectedErrorCodeException(requestUri, output.getError(), output.getAdditionalProperties());
            } else {
                throw new UnexpectedStatusFieldException(requestUri, status, output.getAdditionalProperties());
            }

        } else if (responseStatusFamily == 4) {
            throw new UnexpectedStatusCodeException(requestUri, responseStatusCode);
        } else {
            throw new RequestFailedException(requestUri, responseStatusCode);
        }
    }

}
