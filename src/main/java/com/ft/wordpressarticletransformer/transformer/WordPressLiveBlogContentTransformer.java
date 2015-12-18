package com.ft.wordpressarticletransformer.transformer;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;

import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.Identifier;
import com.ft.wordpressarticletransformer.model.Temporal;
import com.ft.wordpressarticletransformer.model.WordPressLiveBlogContent;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.response.Post;
import com.google.common.collect.ImmutableSortedSet;


public class WordPressLiveBlogContentTransformer
        extends WordPressContentTransformer<WordPressLiveBlogContent> {
    
    public WordPressLiveBlogContentTransformer(BrandSystemResolver brandSystemResolver) {
        super(brandSystemResolver);
    }
    
    @Override
    protected WordPressLiveBlogContent doTransform(String transactionId, Post post, UUID uuid, Date publishedDate, SortedSet<Brand> brands, String originatingSystemId) {

        boolean isInProgress = true;
        if(post.getCustomFields()!=null) {
            List<String> closedness = post.getCustomFields().getClosed();
            if(closedness!=null && !closedness.isEmpty()) {
                isInProgress = !closedness.contains("1");
            }
        }

        return WordPressLiveBlogContent.buildFrom(new Temporal(true, isInProgress))
                .withUuid(uuid)
                .withIdentifiers(ImmutableSortedSet.of(new Identifier(originatingSystemId, post.getUrl())))
                .withTitle(unescapeHtml4(post.getTitle()))
                .withByline(unescapeHtml4(createBylineFromAuthors(post)))
                .withPublishedDate(publishedDate)
                .withBrands(brands)
                .withComments(createComments(post.getCommentStatus())).build();

    }
}
