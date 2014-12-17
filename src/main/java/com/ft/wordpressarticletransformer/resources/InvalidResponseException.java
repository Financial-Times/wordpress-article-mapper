package com.ft.wordpressarticletransformer.resources;

import com.sun.jersey.api.client.ClientResponse;

import java.net.URI;

public class InvalidResponseException extends AbstractAssankaWPAPIException {

    private ClientResponse response;

    public InvalidResponseException(ClientResponse response, URI requestUri) {
        super(String.format("Response not a valid WordPressResponse - check your url. Response status is [%s]", response.getStatus()), requestUri);
        this.response = response;
    }

    public ClientResponse getResponse() {
        return response;
    }

}
