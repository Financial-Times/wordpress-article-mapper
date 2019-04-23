package com.ft.wordpressarticlemapper.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ft.content.model.Standout;
import com.ft.content.model.Syndication;
import com.google.common.base.Objects;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;

public abstract class WordPressContent {

    private static final String TYPE_ARTICLE = "Article";

    private final String uuid;
    private final String title;
    private final List<String> titles;
    private final String byline;
//    private final SortedSet<Brand> brands;
    private final Date publishedDate;
    private final SortedSet<Identifier> identifiers;
    private final String description;
    private final String mediaType;
    private final Integer pixelWidth;
    private final Integer pixelHeight;
    private final String internalBinaryUrl;
    private final String externalBinaryUrl;
    private final String mainImage;
    private final Comments comments;
    private final String publishReference;
    private final Date lastModified;
    private final Date firstPublishedDate;
    private final AccessLevel accessLevel;
    private final String canBeDistributed;
    private final Syndication canBeSyndicated;
    private final String webUrl;
    private final String canonicalWebUrl;
    private final Standout standout;

    protected WordPressContent(UUID uuid,
                               String title,
                               List<String> titles,
                               String byline,
//                               SortedSet<Brand> brands,
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
                               String publishReference,
                               Date lastModified,
                               Date firstPublishedDate,
                               AccessLevel accessLevel,
                               String canBeDistributed,
                               Syndication canBeSyndicated,
                               String webUrl,
                               String canonicalWebUrl,
                               Standout standout) {
        this.identifiers = identifiers;
        this.comments = comments;
        this.uuid = uuid == null ? null : uuid.toString();
        this.title = title;
        this.titles = titles;
        this.byline = byline;
//        this.brands = brands;
        this.publishedDate = publishedDate;
        this.description = description;
        this.mediaType = mediaType;
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        this.internalBinaryUrl = internalBinaryUrl;
        this.externalBinaryUrl = externalBinaryUrl;
        this.mainImage = mainImage;
        this.publishReference = publishReference;
        this.lastModified = lastModified;
        this.firstPublishedDate = firstPublishedDate;
        this.accessLevel = accessLevel;
        this.canBeDistributed = canBeDistributed;
        this.canBeSyndicated = canBeSyndicated;
        this.webUrl = webUrl;
        this.canonicalWebUrl = canonicalWebUrl;
        this.standout = standout;
    }

    public String getUuid() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getTitles() {
        return titles;
    }

    public String getType() {
        return TYPE_ARTICLE;
    }

