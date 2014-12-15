package com.ft.wordpressarticletransformer.resources;

import java.util.UUID;

/**
 * The post has a type or custom type other than "post", meaning it has custom formatting associated with it and is
 * unsupported.
 */
public class UnsupportedPostTypeException extends RuntimeException {

    private final String actualType;
    private final UUID uuid;
    private final String supportedType;

    public UnsupportedPostTypeException(String actualType, UUID uuid, String supportedType) {
        this.actualType = actualType;
        this.uuid = uuid;
        this.supportedType = supportedType;
    }

    public String getActualType() {
        return actualType;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getSupportedType() {
        return supportedType;
    }
}
