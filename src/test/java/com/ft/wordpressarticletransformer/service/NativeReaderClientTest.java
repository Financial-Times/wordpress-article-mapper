package com.ft.wordpressarticletransformer.service;

import com.ft.api.jaxrs.client.exceptions.ApiNetworkingException;
import com.ft.api.jaxrs.client.exceptions.RemoteApiException;
import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.jerseyhttpwrapper.config.EndpointConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NativeReaderClientTest {

    private static final String UUID = "bd541104-105b-4d00-8304-a87f39460c0a";
    private static final String TRANSACTION_ID = "bd541104-105b-4d00-8304-a87f39460c0a";

    private NativeReaderClient nativeReaderClient;
    private URI uri;

    @Mock
    private Client jerseyClient;
    @Mock
    private EndpointConfiguration endpointConfiguration;
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        when(endpointConfiguration.getHost()).thenReturn("wordpress");
        when(endpointConfiguration.getPort()).thenReturn(8080);
        when(endpointConfiguration.getPath()).thenReturn("/wordpress/{uuid}");
        nativeReaderClient = new NativeReaderClient(jerseyClient, endpointConfiguration);
        uri = new URI("http://wordpress:8080/wordpress/" + UUID);
    }

    @Test
    public void testHandlesNonOkStatusWithRemoteApiExceptionIfAbleToExtractEntityError() throws Exception {
        final ClientResponse clientResponse = mock(ClientResponse.class);
        final ErrorEntity errorEntity = new ErrorEntity("Not found error.");
        when(clientResponse.getEntity(ErrorEntity.class)).thenReturn(errorEntity);
        final int notFound = ClientResponse.Status.NOT_FOUND.getStatusCode();
        when(clientResponse.getStatus()).thenReturn(notFound);
        exception.expect(RemoteApiException.class);
        exception.expect(equalsRemoteApiExceptionStatus(notFound));

        nativeReaderClient.handleNonOkStatus(clientResponse, uri);
    }

    @Test
    public void testHandlesNonOkStatusWithRemoteApiExceptionIfUnableToExtractEntityError() throws Exception {
        final ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.getEntity(ErrorEntity.class)).thenThrow(new ClientHandlerException("some problem"));
        final int notFound = ClientResponse.Status.NOT_FOUND.getStatusCode();
        when(clientResponse.getStatus()).thenReturn(notFound);
        exception.expect(RemoteApiException.class);
        exception.expect(equalsRemoteApiExceptionStatus(notFound));

        nativeReaderClient.handleNonOkStatus(clientResponse, uri);
    }

    @Test
    public void testHttpCallThorwsApiNetworkingExceptionIfClientHandlerFailsWithIOEx() throws Exception {
        WebResource webResource = mock(WebResource.class);
        when(jerseyClient.resource(any(URI.class))).thenReturn(webResource);
        WebResource.Builder builder1 = mock(WebResource.Builder.class);
        when(webResource.accept(MediaType.APPLICATION_JSON_TYPE)).thenReturn(builder1);
        WebResource.Builder builder2 = mock(WebResource.Builder.class);
        when(builder1.header(TRANSACTION_ID_HEADER, TRANSACTION_ID)).thenReturn(builder2);
        when(builder2.header(eq("Host"), any(String.class))).thenReturn(builder2);
        when(builder2.get(ClientResponse.class))
                .thenThrow(new ClientHandlerException(new IOException("client handler errored")));
        exception.expect(ApiNetworkingException.class);

        nativeReaderClient.httpCall(uri, TRANSACTION_ID);
    }

    @Test
    public void testHttpCallForwardsClientHandlerExceptionIfFailsWithNotAnIOEx() throws Exception {
        WebResource webResource = mock(WebResource.class);
        when(jerseyClient.resource(any(URI.class))).thenReturn(webResource);
        WebResource.Builder builder1 = mock(WebResource.Builder.class);
        when(webResource.accept(MediaType.APPLICATION_JSON_TYPE)).thenReturn(builder1);
        WebResource.Builder builder2 = mock(WebResource.Builder.class);
        when(builder1.header(TRANSACTION_ID_HEADER, TRANSACTION_ID)).thenReturn(builder2);
        when(builder2.header(eq("Host"), any(String.class))).thenReturn(builder2);
        when(builder2.get(ClientResponse.class)).thenThrow(new ClientHandlerException("client handler errored"));
        exception.expect(ClientHandlerException.class);

        nativeReaderClient.httpCall(uri, TRANSACTION_ID);
    }

    @Test
    public void testFindFileByUuidHappyCase() throws Exception {
        WebResource webResource = mock(WebResource.class);
        when(jerseyClient.resource(uri)).thenReturn(webResource);
        WebResource.Builder builder1 = mock(WebResource.Builder.class);
        when(webResource.accept(MediaType.APPLICATION_JSON_TYPE)).thenReturn(builder1);
        WebResource.Builder builder2 = mock(WebResource.Builder.class);
        when(builder1.header(TRANSACTION_ID_HEADER, TRANSACTION_ID)).thenReturn(builder2);
        when(builder2.header(eq("Host"), any(String.class))).thenReturn(builder2);
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(builder2.get(ClientResponse.class)).thenReturn(clientResponse);
        final Map<String, Object> expectedEomFile = new HashMap<>();
        expectedEomFile.put("apiUrl", "actualApiUrl");
        expectedEomFile.put("post", Collections.singletonMap("uuid", UUID));
        when(clientResponse.getStatus()).thenReturn(ClientResponse.Status.OK.getStatusCode());
        when(clientResponse.getEntity(Map.class)).thenReturn(expectedEomFile);

        final Map<String, Object> wordpressContent = nativeReaderClient.getWordpressContent(UUID, TRANSACTION_ID);

        assertFalse(wordpressContent.isEmpty());
        assertThat(wordpressContent, hasEntry("apiUrl", "actualApiUrl"));
        assertThat(wordpressContent, hasEntry("post", Collections.singletonMap("uuid", UUID)));
    }

    private Matcher<RemoteApiException> equalsRemoteApiExceptionStatus(final int status) {
        return new BaseMatcher<RemoteApiException>() {
            @Override
            public boolean matches(final Object exception) {
                return ((RemoteApiException) exception).getStatus() == status;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Exception's status should match given HTTP status code " + status);
            }
        };
    }
}
