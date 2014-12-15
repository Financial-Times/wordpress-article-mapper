package com.ft.wordpressarticletransformer.resources;

import java.net.URI;

public class RequestFailedException extends RuntimeException{

    private final int responseStatusCode;
    private final URI requestUri;

    public RequestFailedException(URI requestUri, int responseStatusCode) {

        this.requestUri = requestUri;
        this.responseStatusCode = responseStatusCode;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }

    public URI getRequestUri() {
        return requestUri;
    }
}