    public String getByline() {
        return byline;
    }

//    public SortedSet<Brand> getBrands() {
//        return brands;
//    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    public Date getPublishedDate() {
        return publishedDate;
    }

    public SortedSet<Identifier> getIdentifiers() {
        return identifiers;
    }

    public String getDescription() {
        return description;
    }

    public String getMediaType() {
        return mediaType;
    }

    public Integer getPixelWidth() {
        return pixelWidth;
    }

    public Integer getPixelHeight() {
        return pixelHeight;
    }

    public String getInternalBinaryUrl() {
        return internalBinaryUrl;
    }

    public String getExternalBinaryUrl() {
        return externalBinaryUrl;
    }

    public Object getMembers() {
        return null;
    }

    public String getMainImage() {
        return mainImage;
    }

    public Comments getComments() {
        return comments;
    }

    public String getPublishReference() {
        return publishReference;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    public Date getLastModified() {
        return lastModified;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    public Date getFirstPublishedDate() {
        return firstPublishedDate;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public String getCanBeDistributed() {
        return canBeDistributed;
    }

    public Syndication getCanBeSyndicated() {
        return canBeSyndicated;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getCanonicalWebUrl() {
        return canonicalWebUrl;
    }

    public Standout getStandout() {
        return standout;
    }


    @Override
    public String toString() {
        return Objects.toStringHelper(this.getClass())
                .add("uuid", uuid)
                .add("title", title)
                .add("type", getType())
                .add("byline", byline)
//                .add("brands", brands)
                .add("identifiers", identifiers)
                .add("publishedDate", publishedDate)
                .add("description", description)
                .add("mediaType", mediaType)
                .add("pixelWidth", pixelWidth)
                .add("pixelHeight", pixelHeight)
                .add("internalBinaryUrl", internalBinaryUrl)
                .add("externalBinaryUrl", externalBinaryUrl)
                .add("mainImage", mainImage)
                .add("comments", comments)
                .add("publishReference", publishReference)
                .add("lastModified", lastModified)
                .add("firstPublishedDate", firstPublishedDate)
                .add("accessLevel", accessLevel)
                .add("canBeDistributed", canBeDistributed)
                .add("canBeSyndicated", canBeSyndicated)
                .add("webUrl", webUrl)
                .add("canonicalWebUrl", canonicalWebUrl)
                .add("standout", standout)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WordPressContent that = (WordPressContent) o;

        return Objects.equal(this.uuid, that.uuid)
                && Objects.equal(this.title, that.title)
                && Objects.equal(this.byline, that.byline)
//                && Objects.equal(this.brands, that.brands)
                && Objects.equal(this.identifiers, that.identifiers)
                && Objects.equal(this.publishedDate, that.publishedDate)
                && Objects.equal(this.description, that.description)
                && Objects.equal(this.mediaType, that.mediaType)
                && Objects.equal(this.pixelWidth, that.pixelWidth)
                && Objects.equal(this.pixelHeight, that.pixelHeight)
                && Objects.equal(this.internalBinaryUrl, that.internalBinaryUrl)
                && Objects.equal(this.externalBinaryUrl, that.externalBinaryUrl)
                && Objects.equal(this.mainImage, that.mainImage)
                && Objects.equal(this.comments, that.comments)
                && Objects.equal(this.publishReference, that.publishReference)
                && Objects.equal(this.lastModified, that.lastModified)
                && Objects.equal(this.firstPublishedDate, that.firstPublishedDate)
                && Objects.equal(this.accessLevel, that.accessLevel)
                && Objects.equal(this.canBeDistributed, that.canBeDistributed)
                && Objects.equal(this.canBeSyndicated, that.canBeSyndicated)
                && Objects.equal(this.webUrl, that.webUrl)
                && Objects.equal(this.canonicalWebUrl, that.canonicalWebUrl)
                && Objects.equal(this.standout, that.standout);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(title, byline,
//                brands,
                identifiers, uuid, publishedDate,
                description, mediaType, pixelWidth, pixelHeight, internalBinaryUrl, externalBinaryUrl,
                mainImage, comments, publishReference, lastModified, firstPublishedDate, accessLevel, canBeDistributed,
                canBeSyndicated, webUrl, canonicalWebUrl, standout);
    }

    public abstract static class Builder<C extends WordPressContent> {

        private UUID uuid;
        private String title;
        private List<String> titles;
        private String byline;
//        private SortedSet<Brand> brands;
        private Date publishedDate;
        private SortedSet<Identifier> identifiers;
        private String description;
        private String mediaType;
        private Integer pixelWidth;
        private Integer pixelHeight;
        private String internalBinaryUrl;
        private String externalBinaryUrl;
        private String mainImage;
        private Comments comments;
        private String transactionId;
        private Date lastModified;
        private Date firstPublishedDate;
        private AccessLevel accessLevel;
        private String canBeDistributed;
        private Syndication canBeSyndicated;
        private String webUrl;
        private String canonicalWebUrl;
        private Standout standout;

        public Builder<C> withUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public UUID getUuid() {
            return uuid;
        }

        public Builder<C> withTitle(String title) {
            this.title = title;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Builder<C> withTitles(List<String> titles) {
            this.titles = titles;
            if (titles != null) {
                Collections.sort(titles, new LengthComparator());
            }
            return this;
        }

        public List<String> getTitles() {
            return titles;
        }

        public Builder<C> withByline(String byline) {
            this.byline = byline;
            return this;
        }

        public String getByline() {
            return byline;
        }

//        public Builder<C> withBrands(SortedSet<Brand> brands) {
//            this.brands = brands;
//            return this;
//        }

//        public SortedSet<Brand> getBrands() {
//            return brands;
//        }

        public Builder<C> withPublishedDate(Date publishedDate) {
            this.publishedDate = publishedDate;
            return this;
        }

        public Date getPublishedDate() {
            return publishedDate;
        }

        public Builder<C> withIdentifiers(SortedSet<Identifier> identifiers) {
            this.identifiers = identifiers;
            return this;
        }

        public SortedSet<Identifier> getIdentifiers() {
            return identifiers;
        }

        public Builder<C> withDescription(String description) {
            this.description = description;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public Builder<C> withMediaType(String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public String getMediaType() {
            return mediaType;
        }

        public Builder<C> withPixelWidth(Integer pixelWidth) {
            this.pixelWidth = pixelWidth;
            return this;
        }

        public Integer getPixelWidth() {
            return pixelWidth;
        }

        public Builder<C> withPixelHeight(Integer pixelHeight) {
            this.pixelHeight = pixelHeight;
            return this;
        }

        public Integer getPixelHeight() {
            return pixelHeight;
        }

        public Builder<C> withInternalBinaryUrl(String internalDataUrl) {
            this.internalBinaryUrl = internalDataUrl;
            return this;
        }

        public String getInternalBinaryUrl() {
            return internalBinaryUrl;
        }

        public Builder<C> withExternalBinaryUrl(String externalBinaryUrl) {
            this.externalBinaryUrl = externalBinaryUrl;
            return this;
        }

        public String getExternalBinaryUrl() {
            return externalBinaryUrl;
        }

        public Builder<C> withMembers(Object members) {
            // no-op
            return this;
        }

        public Builder<C> withMainImage(String mainImage) {
            this.mainImage = mainImage;
            return this;
        }

        public String getMainImage() {
            return mainImage;
        }

        public Builder<C> withComments(Comments comments) {
            this.comments = comments;
            return this;
        }

        public Comments getComments() {
            return comments;
        }

        public Builder<C> withPublishReference(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public String getPublishReference() {
            return transactionId;
        }

        public Builder<C> withLastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public AccessLevel getAccessLevel() {
            return accessLevel;
        }

        public Builder<C> withAccessLevel(AccessLevel accessLevel) {
            this.accessLevel = accessLevel;
            return this;
        }

        public Date getLastModified() {
            return lastModified;
        }

        public Builder<C> withFirstPublishedDate(Date firstPublishedDate) {
            this.firstPublishedDate = firstPublishedDate;
            return this;
        }

        public Date getFirstPublishedDate() {
            return firstPublishedDate;
        }

        public Builder<C> withCanBeDistributed(String canBeDistributed) {
            this.canBeDistributed = canBeDistributed;
            return this;
        }

        public String getCanBeDistributed() {
            return canBeDistributed;
        }

        public Builder<C> withWebUrl(String webUrl) {
            this.webUrl = webUrl;
            return this;
        }

        public Builder<C> withCanBeSyndicated(Syndication canBeSyndicated) {
            this.canBeSyndicated = canBeSyndicated;
            return this;
        }

        public Syndication getCanBeSyndicated() {
            return canBeSyndicated;
        }

        public String getWebUrl() {
            return webUrl;
        }

        public Builder<C> withStandout(Standout standout) {
            this.standout = standout;
            return this;
        }

        public Standout getStandout() {
            return standout;
        }

        public String getCanonicalWebUrl() {
            return canonicalWebUrl;
        }

        public Builder<C> withCanonicalWebUrl(String canonicalWebUrl) {
            this.canonicalWebUrl = canonicalWebUrl;
            return this;
        }

        public Builder<C> withValuesFrom(C content) {
            return withTitle(content.getTitle())
                    .withTitles(content.getTitles())
                    .withByline(content.getByline())
//                    .withBrands(content.getBrands())
                    .withIdentifiers(content.getIdentifiers())
                    .withUuid(UUID.fromString(content.getUuid()))
                    .withPublishedDate(content.getPublishedDate())
                    .withDescription(content.getDescription())
                    .withMediaType(content.getMediaType())
                    .withPixelWidth(content.getPixelWidth())
                    .withPixelHeight(content.getPixelHeight())
                    .withInternalBinaryUrl(content.getInternalBinaryUrl())
                    .withExternalBinaryUrl(content.getExternalBinaryUrl())
                    .withMainImage(content.getMainImage())
                    .withComments(content.getComments())
                    .withPublishReference(content.getPublishReference())
                    .withLastModified(content.getLastModified())
                    .withFirstPublishedDate(content.getFirstPublishedDate())
                    .withAccessLevel(content.getAccessLevel())
                    .withCanBeDistributed(content.getCanBeDistributed())
                    .withCanBeSyndicated(content.getCanBeSyndicated())
                    .withWebUrl(content.getWebUrl())
                    .withCanonicalWebUrl(content.getCanonicalWebUrl())
                    .withStandout(content.getStandout());
        }

        public abstract C build();
    }

    private static final class LengthComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.length() - o2.length();
        }
    }

}
