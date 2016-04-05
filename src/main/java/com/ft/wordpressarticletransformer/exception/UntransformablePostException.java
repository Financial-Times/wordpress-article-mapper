package com.ft.wordpressarticletransformer.exception;

/**
 * The post is a valid WordPress post, but cannot be published by the transformer.
 * For example:
 * <ul>
 * <li>it has no body text, or consists only of unsupported content</li>
 * </ul>
 */
public class UntransformablePostException
        extends WordPressContentException {

    private final String uuid;

    public UntransformablePostException(String uuid, String reason) {
        super(reason);
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
