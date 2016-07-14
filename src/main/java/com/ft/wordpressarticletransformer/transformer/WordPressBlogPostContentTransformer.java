package com.ft.wordpressarticletransformer.transformer;

import com.ft.wordpressarticletransformer.exception.UnpublishablePostException;
import com.ft.wordpressarticletransformer.exception.UntransformablePostException;
import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.Identifier;
import com.ft.wordpressarticletransformer.model.WordPressBlogPostContent;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.resources.IdentifierBuilder;
import com.ft.wordpressarticletransformer.response.Post;

import com.google.common.base.Strings;

import java.util.Date;
import java.util.Objects;
import java.util.SortedSet;
import java.util.UUID;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;


public class WordPressBlogPostContentTransformer
        extends WordPressContentTransformer<WordPressBlogPostContent> {
    
  private static final String START_BODY = "<body>";
  private static final String END_BODY = "</body>";
  
    private final BodyProcessingFieldTransformer bodyProcessingFieldTransformer;
    
    public WordPressBlogPostContentTransformer(BrandSystemResolver brandSystemResolver,
                                               BodyProcessingFieldTransformer bodyProcessingFieldTransformer,
                                               IdentifierBuilder identifierBuilder) {

        super(brandSystemResolver, identifierBuilder);
        this.bodyProcessingFieldTransformer = bodyProcessingFieldTransformer;
    }
    
    @Override
    protected WordPressBlogPostContent doTransform(String transactionId, Post post, UUID uuid, Date publishedDate,
                                                   SortedSet<Brand> brands, SortedSet<Identifier> identifiers,
                                                   UUID featuredImageUuid, Date lastModified) {
        String body = post.getContent();
        if (Strings.isNullOrEmpty(body)) {
            throw new UnpublishablePostException(uuid.toString(), "Not a valid WordPress article for publication - body of post is empty");
        }
        body = wrapBody(body);
        
        WordPressBlogPostContent.Builder builder = (WordPressBlogPostContent.Builder)WordPressBlogPostContent.builder()
                .withUuid(uuid).withTitle(unescapeHtml4(post.getTitle()))
                .withPublishedDate(publishedDate)
                .withByline(unescapeHtml4(createBylineFromAuthors(post)))
                .withBrands(brands)
                .withIdentifiers(identifiers)
                .withComments(createComments(post.getCommentStatus()))
                .withMainImage(Objects.toString(featuredImageUuid, null))
                .withPublishReference(transactionId)
                .withLastModified(lastModified);
        
        String transformedBody = transformHtml(body, transactionId);
        if (Strings.isNullOrEmpty(unwrapBody(transformedBody))) {
          throw new UntransformablePostException(uuid.toString(), "Not a valid WordPress article for publication - body of transformed post is empty");
        }
        
        builder = builder.withBody(transformedBody)
                         .withOpening(transformHtml(wrapBody(post.getExcerpt()), transactionId));
        
        return builder.build();
    }
    
    private String transformHtml(String html, String transactionId) {
        String transformed = bodyProcessingFieldTransformer.transform(html, transactionId);
        return transformed;
    }
    
    private String wrapBody(String originalBody) {
        return START_BODY + originalBody + END_BODY;
    }
    
    private String unwrapBody(String wrappedBody) {
      if (!(wrappedBody.startsWith(START_BODY) && wrappedBody.endsWith(END_BODY))) {
        throw new IllegalArgumentException("can't unwrap a string that is not a wrapped body");
      }
      
      return wrappedBody.substring(START_BODY.length(), wrappedBody.length() - END_BODY.length()).trim();
    }
}
