package com.ft.wordpressarticletransformer.resources;

import java.net.URI;

/**
 * The HTTP status code ahs not been provided for (200 is expected for application level errors).
 */
public class UnexpectedStatusCodeException extends AbstractAssankaWPAPIException {

    private final URI requestUri;
    private final int responseStatusCode;


    public UnexpectedStatusCodeException(URI requestUri, int responseStatusCode) {
        super(String.format("Unexpected Client Response for [%s] with code [%s].", requestUri, responseStatusCode),requestUri);
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
