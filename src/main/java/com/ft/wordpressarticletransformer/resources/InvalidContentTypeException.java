package com.ft.wordpressarticletransformer.resources;

import com.sun.jersey.api.client.ClientResponse;

import java.net.URI;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class InvalidContentTypeException extends WordPressApiException {

    private ClientResponse response;

    public InvalidContentTypeException(List<String> contentTypes, ClientResponse response, URI requestUri) {
        super(String.format("Response not application/json. Response content type is [%s], response body is [%s]", 
                contentTypes, StringUtils.left(response.getEntity(String.class), 200)), requestUri);
        this.response = response;
    }

    public ClientResponse getResponse() {
        return response;
    }

}
