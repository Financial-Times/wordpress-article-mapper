package com.ft.fastfttransformer.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.dropwizard.client.JerseyClientConfiguration;

import java.net.SocketTimeoutException;

import javax.ws.rs.core.HttpHeaders;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.codahale.metrics.MetricRegistry;
import com.ft.api.jaxrs.errors.WebApplicationServerException;
import com.ft.content.model.Brand;
import com.ft.fastfttransformer.configuration.ClamoConnection;
import com.ft.fastfttransformer.response.FastFTResponse;
import com.ft.fastfttransformer.transformer.BodyProcessingFieldTransformer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;


/**
 * Use this test for low level testing, use TransformerResourceIT for everything else.
 * @author sarah.wells
 *
 */
public class TransformerResourceTest {
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private TransformerResource transformerResource;
    private ClientHandler handler = mock(ClientHandler.class);
    private Client mockClient = new Client(handler);
    private ClientResponse clientResponse = mock(ClientResponse.class);
    private ClamoConnection clamoConnection;
    private BodyProcessingFieldTransformer mockBodyProcessingFieldTransformer = mock(BodyProcessingFieldTransformer.class);
    private Brand fastFtBrand = new Brand("http://uri.for.fast.ft/brand");
    private MetricRegistry appMetrics = new MetricRegistry();
    private HttpHeaders mockHttpHeaders = mock(HttpHeaders.class);
    private FastFTResponse[] fastFTResponses = new FastFTResponse[1];
    private FastFTResponse fastFTResponse;
    
    @Before
    public void setup() {
        clamoConnection = new ClamoConnection("http://localhost", "8080", 0, new JerseyClientConfiguration(), 3);
        transformerResource = new TransformerResource(mockClient, clamoConnection, mockBodyProcessingFieldTransformer, fastFtBrand, appMetrics);
        when(clientResponse.getStatus()).thenReturn(200);
        fastFTResponse = new FastFTResponse();
        fastFTResponse.setStatus("200");
        fastFTResponses[0] = fastFTResponse;
        when(clientResponse.getEntity(FastFTResponse[].class)).thenReturn(fastFTResponses);
        
    }
    
    @Test
    public void shouldReturn503WhenConsistentlyCannotConnectToClamo() {
        when(handler.handle(any(ClientRequest.class))).thenThrow( new ClientHandlerException(new SocketTimeoutException()));
        expectedException.expect(WebApplicationServerException.class);
        transformerResource.getByPostId(234567, mockHttpHeaders );
             
    }
    
    @Test
    public void shouldReturn200WhenCanConnectToClamoOnSubsequentAttempt() {
        when(handler.handle(any(ClientRequest.class))).thenThrow( new ClientHandlerException(new SocketTimeoutException()))
                .thenReturn(clientResponse);
        expectedException.expect(WebApplicationServerException.class);
        transformerResource.getByPostId(234567, mockHttpHeaders );
             
    }
    
}
