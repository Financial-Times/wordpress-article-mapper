package com.ft.wordpressarticletransformer.health;

import static com.ft.dropwizard.matcher.AdvancedHealthCheckResult.healthy;
import static com.ft.dropwizard.matcher.AdvancedHealthCheckResult.unhealthy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ft.api.jaxrs.errors.ServerError;
import com.ft.wordpressarticletransformer.configuration.WordPressConnection;
import com.ft.wordpressarticletransformer.resources.WordPressResilientClient;
import com.ft.wordpressarticletransformer.response.WordPressMostRecentPostsResponse;
import com.ft.messaging.standards.message.v1.SystemId;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectivityToWordPressHealthCheckTest {

	private ConnectivityToWordPressHealthCheck healthCheck;
	
	private WordPressResilientClient wordPressResilientClient = mock(WordPressResilientClient.class);
	private ClientResponse response = mock(ClientResponse.class);
	private WebResource.Builder builder;
	private static final int CONTENT_ID = 12345;


	@Before
	public void setup() {
		when(wordPressResilientClient.getContent(CONTENT_ID)).thenReturn(response);
		List<WordPressConnection> list = null;
		healthCheck = new ConnectivityToWordPressHealthCheck("test health check", wordPressResilientClient, SystemId.systemIdFromCode("test-fastft"), "", list);
	}

	@Test
	public void shouldReturnHealthyWhenClamoStatus200() throws Exception {
//		when(response.getStatus()).thenReturn(200);
//        WordPressMostRecentPostsResponse wordPressMostRecentPostsResponse = mock(WordPressMostRecentPostsResponse.class);
//        WordPressMostRecentPostsResponse[] wordPressMostRecentPostsResponses = new WordPressMostRecentPostsResponse[]{wordPressMostRecentPostsResponse};
//        when(response.getEntity(WordPressMostRecentPostsResponse[].class)).thenReturn(wordPressMostRecentPostsResponses);
//        Data data = mock(Data.class);
//        when(wordPressMostRecentPostsResponse.getData()).thenReturn(data);
//        when(wordPressMostRecentPostsResponse.getStatus()).thenReturn("ok");
//        Map<String, Object> dataMap = new HashMap<String, Object>();
//        dataMap.put("id", CONTENT_ID);
//        when(data.getAdditionalProperties()).thenReturn(dataMap);
//
//        assertThat(healthCheck.checkAdvanced(), is(healthy()));
	}

}
