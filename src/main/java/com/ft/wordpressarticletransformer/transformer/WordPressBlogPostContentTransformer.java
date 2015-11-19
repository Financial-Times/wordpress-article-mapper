package com.ft.wordpressarticletransformer.transformer;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;

import java.net.URI;
import java.util.Date;
import java.util.SortedSet;
import java.util.UUID;

import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.Identifier;
import com.ft.wordpressarticletransformer.model.WordPressBlogPostContent;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.resources.UnpublishablePostException;
import com.ft.wordpressarticletransformer.response.Post;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;


public class WordPressBlogPostContentTransformer
        extends WordPressContentTransformer<WordPressBlogPostContent> {
    
    private final BodyProcessingFieldTransformer bodyProcessingFieldTransformer;
    
    public WordPressBlogPostContentTransformer(BrandSystemResolver brandSystemResolver,
            BodyProcessingFieldTransformer bodyProcessingFieldTransformer) {
        
        super(brandSystemResolver);
        this.bodyProcessingFieldTransformer = bodyProcessingFieldTransformer;
    }
    
    @Override
    protected WordPressBlogPostContent doTransform(String transactionId, URI requestUri, Post post, UUID uuid, Date publishedDate, SortedSet<Brand> brands, String originatingSystemId) {
        String body = post.getContent();
        if (Strings.isNullOrEmpty(body)) {
            throw new UnpublishablePostException(requestUri, uuid, "Not a valid WordPress article for publication - body of post is empty");
        }
        body = wrapBody(body);
        
        WordPressBlogPostContent.Builder builder = (WordPressBlogPostContent.Builder)WordPressBlogPostContent.builder()
                .withUuid(uuid).withTitle(unescapeHtml4(post.getTitle()))
                .withPublishedDate(publishedDate)
                .withByline(unescapeHtml4(createBylineFromAuthors(post, requestUri)))
                .withBrands(brands)
                .withIdentifiers(ImmutableSortedSet.of(new Identifier(originatingSystemId, post.getUrl())))
                .withComments(createComments(post.getCommentStatus()));
        
        builder = builder.withBody(tidiedUpBody(body, transactionId));

        return builder.build();
    }
    
    private String tidiedUpBody(String body, String transactionId) {
        return bodyProcessingFieldTransformer.transform(body, transactionId);
    }
    
    private String wrapBody(String originalBody) {
        return "<body>" + originalBody + "</body>";
    }
}