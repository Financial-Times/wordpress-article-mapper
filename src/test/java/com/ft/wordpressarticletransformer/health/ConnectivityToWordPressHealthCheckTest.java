package com.ft.wordpressarticletransformer.health;

import static com.ft.dropwizard.matcher.AdvancedHealthCheckResult.healthy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ft.wordpressarticletransformer.configuration.WordPressConnection;
import com.ft.wordpressarticletransformer.resources.WordPressResilientClient;
import com.ft.wordpressarticletransformer.response.WordPressMostRecentPostsResponse;
import com.ft.messaging.standards.message.v1.SystemId;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectivityToWordPressHealthCheckTest {

	private ConnectivityToWordPressHealthCheck healthCheck;
	
	private WordPressResilientClient wordPressResilientClient = mock(WordPressResilientClient.class);
	private ClientResponse response = mock(ClientResponse.class);


	@Before
	public void setup() {

		WordPressConnection dummyConnection = new WordPressConnection("localhost","/path",8080);

		List<WordPressConnection> list = Collections.singletonList(dummyConnection);
		healthCheck = new ConnectivityToWordPressHealthCheck("test health check", wordPressResilientClient, SystemId.systemIdFromCode("test-fastft"), "", list);
	}

	@Test
	public void shouldReturnHealthyWhenClamoStatus200() throws Exception {
		when(response.getStatus()).thenReturn(200);
        WordPressMostRecentPostsResponse wordPressMostRecentPostsResponse = mock(WordPressMostRecentPostsResponse.class);

        when(response.getEntity(WordPressMostRecentPostsResponse.class)).thenReturn(wordPressMostRecentPostsResponse);

		when(wordPressMostRecentPostsResponse.getStatus()).thenReturn("ok");
		when(wordPressMostRecentPostsResponse.getCount()).thenReturn(1);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", "2014892");

        when(wordPressMostRecentPostsResponse.getAdditionalProperties()).thenReturn(dataMap);

		when(wordPressResilientClient.getRecentPosts(any(WordPressConnection.class))).thenReturn(response);

        assertThat(healthCheck.checkAdvanced(), is(healthy()));
	}



}
