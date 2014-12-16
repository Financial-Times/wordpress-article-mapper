package com.ft.wordpressarticletransformer.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.SocketTimeoutException;
import java.net.URI;

import com.ft.wordpressarticletransformer.configuration.WordPressConnection;
import com.ft.wordpressarticletransformer.response.WordPressMostRecentPostsResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.codahale.metrics.MetricRegistry;
import com.ft.wordpressarticletransformer.response.Post;
import com.ft.wordpressarticletransformer.response.WordPressResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WordPressResilientClientTest {

	private ClientHandler handler = mock(ClientHandler.class);
    private Client mockClient = new Client(handler);
    private ClientResponse clientResponse = mock(ClientResponse.class);
    private WordPressResponse mockWordPressResponse = mock(WordPressResponse.class);
    private WordPressMostRecentPostsResponse mockWordPressMostRecentPostsResponse = mock(WordPressMostRecentPostsResponse.class);
    private Post mockPost = mock(Post.class);

    private static final int SUCCESSFUL_RESPONSE_STATUS_CODE = 200;
    private static final int ERROR_RESPONSE_STATUS_CODE = 400;
    private static final int INTERNAL_ERROR_RESPONSE_STATUS_CODE = 500;
    private static final String STATUS_NULL = null;
    private static final String STATUS_OK = "ok";
    private static final String STATUS_ERROR = "error";
    private static final String STATUS_UNKNOWN = "unknown";
    private static final String POST_TYPE_GET = "get";
    private static final String ERROR_NOT_FOUND = "Not found";
    private static final String ERROR_UNKNOWN = "Unknown";

    private MetricRegistry appMetrics = new MetricRegistry();
    private URI requestUri = URI.create("http://uat.ftalphaville.ft.com/2014/10/21/2014692/the-6am-london-cut-277/?json=1");
    private UUID uuid = UUID.fromString("17347305-401d-3285-b8c5-e54ba450beeb");
    private String hostname = "www.example.com";
    private String path = "/dummy";
    private int port = 8080;

    private WordPressResilientClient wordPressResilientClient;
    private WordPressConnection wordPressConnection;

    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Before
    public void setup() {
        wordPressResilientClient = new WordPressResilientClient(mockClient, appMetrics, 3, WP.EXAMPLE_API_KEY);
        when(clientResponse.getHeaders()).thenReturn(new MultivaluedMapImpl());
        wordPressConnection = new WordPressConnection(hostname, path, port);
    }

    @Test
    public void shouldReturnResponseWhenCanConnectToWordpress() {
        when(clientResponse.getEntity(WordPressResponse.class)).thenReturn(mockWordPressResponse);
        when(handler.handle(any(ClientRequest.class)))
            .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_STATUS_CODE);
        when(mockWordPressResponse.getStatus()).thenReturn(STATUS_OK);
        Post post = wordPressResilientClient.getContent(requestUri, uuid);
        assertThat(post, is(equalTo(mockWordPressResponse.getPost())));
    }
    
    @Test
    public void shouldThrowExceptionWhenConsistentlyCannotConnectToWordpress() {
        when(clientResponse.getEntity(WordPressResponse.class)).thenReturn(mockWordPressResponse);
        when(handler.handle(any(ClientRequest.class)))
            .thenThrow(new ClientHandlerException(new SocketTimeoutException()));
        expectedException.expect(CannotConnectToWordPressException.class);
        wordPressResilientClient.getContent(requestUri, uuid);
    }

    @Test
    public void shouldReturnResponseWhenCanConnectToWordpressOnSecondAttemptForGetContent() {
        when(clientResponse.getEntity(WordPressResponse.class)).thenReturn(mockWordPressResponse);
        when(handler.handle(any(ClientRequest.class)))
            .thenThrow(new ClientHandlerException(new SocketTimeoutException()))
            .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_STATUS_CODE);
        when(mockWordPressResponse.getStatus()).thenReturn(STATUS_OK);
        Post post = wordPressResilientClient.getContent(requestUri, uuid);
        assertThat(post, is(equalTo(mockWordPressResponse.getPost())));
        verify(handler, times(2)).handle(any(ClientRequest.class));
    }

    @Test(expected=InvalidResponseException.class)
    public void shouldThrowInvalidResponseExceptionWhenStatusIsNullForGetContent() {
        when(clientResponse.getEntity(WordPressResponse.class)).thenReturn(mockWordPressResponse);
        when(handler.handle(any(ClientRequest.class)))
                .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_STATUS_CODE);
        when(mockWordPressResponse.getStatus()).thenReturn(STATUS_NULL);
        wordPressResilientClient.getContent(requestUri, uuid);
    }

    @Test(expected=UnsupportedPostTypeException.class)
    public void shouldThrowUnsupportedPostTypeExceptionWhenPostTypeIsNotPostForGetContent() {
        when(clientResponse.getEntity(WordPressResponse.class)).thenReturn(mockWordPressResponse);
        when(handler.handle(any(ClientRequest.class)))
                .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_STATUS_CODE);
        when(mockWordPressResponse.getStatus()).thenReturn(STATUS_OK);
        when(mockWordPressResponse.getPost()).thenReturn(mockPost);
        when(mockWordPressResponse.getPost().getType()).thenReturn(POST_TYPE_GET);
        wordPressResilientClient.getContent(requestUri, uuid);
    }

    @Test(expected=PostNotFoundException.class)
    public void shouldThrowPostNotFoundExceptionWhenStatusErrorAndErrorCodeNotFoundForGetContent() {
        when(clientResponse.getEntity(WordPressResponse.class)).thenReturn(mockWordPressResponse);
        when(handler.handle(any(ClientRequest.class)))
                .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_STATUS_CODE);
        when(mockWordPressResponse.getStatus()).thenReturn(STATUS_ERROR);
        when(mockWordPressResponse.getError()).thenReturn(ERROR_NOT_FOUND);
        wordPressResilientClient.getContent(requestUri, uuid);

    }

    @Test(expected=UnexpectedErrorCodeException.class)
    public void shouldThrowUnknownErrorCodeExceptionWhenStatusErrorAndErrorCodeIsUnknownForGetContent() {
        when(clientResponse.getEntity(WordPressResponse.class)).thenReturn(mockWordPressResponse);
        when(handler.handle(any(ClientRequest.class)))
                .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_STATUS_CODE);
        when(mockWordPressResponse.getStatus()).thenReturn(STATUS_ERROR);
        when(mockWordPressResponse.getError()).thenReturn(ERROR_UNKNOWN);
        wordPressResilientClient.getContent(requestUri, uuid);
    }

    @Test(expected=UnexpectedStatusFieldException.class)
    public void shouldThrowUnexpectedStatusFieldExceptionWhenStatusIsNotOkOrErrorForGetContent() {
        when(clientResponse.getEntity(WordPressResponse.class)).thenReturn(mockWordPressResponse);
        when(handler.handle(any(ClientRequest.class)))
                .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_STATUS_CODE);
        when(mockWordPressResponse.getStatus()).thenReturn(STATUS_UNKNOWN);
        wordPressResilientClient.getContent(requestUri, uuid);
    }

    @Test(expected=UnexpectedStatusCodeException.class)
    public void shouldThrowUnexpectedStatusCodeExceptionWhenClientResponseReturns400ForGetContent() {
        when(clientResponse.getEntity(WordPressResponse.class)).thenReturn(mockWordPressResponse);
        when(handler.handle(any(ClientRequest.class)))
                .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(ERROR_RESPONSE_STATUS_CODE);
        wordPressResilientClient.getContent(requestUri, uuid);
    }

    @Test(expected=RequestFailedException.class)
    public void shouldThrowRequestFailedExceptionWhenClientResponseReturns500ForGetContent() {
        when(clientResponse.getEntity(WordPressResponse.class)).thenReturn(mockWordPressResponse);
        when(handler.handle(any(ClientRequest.class)))
                .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(INTERNAL_ERROR_RESPONSE_STATUS_CODE);
        wordPressResilientClient.getContent(requestUri, uuid);
    }

    @Test
    public void shouldReturnResponseWhenCanConnectToWordpressOnSecondAttemptForGetRecentPosts() {
        when(clientResponse.getEntity(WordPressMostRecentPostsResponse.class)).thenReturn(mockWordPressMostRecentPostsResponse);
        when(handler.handle(any(ClientRequest.class)))
                .thenThrow(new ClientHandlerException(new SocketTimeoutException()))
                .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_STATUS_CODE);
        when(mockWordPressMostRecentPostsResponse.getStatus()).thenReturn(STATUS_OK);
        WordPressMostRecentPostsResponse output = wordPressResilientClient.getRecentPosts(wordPressConnection);
        assertThat(output, is(equalTo(mockWordPressMostRecentPostsResponse)));
        verify(handler, times(2)).handle(any(ClientRequest.class));
    }

    @Test(expected=InvalidResponseException.class)
    public void shouldThrowInvalidResponseExceptionWhenStatusIsNullForGetRecentPosts() {
        when(clientResponse.getEntity(WordPressMostRecentPostsResponse.class)).thenReturn(mockWordPressMostRecentPostsResponse);
        when(handler.handle(any(ClientRequest.class)))
                .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_STATUS_CODE);
        when(mockWordPressMostRecentPostsResponse.getStatus()).thenReturn(STATUS_NULL);
        wordPressResilientClient.getRecentPosts(wordPressConnection);
    }

    @Test(expected=UnexpectedErrorCodeException.class)
    public void shouldThrowUnknownErrorCodeExceptionWhenStatusErrorAndErrorCodeIsUnknownForGetRecentPosts() {
        when(clientResponse.getEntity(WordPressMostRecentPostsResponse.class)).thenReturn(mockWordPressMostRecentPostsResponse);
        when(handler.handle(any(ClientRequest.class)))
                .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_STATUS_CODE);
        when(mockWordPressMostRecentPostsResponse.getStatus()).thenReturn(STATUS_ERROR);
        when(mockWordPressMostRecentPostsResponse.getError()).thenReturn(ERROR_UNKNOWN);
        wordPressResilientClient.getRecentPosts(wordPressConnection);
    }

    @Test(expected=UnexpectedStatusFieldException.class)
    public void shouldThrowUnexpectedStatusFieldExceptionWhenStatusIsNotOkOrErrorForGetRecentPosts() {
        when(clientResponse.getEntity(WordPressMostRecentPostsResponse.class)).thenReturn(mockWordPressMostRecentPostsResponse);
        when(handler.handle(any(ClientRequest.class)))
                .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_STATUS_CODE);
        when(mockWordPressMostRecentPostsResponse.getStatus()).thenReturn(STATUS_UNKNOWN);
        wordPressResilientClient.getRecentPosts(wordPressConnection);
    }

    @Test(expected=UnexpectedStatusCodeException.class)
    public void shouldThrowUnexpectedStatusCodeExceptionWhenClientResponseReturns400ForGetRecentPosts() {
        when(clientResponse.getEntity(WordPressMostRecentPostsResponse.class)).thenReturn(mockWordPressMostRecentPostsResponse);
        when(handler.handle(any(ClientRequest.class)))
                .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(ERROR_RESPONSE_STATUS_CODE);
        wordPressResilientClient.getRecentPosts(wordPressConnection);
    }

    @Test(expected=RequestFailedException.class)
    public void shouldThrowRequestFailedExceptionWhenClientResponseReturns500ForGetRecentPosts() {
        when(clientResponse.getEntity(WordPressMostRecentPostsResponse.class)).thenReturn(mockWordPressMostRecentPostsResponse);
        when(handler.handle(any(ClientRequest.class)))
                .thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(INTERNAL_ERROR_RESPONSE_STATUS_CODE);
        wordPressResilientClient.getRecentPosts(wordPressConnection);
    }
}
