package com.ft.wordpressarticletransformer.resources;

import java.net.URI;

public class RequestFailedException extends AbstractAssankaWPAPIException {

    private final int responseStatusCode;

    public RequestFailedException(URI requestUri, int responseStatusCode) {
        super(String.format("Unexpected Client Response for [%s] with code [%s].", requestUri, responseStatusCode),requestUri);
        this.responseStatusCode = responseStatusCode;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }

}
