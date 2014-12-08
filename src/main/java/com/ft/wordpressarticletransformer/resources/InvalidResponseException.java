package com.ft.wordpressarticletransformer.resources;

import com.sun.jersey.api.client.ClientResponse;

public class InvalidResponseException extends RuntimeException{

    private ClientResponse response;

    public InvalidResponseException(ClientResponse response) { this.response = response;}

    public ClientResponse getResponse() {
        return response;
    }

}
