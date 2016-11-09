package com.ft.wordpressarticlemapper.exception;

public class UnexpectedStatusFieldException extends WordPressContentException {

    private final String status;
    private final String uuid;

    public UnexpectedStatusFieldException(String status, String uuid) {
        super(String.format("Unexpected WordPress status=\"%s\" for uuid=\"%s\".", status, uuid));
        this.status = status;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getStatus() {
        return status;
    }

}

