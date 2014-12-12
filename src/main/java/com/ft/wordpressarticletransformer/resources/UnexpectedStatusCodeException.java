package com.ft.wordpressarticletransformer.resources;

import java.net.URI;

/**
 * The HTTP status code ahs not been provided for (200 is expected for application level errors).
 */
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
