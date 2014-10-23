package com.ft.fastfttransformer.health;

import static com.ft.dropwizard.matcher.AdvancedHealthCheckResult.healthy;
import static com.ft.dropwizard.matcher.AdvancedHealthCheckResult.unhealthy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import com.ft.api.jaxrs.errors.ServerError;
import com.ft.fastfttransformer.resources.ClamoResilientClient;
import com.ft.fastfttransformer.response.Data;
import com.ft.fastfttransformer.response.FastFTResponse;
import com.ft.messaging.standards.message.v1.SystemId;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.http.conn.ConnectTimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectivityToClamoHealthCheckTest {

	private ConnectivityToClamoHealthCheck healthCheck;
	
	private ClamoResilientClient clamoResilientClient = mock(ClamoResilientClient.class);
	private ClientResponse response = mock(ClientResponse.class);
	private WebResource.Builder builder;
	private static final int CONTENT_ID = 12345;


	@Before
	public void setup() {
		when(clamoResilientClient.doRequest(CONTENT_ID)).thenReturn(response);
		healthCheck = new ConnectivityToClamoHealthCheck("test health check", clamoResilientClient, SystemId.systemIdFromCode("test-fastft"), "", CONTENT_ID);
	}

	@Test
	public void shouldReturnHealthyWhenClamoStatus200() throws Exception {
		when(response.getStatus()).thenReturn(200);
        FastFTResponse fastFTResponse = mock(FastFTResponse.class);
        FastFTResponse[] fastFTResponses = new FastFTResponse[]{fastFTResponse};
        when(response.getEntity(FastFTResponse[].class)).thenReturn(fastFTResponses);
        Data data = mock(Data.class);
        when(fastFTResponse.getData()).thenReturn(data);
        when(fastFTResponse.getStatus()).thenReturn("ok");
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("id", CONTENT_ID);
        when(data.getAdditionalProperties()).thenReturn(dataMap);

        assertThat(healthCheck.checkAdvanced(), is(healthy()));
	}

    @Test
    public void shouldReturnUnhealthyWhenClamoStatus200ButIdNotFound() throws Exception {
        when(response.getStatus()).thenReturn(200);
        FastFTResponse fastFTResponse = mock(FastFTResponse.class);
        FastFTResponse[] fastFTResponses = new FastFTResponse[]{fastFTResponse};
        when(response.getEntity(FastFTResponse[].class)).thenReturn(fastFTResponses);
        Data data = mock(Data.class);
        when(fastFTResponse.getData()).thenReturn(data);
        when(fastFTResponse.getStatus()).thenReturn("ok");
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
	    when(clamoResilientClient.doRequest(CONTENT_ID)).thenThrow(ServerError.status(503).error("timed out").exception());
		assertThat(healthCheck.checkAdvanced(), is(unhealthy("com.ft.api.jaxrs.errors.WebApplicationServerException")));
	}

	@Test
	public void shouldReturnUnhealthyWhenClamoStatusFieldNotOk() throws Exception {
		when(response.getStatus()).thenReturn(200);
        FastFTResponse fastFTResponse = mock(FastFTResponse.class);
        FastFTResponse[] fastFTResponses = new FastFTResponse[]{fastFTResponse};
        when(response.getEntity(FastFTResponse[].class)).thenReturn(fastFTResponses);
        when(fastFTResponse.getStatus()).thenReturn("error");
		assertThat(healthCheck.checkAdvanced(), is(unhealthy("status field in response not \"ok\"")));
	}

}
