package com.ft.wordpressarticletransformer.resources;

import java.util.UUID;

public class UnexpectedStatusFieldException extends RuntimeException {

    private final String status;
    private final UUID uuid;

    public UnexpectedStatusFieldException(String status, UUID uuid) {

        this.status = status;
        this.uuid = uuid;
    }

    public UnexpectedStatusFieldException(String status) {

        this.status = status;
        this.uuid = null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getStatus() {
        return status;
    }
}

