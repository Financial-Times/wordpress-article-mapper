package com.ft.wordpressarticletransformer.resources;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import com.ft.wordpressarticletransformer.response.WordPressPostType;
import com.ft.wordpressarticletransformer.response.WordPressStatus;
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

import java.util.List;
import java.util.UUID;


public class WordPressResilientClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(WordPressResilientClient.class);
	private static final String API_KEY_NAME = "api_key";
	
    private static final String ERROR_NOT_FOUND = "Not found."; // DOES include a dot
    
	private static final String UNSUPPORTED_POST_TYPE =
	        "Not a valid post, type [%s] is not in supported types %s, for content with uuid:[%s]";
	
    private static final Set<String> SUPPORTED_POST_TYPES = WordPressPostType.stringValues();
    
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


	public Post getContent(URI requestUri, UUID uuid, String transactionId) {

        ClientResponse response = null;

	    WebResource webResource = client.resource(requestUri)
                    .queryParam(API_KEY_NAME,wordpressApiKey)
                    .queryParam("cache_buster",transactionId);

        
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
            } catch (UnpublishablePostException | PostNotFoundException e) {
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

            MultivaluedMap<String, String> headers = response.getHeaders();
            List<String> contentTypes = headers.get("Content-Type");
            if (contentTypes == null || !contentTypes.stream().anyMatch(ct -> ct.contains("application/json"))) {
                throw new InvalidContentTypeException(contentTypes, response, requestUri);
            }
            
            wordPressResponse = response.getEntity(WordPressResponse.class);
            String status = wordPressResponse.getStatus();

            if (status == null) {
                throw new InvalidResponseException(response, requestUri);
            }
            
            try {
                switch (WordPressStatus.valueOf(status)) {
                    case ok:
                        if (!isSupportedPostType(wordPressResponse)) {
                            throw new UnpublishablePostException(requestUri, uuid, String.format(UNSUPPORTED_POST_TYPE,
                                    wordPressResponse.getPost().getType(), SUPPORTED_POST_TYPES, uuid));
                        }
                        return wordPressResponse.getPost();
                    
                    case error:
                        throw processWordPressErrorResponse(requestUri, uuid, wordPressResponse);
                        
                }
            }
            catch (IllegalArgumentException e) { /* ignore and throw as below */ }
            
            throw new UnexpectedStatusFieldException(requestUri, status, uuid);
        }
        else if (responseStatusFamily == 4) {
            throw new UnexpectedStatusCodeException(requestUri, responseStatusCode);
        }
        else {
            throw new RequestFailedException(requestUri, responseStatusCode);
        }
    }

    private boolean isSupportedPostType(WordPressResponse wordPressResponse) {
        Post post = wordPressResponse.getPost();
        
        return (post != null) && SUPPORTED_POST_TYPES.contains(post.getType());
    }
    
    private WordPressApiException processWordPressErrorResponse(URI requestUri, UUID uuid, WordPressResponse wordPressResponse) {
        String error = wordPressResponse.getError();
        if (ERROR_NOT_FOUND.equals(error)) {
            return new PostNotFoundException(requestUri, error, uuid);
        }
        
        Post post = wordPressResponse.getPost();
        if ((post != null) && !isSupportedPostType(wordPressResponse)) {
            return new UnpublishablePostException(requestUri, uuid,
                    String.format(UNSUPPORTED_POST_TYPE, post.getType(), SUPPORTED_POST_TYPES, uuid));
        }
        
        // It says it's an error, but we don't understand this kind of error
        return new UnexpectedErrorCodeException(requestUri, error, uuid);
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
            }
            
            try {
                switch (WordPressStatus.valueOf(status)) {
                    case ok:
                        return output;
                    
                    case error:
                        // we expect not to get any error!
                        throw new UnexpectedErrorCodeException(requestUri, output.getError(), output.getAdditionalProperties());
                }
            }
            catch (IllegalArgumentException e) { /* ignore and throw as below */ }
            
            throw new UnexpectedStatusFieldException(requestUri, status, output.getAdditionalProperties());
        }
        else if (responseStatusFamily == 4) {
            throw new UnexpectedStatusCodeException(requestUri, responseStatusCode);
        }
        else {
            throw new RequestFailedException(requestUri, responseStatusCode);
        }
    }

}
