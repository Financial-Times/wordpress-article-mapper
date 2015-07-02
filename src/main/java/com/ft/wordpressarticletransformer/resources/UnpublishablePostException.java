package com.ft.wordpressarticletransformer.resources;

import java.net.URI;
import java.util.UUID;

/**
 * The post is a valid WordPress post, but cannot be published by the transformer. 
 * For example:
 * <ul>
 * <li>it has a type or custom type other than "post", meaning it has custom formatting associated with it</li>
 * <li>it has no body text</li>
 * </ul>
 */
public class UnpublishablePostException
        extends WordPressApiException {
    
    private final UUID uuid;

    public UnpublishablePostException(URI requestUri, UUID uuid, String reason) {
        super(reason, requestUri);
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
