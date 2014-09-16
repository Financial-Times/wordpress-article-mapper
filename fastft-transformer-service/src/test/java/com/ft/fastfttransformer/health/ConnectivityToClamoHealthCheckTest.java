package com.ft.fastfttransformer.health;

import static com.ft.dropwizard.matcher.AdvancedHealthCheckResult.healthy;
import static com.ft.dropwizard.matcher.AdvancedHealthCheckResult.unhealthy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.ft.fastfttransformer.configuration.ClamoConnection;
import com.ft.fastfttransformer.response.Data;
import com.ft.fastfttransformer.response.FastFTResponse;
import com.ft.messaging.standards.message.v1.SystemId;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.dropwizard.client.JerseyClientConfiguration;
import org.apache.http.conn.ConnectTimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectivityToClamoHealthCheckTest {

	private ConnectivityToClamoHealthCheck healthCheck;
	private ClientResponse response;
	private WebResource.Builder builder;
	private static final int CONTENT_ID = 12345;


	@Before
	public void setup() {
		Client client = mock(Client.class);
		ClamoConnection clamoConnection = new ClamoConnection("localhost", "/api", 8080, mock(JerseyClientConfiguration.class));
		healthCheck = new ConnectivityToClamoHealthCheck("test health check", client, SystemId.systemIdFromCode("test-fastft"), "", clamoConnection, CONTENT_ID);

		WebResource webResource = mock(WebResource.class);
		response = mock(ClientResponse.class);
		when(client.resource(any(URI.class))).thenReturn(webResource);
		when(webResource.queryParam(anyString(), anyString())).thenReturn(webResource);
		builder = mock(WebResource.Builder.class);
		when(webResource.accept(anyString())).thenReturn(builder);
		when(builder.get(ClientResponse.class)).thenReturn(response);
	}

	@Test
	public void shouldReturnHealthyWhenClamoStatus200() throws Exception {
		when(response.getStatus()).thenReturn(200);
        FastFTResponse fastFTResponse = mock(FastFTResponse.class);
        FastFTResponse[] fastFTResponses = new FastFTResponse[]{fastFTResponse};
        when(response.getEntity(FastFTResponse[].class)).thenReturn(fastFTResponses);
        Data data = mock(Data.class);
        when(fastFTResponse.getData()).thenReturn(data);
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("id", CONTENT_ID);
        when(data.getAdditionalProperties()).thenReturn(dataMap);

        assertThat(healthCheck.checkAdvanced(), is(healthy()));
	}

    @Test
    public void shouldReturnunHealthyWhenClamoStatus200ButIdNotFound() throws Exception {
        when(response.getStatus()).thenReturn(200);
        FastFTResponse fastFTResponse = mock(FastFTResponse.class);
        FastFTResponse[] fastFTResponses = new FastFTResponse[]{fastFTResponse};
        when(response.getEntity(FastFTResponse[].class)).thenReturn(fastFTResponses);
        Data data = mock(Data.class);
        when(fastFTResponse.getData()).thenReturn(data);
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("id", "54321");
        when(data.getAdditionalProperties()).thenReturn(dataMap);

        assertThat(healthCheck.checkAdvanced(), is(unhealthy("Status code 200 was received from Clamo but content id did not match")));
    }

	@Test
	public void shouldReturnUnhealthyWhenClamoStatus500() throws Exception {
		when(response.getStatus()).thenReturn(500);
		assertThat(healthCheck.checkAdvanced(), is(unhealthy("Status code [500] received when receiving content from Clamo.")));
	}

	@Test
	public void shouldReturnUnhealthyWhenClamoStatus503() throws Exception {
		when(response.getStatus()).thenReturn(503);
		assertThat(healthCheck.checkAdvanced(), is(unhealthy("Status code [503] received when receiving content from Clamo.")));
	}

	@Test
	public void shouldReturnUnhealthyWhenClamoStatus400() throws Exception {
		when(response.getStatus()).thenReturn(400);
		assertThat(healthCheck.checkAdvanced(), is(unhealthy("Status code [400] received when receiving content from Clamo.")));
	}

	@Test
	public void shouldReturnUnhealthyWhenClamoTimesOut() throws Exception {
		when(builder.get(ClientResponse.class)).thenThrow(new ClientHandlerException(new ConnectTimeoutException("timed out")));
		assertThat(healthCheck.checkAdvanced(), is(unhealthy("timed out")));
	}

}
