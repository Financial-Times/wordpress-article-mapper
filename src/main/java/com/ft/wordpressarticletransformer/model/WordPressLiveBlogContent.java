package com.ft.wordpressarticletransformer.model;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


@JsonDeserialize(builder = WordPressLiveBlogContent.Builder.class)
public class WordPressLiveBlogContent
        extends WordPressContent {

    private final Temporal temporal;

    private WordPressLiveBlogContent(UUID uuid,
                   String title,
                   List<String> titles,
                   String byline,
                   SortedSet<Brand> brands,
                   SortedSet<Identifier> identifiers,
                   Date publishedDate,
                   String description,
                   String mediaType,
                   Integer pixelWidth,
                   Integer pixelHeight,
                   String internalBinaryUrl,
                   String externalBinaryUrl,
                   String mainImage,
                   Comments comments,
                   Temporal temporal,
                   String publishReference) {
        
        super(uuid, title, titles, byline, brands, identifiers, publishedDate, description, mediaType, pixelWidth, pixelHeight, internalBinaryUrl, externalBinaryUrl,
                mainImage, comments, publishReference);
        this.temporal = temporal;
        
    }

    @Deprecated
    public boolean isRealtime() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s[realtime=true][temporal=%s]", super.toString(),temporal);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder buildFrom(Temporal temporal) {
        return (new Builder()).withTemporal(temporal);
    }

    public Temporal getTemporal() {
        return temporal;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder
            extends WordPressContent.Builder<WordPressLiveBlogContent> {

        private Temporal temporal;

        public Builder withValuesFrom(WordPressContent content) {
            return ((Builder)super.withValuesFrom(content));
        }

        public Builder withTemporal(Temporal temporal) {
            this.temporal = temporal;
            return this;
        }

		public WordPressLiveBlogContent build() {
            return new WordPressLiveBlogContent(getUuid(), getTitle(), getTitles(), getByline(),
                    getBrands(), getIdentifiers(), getPublishedDate(), getDescription(),
                    getMediaType(), getPixelWidth(), getPixelHeight(),
                    getInternalBinaryUrl(), getExternalBinaryUrl(),
                    getMainImage(), getComments(), temporal, getPublishReference());
        }
    }
}
