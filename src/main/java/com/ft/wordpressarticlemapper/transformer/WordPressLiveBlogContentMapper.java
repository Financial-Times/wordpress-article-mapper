package com.ft.wordpressarticlemapper.transformer;

import com.ft.content.model.Syndication;
import com.ft.wordpressarticlemapper.model.AccessLevel;
import com.ft.wordpressarticlemapper.model.Brand;
import com.ft.wordpressarticlemapper.model.Identifier;
import com.ft.wordpressarticlemapper.model.WordPressLiveBlogContent;
import com.ft.wordpressarticlemapper.resources.IdentifierBuilder;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.content.model.Standout;

import java.util.Date;
import java.util.Objects;
import java.util.SortedSet;
import java.util.UUID;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;


public class WordPressLiveBlogContentMapper
        extends WordPressContentMapper<WordPressLiveBlogContent> {

    public WordPressLiveBlogContentMapper(IdentifierBuilder identifierBuilder,
                                          SyndicationManager syndicationManager,
                                          String canonicalWebUrlTemplate) {
        super(identifierBuilder, syndicationManager, canonicalWebUrlTemplate);
    }

    @Override
    protected WordPressLiveBlogContent doMapping(String transactionId, Post post, UUID uuid, Date publishedDate,
                                                 SortedSet<Identifier> identifiers,
                                                 UUID featuredImageUuid, Date lastModified, Date firstPublishedDate,
                                                 AccessLevel accessLevel, String canBeDistributed,
                                                 Syndication canBeSyndicated, String webUrl, String canonicalWebUrl,
                                                 Standout standout) {

        WordPressLiveBlogContent.Builder builder = (WordPressLiveBlogContent.Builder) WordPressLiveBlogContent.builder()
                .withUuid(uuid)
                .withIdentifiers(identifiers)
                .withTitle(unescapeHtml4(post.getTitle()))
                .withByline(unescapeHtml4(createBylineFromAuthors(post)))
                .withPublishedDate(publishedDate)
                .withComments(createComments(post.getCommentStatus()))
                .withMainImage(Objects.toString(featuredImageUuid, null))
                .withPublishReference(transactionId)
                .withLastModified(lastModified)
                .withFirstPublishedDate(firstPublishedDate)
                .withAccessLevel(accessLevel)
                .withCanBeDistributed(canBeDistributed)
                .withCanBeSyndicated(canBeSyndicated)
                .withWebUrl(webUrl)
                .withCanonicalWebUrl(canonicalWebUrl)
                .withStandout(standout);

        return builder.build();
    }
}
