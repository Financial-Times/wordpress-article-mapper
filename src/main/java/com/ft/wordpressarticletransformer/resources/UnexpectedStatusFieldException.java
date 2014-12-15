package com.ft.wordpressarticletransformer.resources;

import com.ft.wordpressarticletransformer.response.WPFormat;

import java.util.Map;
import java.util.UUID;

public class UnexpectedStatusFieldException extends RuntimeException {

    private final String status;
    private final UUID uuid;
    private Map<String, Object> additionalProperties;


    public UnexpectedStatusFieldException(String status, UUID uuid) {

        this.status = status;
        this.uuid = uuid;
        this.additionalProperties = null;
    }

    public UnexpectedStatusFieldException(String status, Map<String,Object> additionalProperties) {
        this.status = status;
        this.uuid = null;
        this.additionalProperties = additionalProperties;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getStatus() {
        return status;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @Override
    public String getMessage() {
        String additionalPropertiesMessage = "";
        if(hasAdditionalProperties()) {
            additionalPropertiesMessage = System.lineSeparator()
                    +  "\tadditional properties: " + additionalProperties.toString();
        }
        return "status field in response not \"" + WPFormat.STATUS_OK + "\", was " + getStatus() +  additionalPropertiesMessage;
    }

    public boolean hasAdditionalProperties() {
        if(additionalProperties==null) {
            return false;
        }
        return !additionalProperties.isEmpty();
    }
}

