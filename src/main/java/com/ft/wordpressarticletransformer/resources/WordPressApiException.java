package com.ft.wordpressarticletransformer.resources;

import java.net.URI;
import java.util.Map;

/**
 * A generic exception class template with support for recording and formatting the URL and any additional properties
 * associated with the response.
 * @author Simon.Gibbs
 */
public class WordPressApiException extends RuntimeException {

    private Map<String, Object> additionalProperties;
    private URI requestUri;

    public WordPressApiException(Throwable cause, URI requestUri) {
        super(cause.getMessage(),cause);
        this.additionalProperties = null;
        this.requestUri = requestUri;
    }

    public WordPressApiException(String message, URI requestUri) {
        super(message);
        this.additionalProperties = null;
        this.requestUri = requestUri;
    }

    public WordPressApiException(String message, Map<String, Object> additionalProperties, URI requestUri) {
        super(message);
        this.requestUri = requestUri;
        this.additionalProperties = additionalProperties;
    }

    @Override
    public String getMessage() {
        String additionalPropertiesMessage = "";
        if(hasAdditionalProperties()) {
            additionalPropertiesMessage = System.lineSeparator()
                    +  "\tadditionalProperties=" + additionalProperties.toString();
        }
        String uriMessage = "";
        if(hasURI()) {
            uriMessage = System.lineSeparator()
                    +  "\trequestUri=" + requestUri;
        }
        return super.getMessage() +  additionalPropertiesMessage + uriMessage;
    }

    private boolean hasURI() {
        return requestUri != null;
    }

    public boolean hasAdditionalProperties() {
        if(additionalProperties==null) {
            return false;
        }
        return !additionalProperties.isEmpty();
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public URI getRequestUri() {
        return requestUri;
    }
}
