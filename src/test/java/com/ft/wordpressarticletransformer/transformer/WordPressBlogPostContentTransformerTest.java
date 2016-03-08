package com.ft.wordpressarticletransformer.transformer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import com.ft.wordpressarticletransformer.exception.UnpublishablePostException;
import com.ft.wordpressarticletransformer.exception.WordPressContentException;
import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.WordPressBlogPostContent;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.response.Author;
import com.ft.wordpressarticletransformer.response.MainImage;
import com.ft.wordpressarticletransformer.response.Post;
import com.ft.wordpressarticletransformer.response.WordPressImage;
import com.ft.wordpressarticletransformer.util.ImageModelUuidGenerator;
import com.ft.wordpressarticletransformer.util.ImageSetUuidGenerator;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


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
    private static final String IMAGE_URL = "http://www.example.com/images/junit.jpg";
    private static final Date LAST_MODIFIED = new Date();
    private static final Pattern EMBEDDED_IMAGE_REGEX = Pattern.compile("(.+)(<content.*</content>)(.+)");
    private static final String IMAGE_SET_TYPE = "http://www.ft.com/ontology/content/ImageSet";
    
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
        WordPressImage fullSizeImage = new WordPressImage();
        fullSizeImage.setUrl(IMAGE_URL);
        MainImage mainImage = new MainImage();
        mainImage.setImages(Collections.singletonMap("full", fullSizeImage));
        post.setMainImage(mainImage);
        
        UUID imageModelUuid = ImageModelUuidGenerator.fromURL(new URL(IMAGE_URL));
        String imageSetUuid = ImageSetUuidGenerator.fromImageUuid(imageModelUuid).toString();
        
        WordPressBlogPostContent actual = transformer.transform(TX_ID, REQUEST_URI, post, POST_UUID, LAST_MODIFIED);
        assertThat("title", actual.getTitle(), is(equalTo(TITLE)));
        assertThat("byline", actual.getByline(), is(equalTo(AUTHOR_NAME)));
        assertThat("brands", actual.getBrands(), hasItems(BRANDS.toArray(new Brand[BRANDS.size()])));
        
        checkBodyXml("body", WRAPPED_BODY, imageSetUuid, actual.getBody());
        checkBodyXml("opening", WRAPPED_BODY_OPENING, imageSetUuid, actual.getOpening());
        assertThat("identifier authority", actual.getIdentifiers().first().getAuthority(), is(equalTo(SYSTEM_ID)));
        assertThat("identifier value", actual.getIdentifiers().first().getIdentifierValue(), is(equalTo(POST_URL)));
        assertThat("uuid", actual.getUuid(), is(equalTo(POST_UUID.toString())));
        assertThat("comments", actual.getComments().isEnabled(), is(true));
        assertThat("featured image", actual.getMainImage(), equalTo(imageSetUuid));
        assertThat("publishedDate", actual.getPublishedDate().toInstant(), is(equalTo(PUBLISHED_DATE.toInstant())));
        assertThat("lastModified", actual.getLastModified(), is(equalTo(LAST_MODIFIED)));
        assertThat("publishReference", actual.getPublishReference(), is(equalTo(TX_ID)));
    }

    private void checkBodyXml(String fieldName, String expected, String imageSetUuid, String actual)
        throws Exception {
      
      Matcher m = EMBEDDED_IMAGE_REGEX.matcher(actual);
      String bodyMinusContent = m.replaceAll("$1$3");
      assertThat(fieldName, bodyMinusContent, is(equalTo(expected)));
      
      String embeddedImageContent = m.replaceAll("$2");
      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document content = db.parse(new InputSource(new StringReader(embeddedImageContent)));
      XPath xpath = XPathFactory.newInstance().newXPath();
      
      assertThat(fieldName + " embedded content image data tag", xpath.evaluate("/content/@data-embedded", content), equalTo("true"));
      assertThat(fieldName + " embedded content image id", xpath.evaluate("/content/@id", content), equalTo(imageSetUuid));
      assertThat(fieldName + " embedded content image type", xpath.evaluate("/content/@type", content), equalTo(IMAGE_SET_TYPE));
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
