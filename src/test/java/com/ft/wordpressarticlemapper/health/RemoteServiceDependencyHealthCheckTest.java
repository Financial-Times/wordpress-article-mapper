package com.ft.wordpressarticlemapper.health;

import com.ft.jerseyhttpwrapper.config.EndpointConfiguration;
import com.ft.platform.dropwizard.AdvancedResult;
import com.google.common.base.Optional;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.dropwizard.client.JerseyClientConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteServiceDependencyHealthCheckTest {

    private RemoteServiceDependencyHealthCheck healthCheck;

    @Mock
    private EndpointConfiguration mockEndpointConfiguration;
    @Mock
    private Client mockClient;
    @Mock
    private WebResource mockResource;
    @Mock
    private ClientResponse mockClientResponse;

    @Before
    public void setUp() throws Exception {
        when(mockEndpointConfiguration.getHost()).thenReturn("localhost");
        when(mockEndpointConfiguration.getPort()).thenReturn(9080);
        when(mockEndpointConfiguration.getShortName()).thenReturn(Optional.<String>absent());
        when(mockEndpointConfiguration.getJerseyClientConfiguration()).thenReturn(new JerseyClientConfiguration());

        when(mockClient.resource(any(URI.class))).thenReturn(mockResource);
        when(mockResource.get(ClientResponse.class)).thenReturn(mockClientResponse);

        healthCheck = new RemoteServiceDependencyHealthCheck("name", "businessImpact", "panicGuideUrl", mockClient, mockEndpointConfiguration);
    }

    @Test
    public void testWhenNativeReaderPingIsUpHealthCheckShouldPass() throws Exception {
        when(mockClientResponse.getStatus()).thenReturn(200);

        AdvancedResult expectedHealthCheckResult = AdvancedResult.healthy();
        AdvancedResult actualHealthCheckResult = healthCheck.checkAdvanced();

        assertThat(actualHealthCheckResult.status(), is(equalTo(expectedHealthCheckResult.status())));
    }

    @Test
    public void testWhenNativeReaderPingReturnsUnexpectedStatusHealthCheckShouldFail() throws Exception {
        when(mockClientResponse.getStatus()).thenReturn(503);

        AdvancedResult expectedHealthCheckResult = AdvancedResult.error(healthCheck, "Unexpected status : 503");
        AdvancedResult actualHealthCheckResult = healthCheck.checkAdvanced();

        assertThat(actualHealthCheckResult.status(), is(equalTo(expectedHealthCheckResult.status())));
    }

    @Test
    public void testWhenNativeReaderPingIsDownHealthCheckShouldFail() throws Exception {
        when(mockResource.get(ClientResponse.class)).thenThrow(new ClientHandlerException("timeout"));

        AdvancedResult expectedHealthCheckResult = AdvancedResult.error(healthCheck, "NativeRW ping: Exception during ping, timeout");
        AdvancedResult actualHealthCheckResult = healthCheck.checkAdvanced();

        assertThat(actualHealthCheckResult.status(), is(equalTo(expectedHealthCheckResult.status())));
    }
}
