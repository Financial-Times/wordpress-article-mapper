package com.ft.wordpressarticletransformer.resources;

import java.util.UUID;

/**
 * The WP defined error field contained an unexpected value.
 */
public class UnknownStatusErrorCodeException extends RuntimeException {

    private final UUID uuid;
    private final String error;

    public UnknownStatusErrorCodeException(String error, UUID uuid) {

        this.error = error;
        this.uuid = uuid;
    }

    public UnknownStatusErrorCodeException(String error) {

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
