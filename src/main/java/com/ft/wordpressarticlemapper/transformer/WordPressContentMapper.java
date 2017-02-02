package com.ft.wordpressarticlemapper.transformer;

import com.ft.wordpressarticlemapper.exception.BrandResolutionException;
import com.ft.wordpressarticlemapper.exception.IdentifiersBuildException;
import com.ft.wordpressarticlemapper.exception.WordPressContentException;
import com.ft.wordpressarticlemapper.model.Brand;
import com.ft.wordpressarticlemapper.model.Comments;
import com.ft.wordpressarticlemapper.model.Identifier;
import com.ft.wordpressarticlemapper.model.WordPressContent;
import com.ft.wordpressarticlemapper.resources.BrandSystemResolver;
import com.ft.wordpressarticlemapper.resources.IdentifierBuilder;
import com.ft.wordpressarticlemapper.response.Author;
import com.ft.wordpressarticlemapper.response.MainImage;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.wordpressarticlemapper.util.ImageModelUuidGenerator;
import com.ft.wordpressarticlemapper.util.ImageSetUuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;


public abstract class WordPressContentMapper<C extends WordPressContent> {
    private static final Logger LOG = LoggerFactory.getLogger(WordPressContentMapper.class);

    private static final DateTimeFormatter PUBLISH_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX");
    private static final String COMMENT_OPEN_STATUS = "open";

    private final BrandSystemResolver brandSystemResolver;
    private final IdentifierBuilder identifierBuilder;

    public WordPressContentMapper(BrandSystemResolver brandSystemResolver, IdentifierBuilder identifierBuilder) {
        this.brandSystemResolver = brandSystemResolver;
        this.identifierBuilder = identifierBuilder;
    }

    public C mapWordPressArticle(String transactionId, Post post, Date lastModified) {
        String postUrl = post.getUrl();
        if (postUrl == null) {
            throw new IllegalArgumentException("No post Url supplied");
        }
        URI requestUri = UriBuilder.fromUri(postUrl).build();

        Date publishedDate = extractPublishedDate(requestUri, post);

        SortedSet<Brand> brands = new TreeSet<>(extractBrand(requestUri));

        SortedSet<Identifier> identifiers = generateIdentifiers(requestUri, post);
        UUID featuredImageUuid = createMainImageUuid(post);

        UUID uuid = UUID.fromString(post.getUuid());

        Date firstPublishedDate = extractFirstPublishedDate(requestUri, post);

        LOG.info("Returning content for uuid [{}].", uuid);
        return doMapping(transactionId, post, uuid, publishedDate, brands, identifiers,
                featuredImageUuid, lastModified, firstPublishedDate);
    }

    private SortedSet<Identifier> generateIdentifiers(URI requestUri, Post post) {
        SortedSet<Identifier> identifiers = identifierBuilder.buildIdentifiers(requestUri, post);
        if (identifiers == null) {
            String msg = String.format("Failed to build identifiers for uri [%s].", requestUri);
            LOG.error(msg);
            throw new IdentifiersBuildException(msg);
        }
        return identifiers;
    }

    protected abstract C doMapping(String transactionId, Post post, UUID uuid, Date publishedDate,
                                   SortedSet<Brand> brands, SortedSet<Identifier> identifiers,
                                   UUID featuredImageUuid, Date lastModified, Date firstPublishedDate);

    private Set<Brand> extractBrand(URI requestUri) {
        Set<Brand> brand = brandSystemResolver.getBrand(requestUri);

        if (brand == null) {
            String msg = String.format("Failed to resolve brand for uri [%s].", requestUri);
            LOG.error(msg);
            throw new BrandResolutionException(msg);
        }

        return brand;
    }

    private Date extractPublishedDate(URI requestUri, Post post) {
        String publishedDateStr;
        if (post.getDateGmt() != null) {
            publishedDateStr = post.getDateGmt();
        } else if (post.getModifiedGmt() != null) {
            publishedDateStr = post.getModifiedGmt();
        } else {
            LOG.error("Modified and Date GMT fields not found for : " + requestUri);
            publishedDateStr = post.getModified();
        }

        return Date.from(OffsetDateTime.parse(publishedDateStr + "Z", PUBLISH_DATE_FMT).toInstant());
    }

    private Date extractFirstPublishedDate(URI requestUri, Post post) {
        String firstPublishedDateStr = post.getDateGmt();
        if (firstPublishedDateStr == null) {
            LOG.info("First published date cannot be determined as date GMT field is empty. Request URI: " + requestUri);
            return null;
        }
        return Date.from(OffsetDateTime.parse(firstPublishedDateStr + "Z", PUBLISH_DATE_FMT).toInstant());
    }

    protected String createBylineFromAuthors(Post postDetails) {
        Author singleAuthor = postDetails.getAuthor();
        List<Author> authorsList = postDetails.getAuthors();

        if (authorsList != null) {
            return authorsList.stream().map(Author::getName).collect(Collectors.joining(", "));
        } else if (singleAuthor != null) {
            return singleAuthor.getName();
        }

        LOG.warn("Failed to construct byline - article has no authors");
        return null;
    }

    protected Comments createComments(String commentStatus) {
        return new Comments(COMMENT_OPEN_STATUS.equals(commentStatus));
    }

    protected UUID createMainImageUuid(Post post) {
        MainImage img = post.getMainImage();
        if (img == null) {
            LOG.debug("no main image for post {}", post.getUuid());
            return null;
        }

        String imageUrl = img.getUrl();
        try {
            URL u = new URL(imageUrl);
            UUID imageModelUuid = ImageModelUuidGenerator.fromURL(u);
            return ImageSetUuidGenerator.fromImageUuid(imageModelUuid);
        } catch (MalformedURLException e) {
            LOG.error("unable to construct UUID for featured image", e);
            throw new WordPressContentException("unable to construct UUID for featured image", e);
        }
    }
}
