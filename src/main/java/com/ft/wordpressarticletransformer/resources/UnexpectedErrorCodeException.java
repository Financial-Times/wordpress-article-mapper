package com.ft.wordpressarticletransformer.resources;

import com.ft.wordpressarticletransformer.response.WordPressStatus;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

public class UnexpectedErrorCodeException extends WordPressApiException {

    private final UUID uuid;
    private final String error;

    public UnexpectedErrorCodeException(URI requestUri, String error, UUID uuid) {
        super(String.format("Unexpected error from WordPress: [%s] for uuid [%s].",  error, uuid), requestUri);
        this.error = error;
        this.uuid = uuid;
    }

    public UnexpectedErrorCodeException(URI requestUri, String error, Map<String, Object> additionalProperties) {
        super("error code in response not \"" + WordPressStatus.error + "\", was \"" + error + "\"" , additionalProperties, requestUri);
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
