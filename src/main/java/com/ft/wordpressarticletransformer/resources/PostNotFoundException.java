package com.ft.wordpressarticletransformer.resources;

import java.util.UUID;

public class PostNotFoundException extends RuntimeException {
    private final UUID uuid;
    private final String error;

    public PostNotFoundException(String error) {
        this.error = error;
        this.uuid = null;
    }

    public PostNotFoundException(String error, UUID uuid) {
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
