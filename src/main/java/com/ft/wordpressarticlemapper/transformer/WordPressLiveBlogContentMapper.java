package com.ft.wordpressarticlemapper.transformer;

import com.ft.wordpressarticlemapper.model.Brand;
import com.ft.wordpressarticlemapper.model.Identifier;
import com.ft.wordpressarticlemapper.model.WordPressLiveBlogContent;
import com.ft.wordpressarticlemapper.resources.BrandSystemResolver;
import com.ft.wordpressarticlemapper.resources.IdentifierBuilder;
import com.ft.wordpressarticlemapper.response.Post;

import java.util.Date;
import java.util.Objects;
import java.util.SortedSet;
import java.util.UUID;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;


public class WordPressLiveBlogContentMapper
        extends WordPressContentMapper<WordPressLiveBlogContent> {

    public WordPressLiveBlogContentMapper(BrandSystemResolver brandSystemResolver, IdentifierBuilder identifierBuilder) {
        super(brandSystemResolver, identifierBuilder);
    }
    
    @Override
    protected WordPressLiveBlogContent doTransform(String transactionId, Post post, UUID uuid, Date publishedDate,
                                                   SortedSet<Brand> brands, SortedSet<Identifier> identifiers,
                                                   UUID featuredImageUuid, Date lastModified) {
      
        WordPressLiveBlogContent.Builder builder = (WordPressLiveBlogContent.Builder)WordPressLiveBlogContent.builder()
                .withUuid(uuid)
                .withIdentifiers(identifiers)
                .withTitle(unescapeHtml4(post.getTitle()))
                .withByline(unescapeHtml4(createBylineFromAuthors(post)))
                .withPublishedDate(publishedDate)
                .withBrands(brands)
                .withComments(createComments(post.getCommentStatus()))
                .withMainImage(Objects.toString(featuredImageUuid, null))
                .withPublishReference(transactionId)
                .withLastModified(lastModified);
        
        return builder.build();
    }
}
