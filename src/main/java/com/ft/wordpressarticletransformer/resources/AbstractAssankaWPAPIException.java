package com.ft.wordpressarticletransformer.resources;

import java.net.URI;
import java.util.Map;

/**
 * FT Labs seem to have a standard way of present API errors (it turns up in FastFT too)
 * in WP this is implemented in <code>assanka-*</code> plugins (the original name for FT LAbs as Assanka)
 *
 * @author Simon.Gibbs
 */
public class AbstractAssankaWPAPIException  extends RuntimeException {

    private Map<String, Object> additionalProperties;
    private URI requestUri;

    public AbstractAssankaWPAPIException(String message, URI requestUri) {
        super(message);
        this.additionalProperties = null;
        this.requestUri = requestUri;
    }

    public AbstractAssankaWPAPIException(String message, Map<String, Object> additionalProperties, URI requestUri) {
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
