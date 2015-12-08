package com.ft.wordpressarticletransformer.model;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;


@JsonDeserialize(builder = WordPressBlogPostContent.Builder.class)
public class WordPressBlogPostContent
        extends WordPressContent {

    private final String body;

    private WordPressBlogPostContent(String uuid,
                   String title,
                   List<String> titles,
                   String byline,
                   SortedSet<Brand> brands,
                   SortedSet<Identifier> identifiers,
                   Date publishedDate,
                   String body,
                   String description,
                   String mediaType,
                   Integer pixelWidth,
                   Integer pixelHeight,
                   String internalBinaryUrl,
                   String externalBinaryUrl,
                   String mainImage,
                   Comments comments,
                   String publishReference) {
        
        super(uuid, title, titles, byline, brands, identifiers, publishedDate, description, mediaType, pixelWidth, pixelHeight, internalBinaryUrl, externalBinaryUrl,
                mainImage, comments, publishReference);
        
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return String.format("%s[body=%s]", super.toString(), body);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o)
                && Objects.equal(this.body, ((WordPressBlogPostContent)o).body);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), body);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
            extends WordPressContent.Builder<WordPressBlogPostContent> {

        private String body;

        public Builder withBody(String body) {
            this.body = body;
            return this;
        }
        
        public Builder withValuesFrom(WordPressBlogPostContent content) {
            return ((Builder)super.withValuesFrom(content))
                    .withBody(content.getBody());
        }

		public WordPressBlogPostContent build() {
            return new WordPressBlogPostContent(getUuid(), getTitle(), getTitles(), getByline(),
                    getBrands(), getIdentifiers(), getPublishedDate(), body, getDescription(),
                    getMediaType(), getPixelWidth(), getPixelHeight(),
                    getInternalBinaryUrl(), getExternalBinaryUrl(),
                    getMainImage(), getComments(), getPublishReference());
        }
    }
}
