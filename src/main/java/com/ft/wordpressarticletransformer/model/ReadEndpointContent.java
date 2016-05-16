package com.ft.wordpressarticletransformer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadEndpointContent {
    private String id;
    private String type;

    private ReadEndpointContent(@JsonProperty("id") String id,
                                @JsonProperty("type") String type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("id", id)
                .add("type", type)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReadEndpointContent that = (ReadEndpointContent) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    public static class Builder {

        private String id;
        private String type;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public ReadEndpointContent build() {
            return new ReadEndpointContent(
                    id,
                    type);
        }

    }


}