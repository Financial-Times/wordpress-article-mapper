package com.ft.wordpressarticletransformer.resources;

import java.net.URI;

public class CannotConnectToWordPressException extends WordPressApiException {
    
    public CannotConnectToWordPressException(URI requestUri, Throwable cause) {
        super(cause, requestUri);
    }

}
