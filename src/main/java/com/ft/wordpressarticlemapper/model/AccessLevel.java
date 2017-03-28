package com.ft.wordpressarticlemapper.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AccessLevel {

    SUBSCRIBED("subscribed"),
    REGISTERED("registered"),
    PREMIUM("premium"),
    FREE("free");

    private String accessLevel;

    AccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    @JsonValue
    public String getAccessLevel() {
        return accessLevel;
    }
}
