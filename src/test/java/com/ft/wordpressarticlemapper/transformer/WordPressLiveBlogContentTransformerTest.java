package com.ft.wordpressarticlemapper.transformer;

import com.ft.wordpressarticlemapper.model.Brand;
import com.ft.wordpressarticlemapper.model.Identifier;
import com.ft.wordpressarticlemapper.model.WordPressLiveBlogContent;
import com.ft.wordpressarticlemapper.resources.BrandSystemResolver;
import com.ft.wordpressarticlemapper.resources.IdentifierBuilder;
import com.ft.wordpressarticlemapper.response.Author;
import com.ft.wordpressarticlemapper.response.MainImage;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.wordpressarticlemapper.response.WordPressImage;
import com.ft.wordpressarticlemapper.util.ImageModelUuidGenerator;
import com.ft.wordpressarticlemapper.util.ImageSetUuidGenerator;
import com.google.common.collect.ImmutableSortedSet;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class WordPressLiveBlogContentTransformerTest {
    private static final String TX_ID = "junitTransaction";
    private static final String POST_URL = "http://junit.example.org/some-post/";
    private static final UUID POST_UUID = UUID.randomUUID();
    private static final OffsetDateTime PUBLISHED_DATE = OffsetDateTime.parse("2015-09-30T15:30:00.000Z");
    private static final String PUBLISHED_DATE_STR = PUBLISHED_DATE.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    private static final Date LAST_MODIFIED = new Date();
    private static final Set<Brand> BRANDS = new HashSet<Brand>() {{
        add(new Brand("JUNIT-BLOG-BRAND"));
    }};
    private static final String SYSTEM_ID = "http://api.ft.com/system/JUNIT";
    private static final String TITLE = "Test LiveBlog";
    private static final Author AUTHOR = new Author();
    private static final String AUTHOR_NAME = "John Smith";
    private static final String COMMENTS_OPEN = "open";
    private static final SortedSet<Identifier> IDENTIFIERS = ImmutableSortedSet.of(new Identifier(SYSTEM_ID, POST_URL));
    private static final String IMAGE_URL = "http://www.example.com/images/junit.jpg";

    private WordPressLiveBlogContentMapper transformer;
    private BrandSystemResolver brandResolver = mock(BrandSystemResolver.class);
    private IdentifierBuilder identifierBuilder = mock(IdentifierBuilder.class);

    @Before
    public void setUp() {
        transformer = new WordPressLiveBlogContentMapper(brandResolver, identifierBuilder);

        URI requestUri = UriBuilder.fromUri(POST_URL).build();
        when(brandResolver.getBrand(requestUri)).thenReturn(BRANDS);
        when(identifierBuilder.buildIdentifiers(eq(requestUri), any(Post.class))).thenReturn(IDENTIFIERS);
        AUTHOR.setName(AUTHOR_NAME);
    }

    @Test
    public void thatLiveBlogPostIsTransformed() {
        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE_STR);
        post.setAuthors(Collections.singletonList(AUTHOR));
        post.setUrl(POST_URL);
        post.setCommentStatus(COMMENTS_OPEN);
        post.setUuid(POST_UUID.toString());

        WordPressLiveBlogContent actual = transformer.mapWordPressArticle(TX_ID, post, LAST_MODIFIED);

        assertThat("title", actual.getTitle(), is(equalTo(TITLE)));
        assertThat("byline", actual.getByline(), is(equalTo(AUTHOR_NAME)));
        assertThat("brands", actual.getBrands(), (Matcher) hasItems(BRANDS.toArray()));
        assertThat("identifier authority", actual.getIdentifiers().first().getAuthority(), is(equalTo(SYSTEM_ID)));
        assertThat("identifier value", actual.getIdentifiers().first().getIdentifierValue(), is(equalTo(POST_URL)));
        assertThat("uuid", actual.getUuid(), is(equalTo(POST_UUID.toString())));
        assertThat("realtime", actual.isRealtime(), is(true));
        assertThat("comments", actual.getComments().isEnabled(), is(true));
        assertThat("publishedDate", actual.getPublishedDate().toInstant(), is(equalTo(PUBLISHED_DATE.toInstant())));
        assertThat("lastModified", actual.getLastModified(), is(equalTo(LAST_MODIFIED)));
        assertThat("publishReference", actual.getPublishReference(), is(equalTo(TX_ID)));
    }

    @Test
    public void thatLiveBlogPostWithFeaturedImageIsTransformed()
            throws Exception {

        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE_STR);
        post.setAuthors(Collections.singletonList(AUTHOR));
        post.setUrl(POST_URL);
        post.setCommentStatus(COMMENTS_OPEN);
        post.setUuid(POST_UUID.toString());
        WordPressImage fullSizeImage = new WordPressImage();
        fullSizeImage.setUrl(IMAGE_URL);
        MainImage mainImage = new MainImage();
        mainImage.setImages(Collections.singletonMap("full", fullSizeImage));
        post.setMainImage(mainImage);

        UUID imageModelUuid = ImageModelUuidGenerator.fromURL(new URL(IMAGE_URL));
        String imageSetUuid = ImageSetUuidGenerator.fromImageUuid(imageModelUuid).toString();

        WordPressLiveBlogContent actual = transformer.mapWordPressArticle(TX_ID, post, LAST_MODIFIED);
        assertThat("title", actual.getTitle(), is(equalTo(TITLE)));
        assertThat("byline", actual.getByline(), is(equalTo(AUTHOR_NAME)));
        assertThat("brands", actual.getBrands(), hasItems(BRANDS.toArray(new Brand[BRANDS.size()])));

        assertThat("identifier authority", actual.getIdentifiers().first().getAuthority(), is(equalTo(SYSTEM_ID)));
        assertThat("identifier value", actual.getIdentifiers().first().getIdentifierValue(), is(equalTo(POST_URL)));
        assertThat("uuid", actual.getUuid(), is(equalTo(POST_UUID.toString())));
        assertThat("comments", actual.getComments().isEnabled(), is(true));
        assertThat("featured image", actual.getMainImage(), equalTo(imageSetUuid));
        assertThat("publishedDate", actual.getPublishedDate().toInstant(), is(equalTo(PUBLISHED_DATE.toInstant())));
        assertThat("lastModified", actual.getLastModified(), is(equalTo(LAST_MODIFIED)));
        assertThat("publishReference", actual.getPublishReference(), is(equalTo(TX_ID)));
    }
}
