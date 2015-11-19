package com.ft.wordpressarticletransformer.transformer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.WordPressBlogPostContent;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.resources.UnpublishablePostException;
import com.ft.wordpressarticletransformer.resources.WordPressApiException;
import com.ft.wordpressarticletransformer.response.Author;
import com.ft.wordpressarticletransformer.response.Post;


public class WordPressBlogPostContentTransformerTest {
    private static final String TX_ID = "junitTransaction";
    private static final URI REQUEST_URI = URI.create("http://junit.example.org/");
    private static final String POST_URL = "http://junit.example.org/some-post/";
    private static final UUID POST_UUID = UUID.randomUUID();
    private static final String PUBLISHED_DATE = "2015-09-30 15:30:00";
    private static final Brand BRAND = new Brand("JUNIT-BLOG-BRAND");
    private static final String SYSTEM_ID = "http://api.ft.com/system/JUNIT";
    private static final String TITLE = "Test LiveBlog";
    private static final Author AUTHOR = new Author();
    private static final String AUTHOR_NAME = "John Smith";
    private static final String BODY_TEXT = "Some simple text";
    private static final String WRAPPED_BODY = "<body>" + BODY_TEXT + "</body>";
    private static final String COMMENTS_OPEN = "open";
    
    private WordPressBlogPostContentTransformer transformer;
    private BrandSystemResolver brandResolver = mock(BrandSystemResolver.class);
    private BodyProcessingFieldTransformer bodyTransformer = mock(BodyProcessingFieldTransformer.class);
    
    @Before
    public void setUp() {
        transformer = new WordPressBlogPostContentTransformer(brandResolver, bodyTransformer);
        
        when(brandResolver.getBrand(REQUEST_URI)).thenReturn(BRAND);
        when(brandResolver.getOriginatingSystemId(REQUEST_URI)).thenReturn(SYSTEM_ID);
        AUTHOR.setName(AUTHOR_NAME);
        
        when(bodyTransformer.transform(WRAPPED_BODY, TX_ID)).thenReturn(WRAPPED_BODY);
    }
    
    @Test
    public void thatBlogPostIsTransformed() {
        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE);
        post.setAuthors(Collections.singletonList(AUTHOR));
        post.setUrl(POST_URL);
        post.setContent(BODY_TEXT);
        post.setCommentStatus(COMMENTS_OPEN);
        
        WordPressBlogPostContent actual = transformer.transform(TX_ID, REQUEST_URI, post, POST_UUID);
        
        assertThat("title", actual.getTitle(), is(equalTo(TITLE)));
        assertThat("byline", actual.getByline(), is(equalTo(AUTHOR_NAME)));
        assertThat("brands", actual.getBrands(), hasItem(BRAND));
        assertThat("body", actual.getBody(), is(equalTo(WRAPPED_BODY)));
        assertThat("identifier authority", actual.getIdentifiers().first().getAuthority(), is(equalTo(SYSTEM_ID)));
        assertThat("identifier value", actual.getIdentifiers().first().getIdentifierValue(), is(equalTo(POST_URL)));
        assertThat("uuid", actual.getUuid(), is(equalTo(POST_UUID.toString())));
        assertThat("comments", actual.getComments().isEnabled(), is(true));
    }
    
    @Deprecated @Test
    public void thatAuthorCanBeUsedInsteadOfAuthors() {
        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE);
        post.setAuthor(AUTHOR);
        post.setUrl(POST_URL);
        post.setContent(BODY_TEXT);
        post.setCommentStatus(COMMENTS_OPEN);
        
        WordPressBlogPostContent actual = transformer.transform(TX_ID, REQUEST_URI, post, POST_UUID);
        
        assertThat("title", actual.getTitle(), is(equalTo(TITLE)));
        assertThat("byline", actual.getByline(), is(equalTo(AUTHOR_NAME)));
        assertThat("brands", actual.getBrands(), hasItem(BRAND));
        assertThat("body", actual.getBody(), is(equalTo(WRAPPED_BODY)));
        assertThat("identifier authority", actual.getIdentifiers().first().getAuthority(), is(equalTo(SYSTEM_ID)));
        assertThat("identifier value", actual.getIdentifiers().first().getIdentifierValue(), is(equalTo(POST_URL)));
        assertThat("uuid", actual.getUuid(), is(equalTo(POST_UUID.toString())));
        assertThat("comments", actual.getComments().isEnabled(), is(true));
    }
    
    @Test(expected = WordPressApiException.class)
    public void thatTransformerFailsWhenThereAreNoAuthors() {
        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE);
        post.setUrl(POST_URL);
        post.setContent(BODY_TEXT);
        post.setCommentStatus(COMMENTS_OPEN);
        
        transformer.transform(TX_ID, REQUEST_URI, post, POST_UUID);
    }
    
    @Test(expected = UnpublishablePostException.class)
    public void thatBlogPostRequiresBodyText() {
        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE);
        post.setAuthors(Collections.singletonList(AUTHOR));
        post.setUrl(POST_URL);
        post.setCommentStatus(COMMENTS_OPEN);
        
        transformer.transform(TX_ID, REQUEST_URI, post, POST_UUID);
    }
}