package com.ft.wordpressarticletransformer.resources;

import java.util.UUID;

/**
 * The WP defined error field was absent.
 */
public class ErrorCodeNotFoundException extends RuntimeException {

    private final UUID uuid;
    private final String error;

    public ErrorCodeNotFoundException(String error) {
        this.error = error;
        this.uuid = null;
    }

    public ErrorCodeNotFoundException(String error, UUID uuid) {

        this.error = error;
        this.uuid = uuid;
    }

    public String getError() {
        return error;
    }

    public UUID getUuid() {
        return uuid;
    }
}
