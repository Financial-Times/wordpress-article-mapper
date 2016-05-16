package com.ft.wordpressarticletransformer.util;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import static com.ft.wordpressarticletransformer.util.FileReader.resourceFilePath;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_TEMPORARILY;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientMockBuilder {
    private String contentReadOutputTemplate;

    public ClientMockBuilder() {
        try {
            contentReadOutputTemplate = Files.toString(new File(resourceFilePath("content-read-output-template.json")), Charsets.UTF_8);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void mockResolverRedirect(Client resolverClient, URI from, URI to) {
        mockResolverRedirect(resolverClient, from, to, SC_MOVED_TEMPORARILY);
    }

    public void mockResolverRedirect(Client resolverClient, URI from, int status) {
        mockResolverRedirect(resolverClient, from, null, status);
    }

    private void mockResolverRedirect(Client resolverClient, URI from, URI to, int status) {

        WebResource resolverResource = mock(WebResource.class);
        WebResource.Builder resolverBuilder = mock(WebResource.Builder.class);
        when(resolverClient.resource(from)).thenReturn(resolverResource);
        when(resolverResource.cookie(any(Cookie.class))).thenReturn(resolverBuilder);

        ClientResponse redirectionResponse = mock(ClientResponse.class);
        when(redirectionResponse.getStatus()).thenReturn(status);
        if (to != null) {
            when(redirectionResponse.getLocation()).thenReturn(to);
        }
        when(resolverBuilder.head()).thenReturn(redirectionResponse);
        when(resolverResource.head()).thenReturn(redirectionResponse);

//        WebResource resolverResource = mock(WebResource.class);
//        WebResource.Builder resolverBuilder = mock(WebResource.Builder.class);
//        when(resolverClient.resource(from)).thenReturn(resolverResource);
//        when(resolverResource.cookie(any(Cookie.class))).thenReturn(resolverBuilder); //THIS IS THE LAST FIX!!!
//
//        ClientResponse redirectionResponse = mock(ClientResponse.class);
//        when(redirectionResponse.getStatus()).thenReturn(SC_MOVED_TEMPORARILY);
//        when(redirectionResponse.getLocation()).thenReturn(to);
//        when(resolverBuilder.head()).thenReturn(redirectionResponse);

    }

    public void mockDocumentStoreQuery(Client documentStoreQueryClient, URI queryURI, URI to, int status) {
        WebResource queryResource = mock(WebResource.class);
        WebResource.Builder queryBuilder = mock(WebResource.Builder.class);

        when(documentStoreQueryClient.resource(queryURI)).thenReturn(queryResource);
        when(queryResource.header("Host", "document-store-api")).thenReturn(queryBuilder);

        ClientResponse queryResponse = mock(ClientResponse.class);
        when(queryResponse.getStatus()).thenReturn(status);
        when(queryResponse.getLocation()).thenReturn(to);
        when(queryBuilder.head()).thenReturn(queryResponse);
    }

    public void mockDocumentStoreQuery(Client documentStoreQueryClient, URI documentStoreQueryBaseUri, URI authority, URI identifierValue, URI to, int status) {
        URI queryURI = buildDocumentStoreQueryUri(documentStoreQueryBaseUri, authority, identifierValue);
        mockDocumentStoreQuery(documentStoreQueryClient, queryURI, to, status);
    }

    public URI buildDocumentStoreQueryUri(URI documentStoreQueryUri, URI authority, URI identifierValue) {
        URI queryURI = null;
        try {
            queryURI = UriBuilder.fromUri(documentStoreQueryUri)
                    .queryParam("identifierAuthority", URLEncoder.encode(authority.toASCIIString(), "UTF-8"))
                    .queryParam("identifierValue", URLEncoder.encode(identifierValue.toASCIIString(), "UTF-8"))
                    .build();
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
        }
        return queryURI;
    }

    public void mockContentRead(Client contentReadClient, URI contentReadUri, String contentUuid, String hostHeader, int status) {

        WebResource contentReadResource = mock(WebResource.class);
        WebResource.Builder contentReadBuilder = mock(WebResource.Builder.class);
        URI contentReadURI = UriBuilder.fromUri(contentReadUri).path("{uuid}").build(contentUuid);

        when(contentReadClient.resource(contentReadURI)).thenReturn(contentReadResource);
        when(contentReadResource.header("Host", hostHeader)).thenReturn(contentReadBuilder);
        when(contentReadBuilder.accept(MediaType.APPLICATION_JSON_TYPE)).thenReturn(contentReadBuilder);

        ClientResponse contentReadResponse = mock(ClientResponse.class);
        when(contentReadBuilder.get(ClientResponse.class)).thenReturn(contentReadResponse);
        when(contentReadResponse.getStatus()).thenReturn(status);
        if (status == SC_OK) {
            when(contentReadResponse.getEntityInputStream())
                    .thenAnswer((Answer<InputStream>) invocation -> buildResponseInputStream(contentUuid));
        }
    }

    private InputStream buildResponseInputStream(String uuid) {
        String contentReadOutput = String.format(contentReadOutputTemplate, uuid, uuid, uuid);
        return new ByteArrayInputStream(contentReadOutput.getBytes(StandardCharsets.UTF_8));
    }
}
