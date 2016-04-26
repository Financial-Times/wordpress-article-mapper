package com.ft.wordpressarticletransformer.transformer;

import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.Identifier;
import com.ft.wordpressarticletransformer.model.WordPressLiveBlogContent;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.resources.IdentifierBuilder;
import com.ft.wordpressarticletransformer.response.Post;

import java.util.Date;
import java.util.SortedSet;
import java.util.UUID;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;


public class WordPressLiveBlogContentTransformer
        extends WordPressContentTransformer<WordPressLiveBlogContent> {

    public WordPressLiveBlogContentTransformer(BrandSystemResolver brandSystemResolver, IdentifierBuilder identifierBuilder) {
        super(brandSystemResolver, identifierBuilder);
    }
    
    @Override
    protected WordPressLiveBlogContent doTransform(String transactionId, Post post, UUID uuid, Date publishedDate,
                                                   SortedSet<Brand> brands, SortedSet<Identifier> identifiers, Date lastModified) {
        WordPressLiveBlogContent.Builder builder = (WordPressLiveBlogContent.Builder)WordPressLiveBlogContent.builder()
                .withUuid(uuid)
                .withIdentifiers(identifiers)
                .withTitle(unescapeHtml4(post.getTitle()))
                .withByline(unescapeHtml4(createBylineFromAuthors(post)))
                .withPublishedDate(publishedDate)
                .withBrands(brands)
                .withComments(createComments(post.getCommentStatus()))
                .withPublishReference(transactionId)
                .withLastModified(lastModified);
        
        return builder.build();
    }
}
