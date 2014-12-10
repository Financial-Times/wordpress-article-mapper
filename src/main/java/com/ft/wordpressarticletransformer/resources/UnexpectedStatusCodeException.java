package com.ft.wordpressarticletransformer.resources;

import java.net.URI;

public class UnexpectedStatusCodeException extends RuntimeException {

    private final URI requestUri;
    private final int responseStatusCode;


    public UnexpectedStatusCodeException(URI requestUri, int responseStatusCode) {

        this.requestUri = requestUri;
        this.responseStatusCode = responseStatusCode;
    }

    public URI getRequestUri() {
        return requestUri;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }
}
