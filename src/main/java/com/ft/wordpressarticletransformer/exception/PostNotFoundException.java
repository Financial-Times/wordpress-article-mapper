package com.ft.wordpressarticletransformer.exception;

public class PostNotFoundException extends WordPressApiException {

    private final String uuid;

    public PostNotFoundException(String uuid) {
        super(String.format("Error. Content with uuid: [%s] not found", uuid));
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
