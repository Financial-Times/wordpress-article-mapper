package com.ft.wordpressarticletransformer.resources;

import java.net.URI;
import java.util.UUID;

/**
 * The post has a type or custom type other than "post", meaning it has custom formatting associated with it and is
 * unsupported.
 */
public class UnsupportedPostTypeException extends WordPressApiException {

    private final String actualType;
    private final UUID uuid;
    private final String supportedType;

    public UnsupportedPostTypeException(URI requestUri, String actualType, UUID uuid, String supportedType) {
        super(String.format("Not a valid post, type is [%s], should be [%s], for content with uuid:[%s]", actualType, supportedType, uuid) , requestUri);
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
