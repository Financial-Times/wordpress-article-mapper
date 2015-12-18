package com.ft.wordpressarticletransformer.exception;


public class UnexpectedErrorCodeException extends WordPressContentException {

    public UnexpectedErrorCodeException(String error, String uuid) {
        super(String.format("Unexpected error from WordPress: [%s] for uuid [%s].", error, uuid));
    }
}
