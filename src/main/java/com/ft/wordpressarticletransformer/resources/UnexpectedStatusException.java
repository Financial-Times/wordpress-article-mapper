package com.ft.wordpressarticletransformer.resources;

import com.sun.jersey.api.client.ClientResponse;

import java.net.URI;

public class UnexpectedStatusException extends RuntimeException {

    private int responseStatusCode;
    private ClientResponse response;

    public UnexpectedStatusException(int responseStatusCode, ClientResponse response) {
        this.responseStatusCode = responseStatusCode;
        this.response = response;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }

    public ClientResponse getResponse() {
        return response;
    }


}
