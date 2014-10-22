package com.ft.fastfttransformer.resources;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.fastfttransformer.configuration.ClamoConnection;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ClamoResilientClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ClamoResilientClient.class);
    
    private final Client client;
    private final ClamoConnection clamoConnection;

    private final Timer requests;

    public ClamoResilientClient(Client client, MetricRegistry appMetrics, ClamoConnection clamoConnection) {
        this.client = client;
        this.clamoConnection = clamoConnection;
        this.requests = appMetrics.timer(MetricRegistry.name(TransformerResource.class, "requestToClamo"));
    }

    public ClientResponse doRequest(int postId) {
        String queryStringValue = Clamo.buildPostRequest(postId);

        URI fastFtContentByIdUri = getClamoBaseUrl(postId);
        
        String eq = null;
        try {
            eq = URLEncoder.encode(queryStringValue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // should never happen, UTF-8 is part of the Java spec
            throw ServerError.status(503).error("JVM Capability missing: UTF-8 encoding").exception();
        }

        WebResource webResource = client.resource(fastFtContentByIdUri);
        
        ClientHandlerException lastClientHandlerException = null;
        
        for (int attemptsCount = 1; attemptsCount <= clamoConnection.getNumberOfConnectionAttempts(); attemptsCount++) {
            LOGGER.info("[REQUEST STARTED] attempt={} requestUri={} queryString={}", attemptsCount, fastFtContentByIdUri, queryStringValue);
            Timer.Context requestsTimer = requests.time();
            long startTime = System.currentTimeMillis();
            
            ClientResponse response = null;
            
            try {
                response = webResource.queryParam("request", eq)
                        .accept("application/json").get(ClientResponse.class);
                return response; 
            } catch (ClientHandlerException che) {
                lastClientHandlerException = che; 
                LOGGER.warn("[REQUEST FAILED] attempt={} exception={}", attemptsCount, che.getMessage());
            } finally {
                long endTime = System.currentTimeMillis();
                long timeTakenMillis = (endTime - startTime);
                requestsTimer.stop();
                LOGGER.info("[REQUEST FINISHED] attempt={} time_ms={}", attemptsCount, timeTakenMillis);
            }
        }
        
        Throwable cause = lastClientHandlerException.getCause();
        if(cause instanceof IOException) {
            throw ServerError.status(503).context(webResource).error(
                        String.format("Cannot connect to Clamo for url: [%s] with queryString: [%s]", fastFtContentByIdUri, queryStringValue)).exception(cause);
        }
        throw lastClientHandlerException;

        
    }

    private URI getClamoBaseUrl(int id) {
        return UriBuilder.fromPath(clamoConnection.getPath())
                .scheme("http")
                .host(clamoConnection.getHostName())
                .port(clamoConnection.getPort())
                .build(id);
    }

}
