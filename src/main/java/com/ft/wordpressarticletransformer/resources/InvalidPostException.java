package com.ft.wordpressarticletransformer.resources;

import com.ft.wordpressarticletransformer.response.WordPressResponse;

import java.net.URI;

public class InvalidPostException extends RuntimeException {

    private WordPressResponse wordPressResponse;


    public InvalidPostException(WordPressResponse wordPressResponse) {
        this.wordPressResponse = wordPressResponse;
    }

    public WordPressResponse getWordPressResponse() {
        return wordPressResponse;
    }

}
