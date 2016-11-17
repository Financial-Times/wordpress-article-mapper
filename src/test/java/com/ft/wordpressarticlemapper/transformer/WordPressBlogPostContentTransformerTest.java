package com.ft.wordpressarticlemapper.transformer;

import com.ft.wordpressarticlemapper.exception.UnpublishablePostException;
import com.ft.wordpressarticlemapper.exception.UntransformablePostException;
import com.ft.wordpressarticlemapper.model.Brand;
import com.ft.wordpressarticlemapper.model.Identifier;
import com.ft.wordpressarticlemapper.model.WordPressBlogPostContent;
import com.ft.wordpressarticlemapper.resources.BrandSystemResolver;
import com.ft.wordpressarticlemapper.resources.IdentifierBuilder;
import com.ft.wordpressarticlemapper.response.Author;
import com.ft.wordpressarticlemapper.response.MainImage;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.wordpressarticlemapper.response.WordPressImage;
import com.ft.wordpressarticlemapper.util.ImageModelUuidGenerator;
import com.ft.wordpressarticlemapper.util.ImageSetUuidGenerator;
import com.google.common.collect.ImmutableSortedSet;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class WordPressBlogPostContentTransformerTest {
    private static final String TX_ID = "junitTransaction";
    private static final String POST_URL = "http://junit.example.org/some-post/";
    private static final UUID POST_UUID = UUID.randomUUID();
    private static final OffsetDateTime PUBLISHED_DATE = OffsetDateTime.parse("2015-09-30T15:30:00.000Z");
    private static final String PUBLISHED_DATE_STR = PUBLISHED_DATE.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    private static final Set<Brand> BRANDS = Collections.singleton(new Brand("JUNIT-BLOG-BRAND"));
    private static final String SYSTEM_ID = "http://api.ft.com/system/JUNIT";
    private static final SortedSet<Identifier> IDENTIFIERS = ImmutableSortedSet.of(new Identifier(SYSTEM_ID, POST_URL));
    private static final String TITLE = "Test LiveBlog";
    private static final Author AUTHOR = new Author();
    private static final String AUTHOR_NAME = "John Smith";
    private static final String BODY_TEXT = "Some simple text";
    private static final String WRAPPED_BODY = "<body>" + BODY_TEXT + "</body>";
    private static final String BODY_OPENING = "Some";
    private static final String WRAPPED_BODY_OPENING = "<body>" + BODY_OPENING + "</body>";
    private static final String COMMENTS_OPEN = "open";
    private static final String IMAGE_URL = "http://www.example.com/images/junit.jpg";
    private static final Date LAST_MODIFIED = new Date();

    private WordPressBlogPostContentMapper mapper;
    private BrandSystemResolver brandResolver = mock(BrandSystemResolver.class);
    private BodyProcessingFieldTransformer bodyTransformer = mock(BodyProcessingFieldTransformer.class);
    private IdentifierBuilder identifierBuilder = mock(IdentifierBuilder.class);

    @Before
    public void setUp() {
        mapper = new WordPressBlogPostContentMapper(brandResolver, bodyTransformer, identifierBuilder);

        URI requestUri = UriBuilder.fromUri(POST_URL).build();
        when(brandResolver.getBrand(requestUri)).thenReturn(BRANDS);
        when(identifierBuilder.buildIdentifiers(eq(requestUri), any(Post.class))).thenReturn(IDENTIFIERS);
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
        post.setUuid(POST_UUID.toString());

        WordPressBlogPostContent actual = mapper.mapWordPressArticle(TX_ID, post, LAST_MODIFIED);
        assertThat("title", actual.getTitle(), is(equalTo(TITLE)));
        assertThat("byline", actual.getByline(), is(equalTo(AUTHOR_NAME)));
        assertThat("brands", actual.getBrands(), hasItems(BRANDS.toArray(new Brand[BRANDS.size()])));
        assertThat("body", actual.getBody(), is(equalTo(WRAPPED_BODY)));
        assertThat("opening", actual.getOpening(), is(equalTo(WRAPPED_BODY_OPENING)));
        assertThat("identifier authority", actual.getIdentifiers().first().getAuthority(), is(equalTo(SYSTEM_ID)));
        assertThat("identifier value", actual.getIdentifiers().first().getIdentifierValue(), is(equalTo(POST_URL)));
        assertThat("uuid", actual.getUuid(), is(equalTo(POST_UUID.toString())));
        assertThat("comments", actual.getComments().isEnabled(), is(true));
        assertThat("mainImage", actual.getMainImage(), is(nullValue()));
        assertThat("publishedDate", actual.getPublishedDate().toInstant(), is(equalTo(PUBLISHED_DATE.toInstant())));
        assertThat("lastModified", actual.getLastModified(), is(equalTo(LAST_MODIFIED)));
        assertThat("publishReference", actual.getPublishReference(), is(equalTo(TX_ID)));
    }

    @Test
    public void thatBlogPostWithFeaturedImageIsTransformed()
            throws Exception {

        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE_STR);
        post.setAuthors(Collections.singletonList(AUTHOR));
        post.setUrl(POST_URL);
        post.setContent(BODY_TEXT);
        post.setExcerpt(BODY_OPENING);
        post.setCommentStatus(COMMENTS_OPEN);
        post.setUuid(POST_UUID.toString());
        WordPressImage fullSizeImage = new WordPressImage();
        fullSizeImage.setUrl(IMAGE_URL);
        MainImage mainImage = new MainImage();
        mainImage.setImages(Collections.singletonMap("full", fullSizeImage));
        post.setMainImage(mainImage);

        UUID imageModelUuid = ImageModelUuidGenerator.fromURL(new URL(IMAGE_URL));
        String imageSetUuid = ImageSetUuidGenerator.fromImageUuid(imageModelUuid).toString();

        WordPressBlogPostContent actual = mapper.mapWordPressArticle(TX_ID, post, LAST_MODIFIED);
        assertThat("title", actual.getTitle(), is(equalTo(TITLE)));
        assertThat("byline", actual.getByline(), is(equalTo(AUTHOR_NAME)));
        assertThat("brands", actual.getBrands(), hasItems(BRANDS.toArray(new Brand[BRANDS.size()])));

        checkBodyXml("body", WRAPPED_BODY, actual.getBody());
        checkBodyXml("opening", WRAPPED_BODY_OPENING, actual.getOpening());
        assertThat("identifier authority", actual.getIdentifiers().first().getAuthority(), is(equalTo(SYSTEM_ID)));
        assertThat("identifier value", actual.getIdentifiers().first().getIdentifierValue(), is(equalTo(POST_URL)));
        assertThat("uuid", actual.getUuid(), is(equalTo(POST_UUID.toString())));
        assertThat("comments", actual.getComments().isEnabled(), is(true));
        assertThat("featured image", actual.getMainImage(), equalTo(imageSetUuid));
        assertThat("publishedDate", actual.getPublishedDate().toInstant(), is(equalTo(PUBLISHED_DATE.toInstant())));
        assertThat("lastModified", actual.getLastModified(), is(equalTo(LAST_MODIFIED)));
        assertThat("publishReference", actual.getPublishReference(), is(equalTo(TX_ID)));
    }

    private void checkBodyXml(String fieldName, String expected, String actual)
            throws Exception {

        assertThat(fieldName, actual, is(equalTo(expected)));
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
        post.setUuid(POST_UUID.toString());

        WordPressBlogPostContent actual = mapper.mapWordPressArticle(TX_ID, post, LAST_MODIFIED);

        assertThat("title", actual.getTitle(), is(equalTo(TITLE)));
        assertThat("byline", actual.getByline(), is(equalTo(AUTHOR_NAME)));
        assertThat("brands", actual.getBrands(), (org.hamcrest.Matcher) hasItems(BRANDS.toArray()));
        assertThat("body", actual.getBody(), is(equalTo(WRAPPED_BODY)));
        assertThat("identifier authority", actual.getIdentifiers().first().getAuthority(), is(equalTo(SYSTEM_ID)));
        assertThat("identifier value", actual.getIdentifiers().first().getIdentifierValue(), is(equalTo(POST_URL)));
        assertThat("uuid", actual.getUuid(), is(equalTo(POST_UUID.toString())));
        assertThat("comments", actual.getComments().isEnabled(), is(true));
        assertThat("publishedDate", actual.getPublishedDate().toInstant(), is(equalTo(PUBLISHED_DATE.toInstant())));
        assertThat("lastModified", actual.getLastModified(), is(equalTo(LAST_MODIFIED)));
        assertThat("publishReference", actual.getPublishReference(), is(equalTo(TX_ID)));
    }

    @Test
    public void thatTransformerAllowsPostWithNoAuthors() {
        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE_STR);
        post.setUrl(POST_URL);
        post.setContent(BODY_TEXT);
        post.setCommentStatus(COMMENTS_OPEN);
        post.setUuid(POST_UUID.toString());

        WordPressBlogPostContent actual = mapper.mapWordPressArticle(TX_ID, post, LAST_MODIFIED);

        assertThat("title", actual.getTitle(), is(equalTo(TITLE)));
        assertThat("byline", actual.getByline(), is(nullValue()));
        assertThat("brands", actual.getBrands(), (org.hamcrest.Matcher) hasItems(BRANDS.toArray()));
        assertThat("body", actual.getBody(), is(equalTo(WRAPPED_BODY)));
        assertThat("identifier authority", actual.getIdentifiers().first().getAuthority(), is(equalTo(SYSTEM_ID)));
        assertThat("identifier value", actual.getIdentifiers().first().getIdentifierValue(), is(equalTo(POST_URL)));
        assertThat("uuid", actual.getUuid(), is(equalTo(POST_UUID.toString())));
        assertThat("comments", actual.getComments().isEnabled(), is(true));
        assertThat("publishedDate", actual.getPublishedDate().toInstant(), is(equalTo(PUBLISHED_DATE.toInstant())));
        assertThat("lastModified", actual.getLastModified(), is(equalTo(LAST_MODIFIED)));
        assertThat("publishReference", actual.getPublishReference(), is(equalTo(TX_ID)));
    }

    @Test(expected = UnpublishablePostException.class)
    public void thatBlogPostRequiresBodyText() {
        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE_STR);
        post.setAuthors(Collections.singletonList(AUTHOR));
        post.setUrl(POST_URL);
        post.setCommentStatus(COMMENTS_OPEN);
        post.setUuid(POST_UUID.toString());

        mapper.mapWordPressArticle(TX_ID, post, LAST_MODIFIED);
    }

    @Test(expected = UntransformablePostException.class)
    public void thatBlogPostContainingOnlyUnsupportedTagsIsRejected() {
        String unsupportedBody = "<table><thead><tr><th>foo</th></tr></thead><tbody><tr><td>bar</td></tr></tbody></table>\n\n";
        when(bodyTransformer.transform("<body>" + unsupportedBody + "</body>", TX_ID)).thenReturn("<body>\n\n</body>");

        Post post = new Post();
        post.setTitle(TITLE);
        post.setContent(unsupportedBody);
        post.setDateGmt(PUBLISHED_DATE_STR);
        post.setAuthors(Collections.singletonList(AUTHOR));
        post.setUrl(POST_URL);
        post.setCommentStatus(COMMENTS_OPEN);
        post.setUuid(POST_UUID.toString());

        mapper.mapWordPressArticle(TX_ID, post, LAST_MODIFIED);
    }
}