package com.ft.wordpressarticletransformer.transformer;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.Comments;
import com.ft.wordpressarticletransformer.model.WordPressContent;
import com.ft.wordpressarticletransformer.resources.BrandResolutionException;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.resources.WordPressApiException;
import com.ft.wordpressarticletransformer.response.Author;
import com.ft.wordpressarticletransformer.response.Post;
import com.google.common.collect.ImmutableSortedSet;


public abstract class WordPressContentTransformer<C extends WordPressContent> {
    private static final Logger LOG = LoggerFactory.getLogger(WordPressContentTransformer.class);
    
    private static final DateTimeFormatter PUBLISH_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX");
    private static final String COMMENT_OPEN_STATUS = "open";
    
    private final BrandSystemResolver brandSystemResolver;
    
    public WordPressContentTransformer(BrandSystemResolver brandSystemResolver) {
        this.brandSystemResolver = brandSystemResolver;
    }
    
    public C transform(String transactionId, URI requestUri, Post post, UUID uuid) {
        Date publishedDate = extractPublishedDate(requestUri, post);
        
        Brand brand = extractBrand(requestUri);
        SortedSet<Brand> brands = ImmutableSortedSet.of(brand);
        
        String originatingSystemId = extractSystemId(requestUri);
        
        LOG.info("Returning content for uuid [{}].", uuid);
        return doTransform(transactionId, requestUri, post, uuid, publishedDate, brands, originatingSystemId);
    }
    
    protected abstract C doTransform(String transactionId, URI requestUri, Post post, UUID uuid, Date publishedDate, SortedSet<Brand> brands, String originatingSystemId);
    
    private Brand extractBrand(URI requestUri) {
        Brand brand = brandSystemResolver.getBrand(requestUri);

        if(brand == null){
            String msg = String.format("Failed to resolve brand for uri [%s].", requestUri);
            LOG.error(msg);
            throw new BrandResolutionException(msg);
        }
        
        return brand;
    }
    
    private String extractSystemId(URI requestUri) {
        String originatingSystemId = brandSystemResolver.getOriginatingSystemId(requestUri);
        if(originatingSystemId == null){
            String msg = String.format("Failed to resolve originatingSystemId for uri [%s].", requestUri);
            LOG.error(msg);
            throw new BrandResolutionException(msg);
        }
        
        return originatingSystemId;
    }
    
    private Date extractPublishedDate(URI requestUri, Post post) {
        String publishedDateStr = null;
        if (post.getModifiedGmt() != null) {
            publishedDateStr = post.getModifiedGmt();
        }
        else if (post.getDateGmt() != null) {
            publishedDateStr = post.getDateGmt();
        }
        else {
            LOG.error("Modified and Date GMT fields not found for : " + requestUri);
            publishedDateStr = post.getModified();
        }
        
        return Date.from(OffsetDateTime.parse(publishedDateStr + "Z", PUBLISH_DATE_FMT).toInstant());
    }
    
    protected String createBylineFromAuthors(Post postDetails, URI requestUri) {
        Author singleAuthor = postDetails.getAuthor();
        List<Author> authorsList = postDetails.getAuthors();
        
        if (authorsList != null) {
            return authorsList.stream().map(i -> i.getName()).collect(Collectors.joining(", "));
        } else if (singleAuthor != null) {
            return singleAuthor.getName();
        }
        
        LOG.error("Failed to construct byline");
        throw new WordPressApiException("article has no authors", requestUri);
    }
    
    protected Comments createComments(String commentStatus) {
        return new Comments(COMMENT_OPEN_STATUS.equals(commentStatus));
    }
}
