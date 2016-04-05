package com.ft.wordpressarticletransformer.transformer;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;

import java.util.Date;
import java.util.Objects;
import java.util.SortedSet;
import java.util.UUID;

import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.Identifier;
import com.ft.wordpressarticletransformer.model.WordPressBlogPostContent;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.exception.UnpublishablePostException;
import com.ft.wordpressarticletransformer.response.Post;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;


public class WordPressBlogPostContentTransformer
        extends WordPressContentTransformer<WordPressBlogPostContent> {
    
  private static final String START_BODY = "<body>";
  private static final String MAIN_IMAGE_XML =
      "<content data-embedded=\"true\" id=\"%s\" type=\"http://www.ft.com/ontology/content/ImageSet\"></content>";
  
    private final BodyProcessingFieldTransformer bodyProcessingFieldTransformer;
    
    public WordPressBlogPostContentTransformer(BrandSystemResolver brandSystemResolver,
            BodyProcessingFieldTransformer bodyProcessingFieldTransformer) {
        
        super(brandSystemResolver);
        this.bodyProcessingFieldTransformer = bodyProcessingFieldTransformer;
    }
    
    @Override
    protected WordPressBlogPostContent doTransform(String transactionId, Post post, UUID uuid, Date publishedDate, 
                                                   SortedSet<Brand> brands, String originatingSystemId, Date lastModified) {
        String body = post.getContent();
        if (Strings.isNullOrEmpty(body)) {
            throw new UnpublishablePostException(uuid.toString(), "Not a valid WordPress article for publication - body of post is empty");
        }
        body = wrapBody(body);
        UUID featuredImageUuid = createMainImageUuid(post);
        
        WordPressBlogPostContent.Builder builder = (WordPressBlogPostContent.Builder)WordPressBlogPostContent.builder()
                .withUuid(uuid).withTitle(unescapeHtml4(post.getTitle()))
                .withPublishedDate(publishedDate)
                .withByline(unescapeHtml4(createBylineFromAuthors(post)))
                .withBrands(brands)
                .withIdentifiers(ImmutableSortedSet.of(new Identifier(originatingSystemId, post.getUrl())))
                .withComments(createComments(post.getCommentStatus()))
//                .withMainImage(Objects.toString(featuredImageUuid, null))
                .withPublishReference(transactionId)
                .withLastModified(lastModified);
        
        builder = builder.withBody(transformHtml(body, featuredImageUuid, transactionId))
                         .withOpening(transformHtml(wrapBody(post.getExcerpt()), featuredImageUuid, transactionId));
        
        return builder.build();
    }
    
    private String transformHtml(String html, UUID featuredImageUuid, String transactionId) {
        String transformed = bodyProcessingFieldTransformer.transform(html, transactionId);
        /*if (featuredImageUuid != null) {
          int i = transformed.indexOf(START_BODY) + START_BODY.length();
          transformed = transformed.substring(0,  i)
              + String.format(MAIN_IMAGE_XML, featuredImageUuid)
              + transformed.substring(i);
        }*/
        return transformed;
    }
    
    private String wrapBody(String originalBody) {
        return START_BODY + originalBody + "</body>";
    }
}
