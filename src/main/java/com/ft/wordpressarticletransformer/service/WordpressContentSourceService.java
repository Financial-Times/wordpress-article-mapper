package com.ft.wordpressarticletransformer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.client.exceptions.ApiNetworkingException;
import com.ft.api.jaxrs.client.exceptions.RemoteApiException;
import com.ft.wordpressarticletransformer.exception.NativeStoreReaderUnavailableException;
import com.ft.wordpressarticletransformer.exception.NativeStoreReaderUnreachableException;
import com.ft.wordpressarticletransformer.exception.PostNotFoundException;
import com.ft.wordpressarticletransformer.exception.UnexpectedNativeStoreReaderException;
import com.ft.wordpressarticletransformer.response.WordPressResponse;

import java.util.Map;

public class WordpressContentSourceService {

    private static final int NOT_FOUND = 404;
    private static final int SERVICE_UNAVAILABLE = 503;
    private final WordpressResponseValidator wordpressResponseValidator;
    private final NativeReaderClient nativeReaderClient;

    public WordpressContentSourceService(WordpressResponseValidator wordpressResponseValidator, NativeReaderClient nativeReaderClient) {
        this.wordpressResponseValidator = wordpressResponseValidator;
        this.nativeReaderClient = nativeReaderClient;
    }

    public WordPressResponse getValidWordpressResponse(final String uuid, final String transactionId) {
        try {
            Map<String, Object> wordpressContent = nativeReaderClient.getWordpressContent(uuid, transactionId);
            WordPressResponse wordPressResponse = new ObjectMapper().convertValue(wordpressContent, WordPressResponse.class);
            wordpressResponseValidator.validateWordpressResponse(wordPressResponse, uuid);
            return wordPressResponse;

        } catch (RemoteApiException rae) {
            switch (rae.getStatus()) {
                case NOT_FOUND:
                    throw new PostNotFoundException(uuid);
                case SERVICE_UNAVAILABLE:
                    throw new NativeStoreReaderUnavailableException(rae.getMessage());
                default:
                    throw new UnexpectedNativeStoreReaderException(rae.getStatus(), rae);
            }
        } catch (ApiNetworkingException ne) {
            throw new NativeStoreReaderUnreachableException(ne);
        }
    }


}
