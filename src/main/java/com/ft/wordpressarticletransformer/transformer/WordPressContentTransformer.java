package com.ft.wordpressarticletransformer.transformer;

import com.ft.wordpressarticletransformer.exception.BrandResolutionException;
import com.ft.wordpressarticletransformer.exception.IdentifiersBuildException;
import com.ft.wordpressarticletransformer.exception.WordPressContentException;
import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.Comments;
import com.ft.wordpressarticletransformer.model.Identifier;
import com.ft.wordpressarticletransformer.model.WordPressContent;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.resources.IdentifierBuilder;
import com.ft.wordpressarticletransformer.response.Author;
import com.ft.wordpressarticletransformer.response.MainImage;
import com.ft.wordpressarticletransformer.response.Post;
import com.ft.wordpressarticletransformer.response.WordPressImage;
import com.ft.wordpressarticletransformer.util.ImageModelUuidGenerator;
import com.ft.wordpressarticletransformer.util.ImageSetUuidGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


public abstract class WordPressContentTransformer<C extends WordPressContent> {
    private static final Logger LOG = LoggerFactory.getLogger(WordPressContentTransformer.class);

    private static final DateTimeFormatter PUBLISH_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX");
    private static final String COMMENT_OPEN_STATUS = "open";

    private final BrandSystemResolver brandSystemResolver;
    private final IdentifierBuilder identifierBuilder;

    public WordPressContentTransformer(BrandSystemResolver brandSystemResolver, IdentifierBuilder identifierBuilder) {
        this.brandSystemResolver = brandSystemResolver;
        this.identifierBuilder = identifierBuilder;
    }

    public C transform(String transactionId, URI requestUri, Post post, UUID uuid, Date lastModified) {
        Date publishedDate = extractPublishedDate(requestUri, post);

        SortedSet<Brand> brands = new TreeSet<>(extractBrand(requestUri));

        SortedSet<Identifier> identifiers = generateIdentifiers(requestUri, post);

        LOG.info("Returning content for uuid [{}].", uuid);
        return doTransform(transactionId, post, uuid, publishedDate, brands, identifiers, lastModified);
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

    protected abstract C doTransform(String transactionId, Post post, UUID uuid, Date publishedDate,
                                     SortedSet<Brand> brands, SortedSet<Identifier> identifiers, Date lastModified);

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
        String publishedDateStr = null;
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

    protected String createBylineFromAuthors(Post postDetails) {
        Author singleAuthor = postDetails.getAuthor();
        List<Author> authorsList = postDetails.getAuthors();

        if (authorsList != null) {
            return authorsList.stream().map(i -> i.getName()).collect(Collectors.joining(", "));
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
      
      WordPressImage fullImage = img.getImages().get("full");
      if (fullImage == null) {
        LOG.warn("no full-size image for post {}", post.getUuid());
        return null;
      }
      
      String imageUrl = fullImage.getUrl();
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
