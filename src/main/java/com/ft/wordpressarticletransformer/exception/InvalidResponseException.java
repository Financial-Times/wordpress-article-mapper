package com.ft.wordpressarticletransformer.exception;

public class InvalidResponseException extends WordPressApiException {

    public InvalidResponseException(String message) {
        super(message);
    }
}
