package com.ft.wordpressarticletransformer.resources;

import com.ft.wordpressarticletransformer.response.WPFormat;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

public class UnknownErrorCodeException extends AbstractAssankaWPAPIException {

    private final UUID uuid;
    private final String error;
    private Map<String, Object> additionalProperties;

    public UnknownErrorCodeException(URI requestUri, String error, UUID uuid) {
        super(String.format("Unexpected error from WordPress: [%s] for uuid [%s].",  error, uuid), requestUri);
        this.error = error;
        this.uuid = uuid;
    }

    public UnknownErrorCodeException(URI requestUri, String error, Map<String,Object> additionalProperties) {
        super("error code in response not \"" + WPFormat.STATUS_ERROR + "\", was \"" + error + "\"" , requestUri);
        this.error = error;
        this.additionalProperties = additionalProperties;
        this.uuid = null;
    }

    @Override
    public String getMessage() {
        String additionalPropertiesMessage = "";
        if(hasAdditionalProperties()) {
            additionalPropertiesMessage = System.lineSeparator()
                    +  "\tadditional properties: " + additionalProperties.toString();
        }
        return "error code in response not \"" + WPFormat.STATUS_ERROR + "\", was " + error + additionalPropertiesMessage;
    }

    public String getError() {
        return error;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean hasAdditionalProperties() {
        if(additionalProperties==null) {
            return false;
        }
        return !additionalProperties.isEmpty();
    }
}
