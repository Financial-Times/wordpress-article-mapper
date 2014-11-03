package com.ft.wordpressarticletransformer.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.SocketTimeoutException;
import java.net.URI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.codahale.metrics.MetricRegistry;
import com.ft.api.jaxrs.errors.WebApplicationServerException;
import com.ft.wordpressarticletransformer.configuration.WordPressConnection;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class WordPressResilientClientTest {
    
    private ClientHandler handler = mock(ClientHandler.class);
    private Client mockClient = new Client(handler);
    private ClientResponse clientResponse = mock(ClientResponse.class);
    private MetricRegistry appMetrics = new MetricRegistry();
    
    private URI requestUri = URI.create("http://uat.ftalphaville.ft.com/2014/10/21/2014692/the-6am-london-cut-277/?json=1");

    private WordPressResilientClient wordPressResilientClient;
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Before
    public void setup() {
        wordPressResilientClient = new WordPressResilientClient(mockClient, appMetrics, 3);
        when(clientResponse.getHeaders()).thenReturn(new MultivaluedMapImpl());
    }

    @Test
    public void shouldReturnResponseWhenCanConnectToClamo() {
        when(handler.handle(any(ClientRequest.class)))
            .thenReturn(clientResponse);
        ClientResponse response = wordPressResilientClient.getContent(requestUri);
        assertThat("response", response, is(equalTo(clientResponse)));
    }
    
    @Test
    public void shouldThrowExceptionWhenConsistentlyCannotConnectToClamo() {
        when(handler.handle(any(ClientRequest.class)))
            .thenThrow( new ClientHandlerException(new SocketTimeoutException()));
        expectedException.expect(WebApplicationServerException.class);
        wordPressResilientClient.getContent(requestUri);
    }

    @Test
    public void shouldReturnResponseWhenCanConnectToClamoOnSecondAttempt() {
        when(handler.handle(any(ClientRequest.class)))
            .thenThrow( new ClientHandlerException(new SocketTimeoutException()))
            .thenReturn(clientResponse);
        ClientResponse response = wordPressResilientClient.getContent(requestUri);
        assertThat("response", response, is(equalTo(clientResponse)));
    }
}
