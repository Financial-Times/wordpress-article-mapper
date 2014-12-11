package com.ft.wordpressarticletransformer.resources;

import java.util.UUID;

public class UnknownErrorCodeException extends RuntimeException {
    private final UUID uuid;
    private final String error;

    public UnknownErrorCodeException(String error, UUID uuid) {

        this.error = error;
        this.uuid = uuid;
    }

    public UnknownErrorCodeException(String error) {

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
