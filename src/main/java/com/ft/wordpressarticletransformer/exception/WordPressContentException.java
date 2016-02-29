package com.ft.wordpressarticletransformer.exception;

public class WordPressContentException extends RuntimeException {

    public WordPressContentException(String message) {
        super(message);
    }

    public WordPressContentException(String message, Throwable cause) {
        super(message, cause);
    }
}
