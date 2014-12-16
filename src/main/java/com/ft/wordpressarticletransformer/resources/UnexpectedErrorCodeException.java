package com.ft.wordpressarticletransformer.resources;

import java.util.UUID;

public class UnexpectedErrorCodeException extends RuntimeException {
    private final UUID uuid;
    private final String error;

    public UnexpectedErrorCodeException(String error, UUID uuid) {

        this.error = error;
        this.uuid = uuid;
    }

    public UnexpectedErrorCodeException(String error) {

        this.error = error;
        this.uuid = null;
    }

    public String getError() {
        return error;
    }

    public UUID getUuid() {
        return uuid;
    }
}
