package com.ft.wordpressarticletransformer.resources;

import java.net.URI;

public class CannotConnectToWordPressException extends RuntimeException {
    
    private URI requestUri;

    public CannotConnectToWordPressException(URI requestUri, Throwable cause) {
        super(cause);
        this.requestUri = requestUri;
    }

    public URI getRequestUri() {
        return requestUri;
    }

}
