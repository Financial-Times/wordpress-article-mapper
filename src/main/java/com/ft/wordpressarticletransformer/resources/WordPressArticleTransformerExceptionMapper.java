package com.ft.wordpressarticletransformer.resources;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import com.ft.wordpressarticletransformer.exception.BrandResolutionException;
import com.ft.wordpressarticletransformer.exception.InvalidResponseException;
import com.ft.wordpressarticletransformer.exception.PostNotFoundException;
import com.ft.wordpressarticletransformer.exception.UnpublishablePostException;
import com.ft.wordpressarticletransformer.exception.WordPressContentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ft.bodyprocessing.BodyProcessingException;


public class WordPressArticleTransformerExceptionMapper
        extends com.ft.api.jaxrs.errors.RuntimeExceptionMapper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WordPressArticleTransformerExceptionMapper.class);
    
    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof WordPressContentException) {
            return toResponse((WordPressContentException)exception);
        }
        
        if (exception instanceof IllegalArgumentException) {
            return respondWith(SC_BAD_REQUEST, exception.getMessage(), exception);
        }
        
        if (exception instanceof BrandResolutionException) {
            return respondWith(SC_INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
        }
        
        if (exception instanceof BodyProcessingException) {
            return respondWith(SC_INTERNAL_SERVER_ERROR, "article has invalid body", exception);
        }
        
        return super.toResponse(exception);
    }
    
    private Response toResponse(WordPressContentException wpe) {
        if (wpe instanceof PostNotFoundException) {
            return respondWith(SC_NOT_FOUND, wpe.getMessage(), wpe,
                    Collections.singletonMap("uuid", ((PostNotFoundException)wpe).getUuid())
                    );
        }
        
        if (wpe instanceof UnpublishablePostException) {
            return respondWith(SC_UNPROCESSABLE_ENTITY, wpe.getMessage(), wpe);
        }
        
        if (wpe instanceof InvalidResponseException) {
            return respondWith(SC_BAD_REQUEST, wpe.getMessage(), wpe);
        }

        return respondWith(SC_INTERNAL_SERVER_ERROR, wpe.getMessage(), wpe);
    }
    
    private Response respondWith(int status, String reason, Throwable t) {
        return respondWith(status, reason, t, Collections.emptyMap());
    }
    
    private Response respondWith(int status, String reason, Throwable t, Map<String,Object> context) {
        logResponse(status, reason, t);
        
        Map<String,Object> responseMessage = new HashMap<>(context);
        responseMessage.put("message", reason);
        return Response.serverError().status(status).entity(responseMessage).type(APPLICATION_JSON_TYPE).build();
    }

    private void logResponse(int status, String reason, Throwable t) {
        String logMessage = format("Transformer error. Responding with status <%s> and reason <%s>.", status, reason);
        if (400 <= status && status < 500) {
            LOGGER.warn(logMessage, t);
        } else {
            LOGGER.error(logMessage, t);
        }
    }
}

