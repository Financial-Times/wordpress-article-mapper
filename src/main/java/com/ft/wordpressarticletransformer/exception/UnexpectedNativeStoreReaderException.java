package com.ft.wordpressarticletransformer.exception;

public class UnexpectedNativeStoreReaderException extends RuntimeException {

    public UnexpectedNativeStoreReaderException(int statusCode, Throwable cause) {
        super(String.format("Unexpected error status from Native Reader: [%s].",  statusCode), cause);
    }

}
