package com.ft.wordpressarticlemapper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;


@JsonDeserialize(builder = WordPressBlogPostContent.Builder.class)
public class WordPressBlogPostContent
        extends WordPressContent {

    private final String body;
    private final String opening;

    private WordPressBlogPostContent(UUID uuid,
                                     String title,
                                     List<String> titles,
                                     String byline,
                                     SortedSet<Brand> brands,
                                     SortedSet<Identifier> identifiers,
                                     Date publishedDate,
                                     String body,
                                     String opening,
                                     String description,
                                     String mediaType,
                                     Integer pixelWidth,
                                     Integer pixelHeight,
                                     String internalBinaryUrl,
                                     String externalBinaryUrl,
                                     String mainImage,
                                     Comments comments,
                                     String publishReference,
                                     Date lastModified,
                                     Date firstPublishedDate,
                                     AccessLevel accessLevel,
                                     String canBeDistributed,
                                     String webUrl,
                                     boolean scoop) {

        super(uuid, title, titles, byline, brands, identifiers, publishedDate, description, mediaType, pixelWidth,
                pixelHeight, internalBinaryUrl, externalBinaryUrl, mainImage, comments, publishReference, lastModified,
                firstPublishedDate, accessLevel, canBeDistributed, webUrl,scoop);

        this.body = body;
        this.opening = opening;
    }

    public String getBody() {
        return body;
    }

    public String getOpening() {
        return opening;
    }

    @Override
    public String toString() {
        return String.format("%s[body=%s,opening=%s]", super.toString(), body, opening);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o)
                && Objects.equal(this.body, ((WordPressBlogPostContent) o).body)
                && Objects.equal(this.opening, ((WordPressBlogPostContent) o).opening);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), body, opening);
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder
            extends WordPressContent.Builder<WordPressBlogPostContent> {

        private String body;
        private String opening;

        public Builder withBody(String body) {
            this.body = body;
            return this;
        }

        public Builder withOpening(String opening) {
            this.opening = opening;
            return this;
        }

        public Builder withValuesFrom(WordPressBlogPostContent content) {
            return ((Builder) super.withValuesFrom(content))
                    .withBody(content.getBody());
        }

        public WordPressBlogPostContent build() {
            return new WordPressBlogPostContent(getUuid(), getTitle(), getTitles(), getByline(),
                    getBrands(), getIdentifiers(), getPublishedDate(), body, opening, getDescription(),
                    getMediaType(), getPixelWidth(), getPixelHeight(),
                    getInternalBinaryUrl(), getExternalBinaryUrl(),
                    getMainImage(), getComments(), getPublishReference(), getLastModified(), getFirstPublishedDate(),
                    getAccessLevel(), getCanBeDistributed(), getWebUrl(),isScoop());
        }
    }
}
