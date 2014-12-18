package com.ft.wordpressarticletransformer.resources;

import java.net.URI;
import java.util.UUID;

public class PostNotFoundException extends WordPressApiException {

    private final UUID uuid;
    private final String error;

    public PostNotFoundException(URI requestUri, String error, UUID uuid) {
        super(String.format("Error [%s]. Content with uuid: [%s] not found", error, uuid),requestUri);
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
