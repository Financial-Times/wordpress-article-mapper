package com.ft.wordpressarticletransformer.transformer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.ft.wordpressarticletransformer.exception.UnpublishablePostException;
import com.ft.wordpressarticletransformer.exception.WordPressContentException;
import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.WordPressBlogPostContent;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.response.Author;
import com.ft.wordpressarticletransformer.response.Post;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;


public class WordPressBlogPostContentTransformerTest {
    private static final String TX_ID = "junitTransaction";
    private static final URI REQUEST_URI = URI.create("http://junit.example.org/");
    private static final String POST_URL = "http://junit.example.org/some-post/";
    private static final UUID POST_UUID = UUID.randomUUID();
    private static final OffsetDateTime PUBLISHED_DATE = OffsetDateTime.parse("2015-09-30T15:30:00.000Z");
    private static final String PUBLISHED_DATE_STR = PUBLISHED_DATE.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    private static final Set<Brand> BRANDS = Collections.singleton(new Brand("JUNIT-BLOG-BRAND"));
    private static final String SYSTEM_ID = "http://api.ft.com/system/JUNIT";
    private static final String TITLE = "Test LiveBlog";
    private static final Author AUTHOR = new Author();
    private static final String AUTHOR_NAME = "John Smith";
    private static final String BODY_TEXT = "Some simple text";
    private static final String WRAPPED_BODY = "<body>" + BODY_TEXT + "</body>";
    private static final String BODY_OPENING = "Some";
    private static final String WRAPPED_BODY_OPENING = "<body>" + BODY_OPENING + "</body>";
    private static final String COMMENTS_OPEN = "open";
    private static final Date LAST_MODIFIED = new Date();

    private WordPressBlogPostContentTransformer transformer;
    private BrandSystemResolver brandResolver = mock(BrandSystemResolver.class);
    private BodyProcessingFieldTransformer bodyTransformer = mock(BodyProcessingFieldTransformer.class);

    @Before
    public void setUp() {
        transformer = new WordPressBlogPostContentTransformer(brandResolver, bodyTransformer);

        when(brandResolver.getBrand(REQUEST_URI)).thenReturn(BRANDS);
        when(brandResolver.getOriginatingSystemId(REQUEST_URI)).thenReturn(SYSTEM_ID);
        AUTHOR.setName(AUTHOR_NAME);

        when(bodyTransformer.transform(WRAPPED_BODY, TX_ID)).thenReturn(WRAPPED_BODY);
        when(bodyTransformer.transform(WRAPPED_BODY_OPENING, TX_ID)).thenReturn(WRAPPED_BODY_OPENING);
    }

    @Test
    public void thatBlogPostIsTransformed() {
        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE_STR);
        post.setAuthors(Collections.singletonList(AUTHOR));
        post.setUrl(POST_URL);
        post.setContent(BODY_TEXT);
        post.setExcerpt(BODY_OPENING);
        post.setCommentStatus(COMMENTS_OPEN);

        WordPressBlogPostContent actual = transformer.transform(TX_ID, REQUEST_URI, post, POST_UUID, LAST_MODIFIED);
        assertThat("title", actual.getTitle(), is(equalTo(TITLE)));
        assertThat("byline", actual.getByline(), is(equalTo(AUTHOR_NAME)));
        assertThat("brands", actual.getBrands(), hasItems(BRANDS.toArray(new Brand[BRANDS.size()])));
        assertThat("body", actual.getBody(), is(equalTo(WRAPPED_BODY)));
        assertThat("opening", actual.getOpening(), is(equalTo(WRAPPED_BODY_OPENING)));
        assertThat("identifier authority", actual.getIdentifiers().first().getAuthority(), is(equalTo(SYSTEM_ID)));
        assertThat("identifier value", actual.getIdentifiers().first().getIdentifierValue(), is(equalTo(POST_URL)));
        assertThat("uuid", actual.getUuid(), is(equalTo(POST_UUID.toString())));
        assertThat("comments", actual.getComments().isEnabled(), is(true));
        assertThat("publishedDate", actual.getPublishedDate().toInstant(), is(equalTo(PUBLISHED_DATE.toInstant())));
        assertThat("lastModified", actual.getLastModified(), is(equalTo(LAST_MODIFIED)));
        assertThat("publishReference", actual.getPublishReference(), is(equalTo(TX_ID)));
    }

    @Deprecated
    @Test
    public void thatAuthorCanBeUsedInsteadOfAuthors() {
        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE_STR);
        post.setAuthor(AUTHOR);
        post.setUrl(POST_URL);
        post.setContent(BODY_TEXT);
        post.setCommentStatus(COMMENTS_OPEN);

        WordPressBlogPostContent actual = transformer.transform(TX_ID, REQUEST_URI, post, POST_UUID, LAST_MODIFIED);

        assertThat("title", actual.getTitle(), is(equalTo(TITLE)));
        assertThat("byline", actual.getByline(), is(equalTo(AUTHOR_NAME)));
        assertThat("brands", actual.getBrands(), (Matcher) hasItems(BRANDS.toArray()));
        assertThat("body", actual.getBody(), is(equalTo(WRAPPED_BODY)));
        assertThat("identifier authority", actual.getIdentifiers().first().getAuthority(), is(equalTo(SYSTEM_ID)));
        assertThat("identifier value", actual.getIdentifiers().first().getIdentifierValue(), is(equalTo(POST_URL)));
        assertThat("uuid", actual.getUuid(), is(equalTo(POST_UUID.toString())));
        assertThat("comments", actual.getComments().isEnabled(), is(true));
        assertThat("publishedDate", actual.getPublishedDate().toInstant(), is(equalTo(PUBLISHED_DATE.toInstant())));
        assertThat("lastModified", actual.getLastModified(), is(equalTo(LAST_MODIFIED)));
        assertThat("publishReference", actual.getPublishReference(), is(equalTo(TX_ID)));
    }

    @Test(expected = WordPressContentException.class)
    public void thatTransformerFailsWhenThereAreNoAuthors() {
        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE_STR);
        post.setUrl(POST_URL);
        post.setContent(BODY_TEXT);
        post.setCommentStatus(COMMENTS_OPEN);

        transformer.transform(TX_ID, REQUEST_URI, post, POST_UUID, LAST_MODIFIED);
    }

    @Test(expected = UnpublishablePostException.class)
    public void thatBlogPostRequiresBodyText() {
        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE_STR);
        post.setAuthors(Collections.singletonList(AUTHOR));
        post.setUrl(POST_URL);
        post.setCommentStatus(COMMENTS_OPEN);

        transformer.transform(TX_ID, REQUEST_URI, post, POST_UUID, LAST_MODIFIED);
    }
}
