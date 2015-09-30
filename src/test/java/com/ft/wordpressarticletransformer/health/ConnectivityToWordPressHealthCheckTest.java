package com.ft.wordpressarticletransformer.health;

import static com.ft.dropwizard.matcher.AdvancedHealthCheckResult.healthy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ft.api.jaxrs.errors.ServerError;
import com.ft.wordpressarticletransformer.configuration.WordPressConnection;
import com.ft.wordpressarticletransformer.resources.UnexpectedStatusCodeException;
import com.ft.wordpressarticletransformer.resources.WordPressResilientClient;
import com.ft.wordpressarticletransformer.response.WordPressMostRecentPostsResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectivityToWordPressHealthCheckTest {

    private static final URI EXAMPLE_URI = URI.create("http://example.com/");
    private ConnectivityToWordPressHealthCheck healthCheck;
	
	private WordPressResilientClient wordPressResilientClient = mock(WordPressResilientClient.class);
    private WordPressMostRecentPostsResponse response = mock(WordPressMostRecentPostsResponse.class);


	@Before
	public void setup() {

		WordPressConnection dummyConnection = new WordPressConnection("localhost","/path",8080);

		List<WordPressConnection> list = Collections.singletonList(dummyConnection);
		healthCheck = new ConnectivityToWordPressHealthCheck("test health check", wordPressResilientClient, "", list);
	}

	@Test
	public void shouldReturnHealthyWhenWPStatusIsOk() throws Exception {
		when(response.getStatus()).thenReturn("ok");

		when(response.getStatus()).thenReturn("ok");
		when(response.getCount()).thenReturn(1);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", "2014892");

        when(response.getAdditionalProperties()).thenReturn(dataMap);

		when(wordPressResilientClient.getRecentPosts(any(WordPressConnection.class))).thenReturn(response);

        assertThat(healthCheck.checkAdvanced(), is(healthy()));
	}

    @Test
    public void shouldReturnUnhealthyWhenWPStatus500() throws Exception {

        when(wordPressResilientClient.getRecentPosts(any(WordPressConnection.class))).thenThrow(new UnexpectedStatusCodeException(EXAMPLE_URI,500));

        assertThat(healthCheck.checkAdvanced(), is(not(healthy())));
    }

    @Test
    public void shouldReturnUnhealthyWhenWPClientThrowsException() throws Exception {
        when(wordPressResilientClient.getRecentPosts(any(WordPressConnection.class))).thenThrow(ServerError.status(503).exception());

        assertThat(healthCheck.checkAdvanced(), is(not(healthy())));
    }

    @Test
    public void shouldReturnUnhealthyWhenWPClientThrowsUnexpectedException() throws Exception {
        when(wordPressResilientClient.getRecentPosts(any(WordPressConnection.class))).thenThrow(new RuntimeException("synthetic exception"));

        assertThat(healthCheck.checkAdvanced(), is(not(healthy())));
    }

}
