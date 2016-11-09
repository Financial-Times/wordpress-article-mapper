package com.ft.wordpressarticlemapper.transformer;

import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.wordpressarticlemapper.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticlemapper.resources.BlogApiEndpointMetadata;
import com.ft.wordpressarticlemapper.util.ClientMockBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.client.Client;

import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import ch.qos.logback.classic.Logger;

import static com.ft.wordpressarticlemapper.transformer.LoggingTestHelper.assertLogEvent;
import static com.ft.wordpressarticlemapper.transformer.LoggingTestHelper.configureMockAppenderFor;
import static com.ft.wordpressarticlemapper.transformer.LoggingTestHelper.resetLoggingFor;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class LinkResolverBodyProcessorTest {
    static final String ARTICLE_TYPE = "http://www.ft.com/ontology/content/Article";

    private static final Pattern SHORT_URL_PATTERN = Pattern.compile("http:\\/\\/short\\.example\\.com\\/.*");
    private static final String FULL_WORDPRESS_URL = "http://something.somethingelse.ft.com/ablog/2016/04/29/the-title-of-the-post/";
    private static final String BRAND_ID = "http://api.ft.com/system/JUNIT";
    private static final String BLOG_ID = "FT-LABS-WP-Y-XXX";
    private static final String BLOG_AUTHORITY = "http://api.ft.com/system/" + BLOG_ID;
    private static final URI DOC_STORE_URI = URI.create("http://localhost:8080/");
    private static final URI DOC_STORE_QUERY = DOC_STORE_URI.resolve("/content-query");
    private static final URI CONTENT_READ_URI = URI.create("http://localhost:8080/content");
    private static final String CONTENT_READ_HOST_HEADER = "content-public-read";
    private static final String DOC_STORE_HOST_HEADER = "document-store-api";

    private Client resolverClient = mock(Client.class);
    private Client documentStoreQueryClient = mock(Client.class);
    private Client contentReadClient = mock(Client.class);

    private static final ClientMockBuilder CLIENT_MOCK_BUILDER = new ClientMockBuilder();

    private LinkResolverBodyProcessor processor;

    @Before
    public void setup() {

        Set<String> brands = ImmutableSet.of(BRAND_ID);
        List<BlogApiEndpointMetadata> metadataList = ImmutableList.of(
                new BlogApiEndpointMetadata("www.ft.com/resolved", brands, BLOG_ID),
                new BlogApiEndpointMetadata("somethingelse.ft.com/ablog", brands, BLOG_ID));
        BlogApiEndpointMetadataManager blogApiEndpointMetadataManager = new BlogApiEndpointMetadataManager(metadataList);

        processor = new LinkResolverBodyProcessor(
                Collections.singleton(SHORT_URL_PATTERN),
                resolverClient,
                blogApiEndpointMetadataManager,
                documentStoreQueryClient,
                DOC_STORE_URI,
                DOC_STORE_HOST_HEADER,
                contentReadClient,
                CONTENT_READ_URI,
                CONTENT_READ_HOST_HEADER,
                1, 2);
    }

    @Test
    public void thatUuidLinksAreResolvedToContent() {
        UUID ftContentUUID = UUID.randomUUID();
        URI uuidUrl = URI.create("http://www.ft.com/intl/cms/s/0/" + ftContentUUID + "#axzz425mjpp00");
        String bodyWithUuidLink = "<body><p>Blah blah blah <a href=\"" + uuidUrl
                + "\">usw</a> ...</p></body>";

        String expectedTransformed = "<body><p>Blah blah blah <content id=\"" + ftContentUUID
                + "\" type=\"" + ARTICLE_TYPE + "\">usw</content> ...</p></body>";


        CLIENT_MOCK_BUILDER.mockContentRead(contentReadClient, CONTENT_READ_URI, ftContentUUID.toString(), CONTENT_READ_HOST_HEADER, SC_OK);

        String actual = processor.process(bodyWithUuidLink, null);
        assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expectedTransformed));
    }

    @Test
    public void thatUuidLinksNotFoundAreNotTransformed() {
        UUID ftContentUUID = UUID.randomUUID();
        URI uuidUrl = URI.create("http://www.ft.com/intl/cms/s/0/" + ftContentUUID + "#axzz425mjpp00");
        String bodyWithUuidLink = "<body><p>Blah blah blah <a href=\"" + uuidUrl
                + "\">usw</a> ...</p></body>";

        CLIENT_MOCK_BUILDER.mockContentRead(contentReadClient, CONTENT_READ_URI, ftContentUUID.toString(), CONTENT_READ_HOST_HEADER, SC_NOT_FOUND);

        String actual = processor.process(bodyWithUuidLink, null);
        assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(bodyWithUuidLink));
    }

    @Test
    public void thatUuidLinksWithErrorResponseAreNotTransformed() {
        UUID ftContentUUID = UUID.randomUUID();
        URI uuidUrl = URI.create("http://www.ft.com/intl/cms/s/0/" + ftContentUUID + "#axzz425mjpp00");
        String bodyWithUuidLink = "<body><p>Blah blah blah <a href=\"" + uuidUrl
                + "\">usw</a> ...</p></body>";

        CLIENT_MOCK_BUILDER.mockContentRead(contentReadClient, CONTENT_READ_URI, ftContentUUID.toString(), CONTENT_READ_HOST_HEADER, SC_INTERNAL_SERVER_ERROR);

        String actual = processor.process(bodyWithUuidLink, null);
        assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(bodyWithUuidLink));
    }

    @Test
    public void thatShortenedLinksAreResolvedToContent() {
        URI shortUrl = URI.create("http://short.example.com/foobar");
        String resolvedIdentifier = "http://www.ft.com/resolved/foo/bar";
        UUID ftContentUUID = UUID.randomUUID();
        String bodyWithShortLink = "<body><p>Blah blah blah <a href=\"" + shortUrl
                + "\">usw</a> ...</p></body>";

        String expectedTransformed = "<body><p>Blah blah blah <content id=\"" + ftContentUUID
                + "\" type=\"" + ARTICLE_TYPE + "\">usw</content> ...</p></body>";

        CLIENT_MOCK_BUILDER.mockResolverRedirect(resolverClient, shortUrl, URI.create(resolvedIdentifier));
        URI queryURI = CLIENT_MOCK_BUILDER.buildDocumentStoreQueryUri(
                DOC_STORE_QUERY,
                URI.create(BLOG_AUTHORITY),
                URI.create(resolvedIdentifier)
        );
        CLIENT_MOCK_BUILDER.mockDocumentStoreQuery(
                documentStoreQueryClient,
                queryURI,
                URI.create("http://www.ft.com/content/" + ftContentUUID),
                SC_MOVED_PERMANENTLY
        );
        CLIENT_MOCK_BUILDER.mockContentRead(contentReadClient, CONTENT_READ_URI, ftContentUUID.toString(), CONTENT_READ_HOST_HEADER, SC_OK);

        String actual = processor.process(bodyWithShortLink, null);
        assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expectedTransformed));

        InOrder inOrder = inOrder(resolverClient);
        inOrder.verify(resolverClient).setFollowRedirects(false);
        inOrder.verify(resolverClient).resource(shortUrl);

        inOrder = inOrder(documentStoreQueryClient);
        inOrder.verify(documentStoreQueryClient).setFollowRedirects(false);
        inOrder.verify(documentStoreQueryClient).resource(queryURI);
    }

    @Test
    public void thatShortenedLinksWithQueryParametersAreResolvedToContent() {
        URI shortUrl = URI.create("http://short.example.com/foobar");
        String resolvedIdentifier = "http://www.ft.com/resolved/foo/bar/?p=12345";
        UUID ftContentUUID = UUID.randomUUID();
        String bodyWithShortLink = "<body><p>Blah blah blah <a href=\"" + shortUrl
                + "\">usw</a> ...</p></body>";

        String expectedTransformed = "<body><p>Blah blah blah <content id=\"" + ftContentUUID
                + "\" type=\"" + ARTICLE_TYPE + "\">usw</content> ...</p></body>";

        CLIENT_MOCK_BUILDER.mockResolverRedirect(resolverClient, shortUrl, URI.create(resolvedIdentifier));
        URI queryURI = CLIENT_MOCK_BUILDER.buildDocumentStoreQueryUri(
                DOC_STORE_QUERY,
                URI.create(BLOG_AUTHORITY),
                URI.create(resolvedIdentifier)
        );
        CLIENT_MOCK_BUILDER.mockDocumentStoreQuery(
                documentStoreQueryClient,
                queryURI,
                URI.create("http://www.ft.com/content/" + ftContentUUID),
                SC_MOVED_PERMANENTLY
        );
        CLIENT_MOCK_BUILDER.mockContentRead(contentReadClient, CONTENT_READ_URI, ftContentUUID.toString(), CONTENT_READ_HOST_HEADER, SC_OK);

        String actual = processor.process(bodyWithShortLink, null);
        assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expectedTransformed));

        InOrder inOrder = inOrder(resolverClient);
        inOrder.verify(resolverClient).setFollowRedirects(false);
        inOrder.verify(resolverClient).resource(shortUrl);

        inOrder = inOrder(documentStoreQueryClient);
        inOrder.verify(documentStoreQueryClient).setFollowRedirects(false);
        inOrder.verify(documentStoreQueryClient).resource(queryURI);
    }

    @Test
    public void thatMaximumNumberOfLinksAreResolvedToContent() {
        URI[] shortUrl = new URI[]{
                URI.create("http://short.example.com/foobar/1"),
                URI.create("http://short.example.com/foobar/2"),
                URI.create("http://short.example.com/foobar/3")
        };

        String resolvedIdentifier = "http://www.ft.com/resolved/foo/bar";
        String resolvedIdentifier2 = "http://www.ft.com/resolved/bar/foo";
        UUID ftContentUUID = UUID.randomUUID();
        UUID ftContentUUID2 = UUID.randomUUID();
        String bodyWithShortLink = "<body><p>Blah blah blah "
                + "<a href=\"" + shortUrl[0] + "\">Link 0</a>"
                + "<a href=\"" + shortUrl[1] + "\">Link 1</a>"
                + "<a href=\"" + shortUrl[2] + "\">Link 2</a>"
                + "...</p></body>";

        String expectedTransformed = "<body><p>Blah blah blah "
                + "<content id=\"" + ftContentUUID + "\" type=\"" + ARTICLE_TYPE + "\">Link 0</content>"
                + "<content id=\"" + ftContentUUID2 + "\" type=\"" + ARTICLE_TYPE + "\">Link 1</content>"
                + "<a href=\"" + shortUrl[2] + "\">Link 2</a>"
                + "...</p></body>";

        CLIENT_MOCK_BUILDER.mockResolverRedirect(resolverClient, shortUrl[0], URI.create(resolvedIdentifier));
        CLIENT_MOCK_BUILDER.mockResolverRedirect(resolverClient, shortUrl[1], URI.create(resolvedIdentifier2));
        CLIENT_MOCK_BUILDER.mockResolverRedirect(resolverClient, shortUrl[2], URI.create(resolvedIdentifier2));
        CLIENT_MOCK_BUILDER.mockDocumentStoreQuery(
                documentStoreQueryClient,
                DOC_STORE_QUERY,
                URI.create(BLOG_AUTHORITY),
                URI.create(resolvedIdentifier),
                URI.create("http://www.ft.com/content/" + ftContentUUID),
                SC_MOVED_PERMANENTLY
        );
        CLIENT_MOCK_BUILDER.mockDocumentStoreQuery(
                documentStoreQueryClient,
                DOC_STORE_QUERY,
                URI.create(BLOG_AUTHORITY),
                URI.create(resolvedIdentifier2),
                URI.create("http://www.ft.com/content/" + ftContentUUID2),
                SC_MOVED_PERMANENTLY
        );
        CLIENT_MOCK_BUILDER.mockContentRead(contentReadClient, CONTENT_READ_URI, ftContentUUID.toString(), CONTENT_READ_HOST_HEADER, SC_OK);
        CLIENT_MOCK_BUILDER.mockContentRead(contentReadClient, CONTENT_READ_URI, ftContentUUID2.toString(), CONTENT_READ_HOST_HEADER, SC_OK);

        try {
            Logger logger = configureMockAppenderFor(LinkResolverBodyProcessor.class);

            String actual = processor.process(bodyWithShortLink, null);
            assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expectedTransformed));

            verify(resolverClient).resource(shortUrl[0]);
            verify(resolverClient).resource(shortUrl[1]);
            verify(resolverClient, never()).resource(shortUrl[2]);
            assertLogEvent(logger, "Article contains too many links to resolve\\. Omitting.*" + shortUrl[2] + ".*");
        } finally {
            resetLoggingFor(LinkResolverBodyProcessor.class);
        }
    }

    @Test
    public void thatShortenedLinksForNonFTContentAreNotTransformed() {
        URI shortUrl = URI.create("http://short.example.com/foobar");
        URI redirectionUrl = URI.create("http://www.example.org/");

        String body = "<body><p>Blah blah blah <a href=\"" + shortUrl
                + "\">usw</a> ...</p></body>";

        CLIENT_MOCK_BUILDER.mockResolverRedirect(resolverClient, shortUrl, redirectionUrl);
        CLIENT_MOCK_BUILDER.mockResolverRedirect(resolverClient, redirectionUrl, SC_OK);

        String actual = processor.process(body, null);
        assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(body));

        InOrder inOrder = inOrder(resolverClient);
        inOrder.verify(resolverClient).setFollowRedirects(false);
        inOrder.verify(resolverClient).resource(shortUrl);

        verify(documentStoreQueryClient, never()).resource(any(URI.class));
    }

    @Test
    public void thatOtherLinksAreNotTransformed() {
        String body = "<body><p>Blah blah blah <a href=\"http://www.example.com/foobar\">usw</a> ...</p></body>";

        String actual = processor.process(body, null);
        assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(body));
        verify(resolverClient, never()).resource(any(URI.class));
        verify(documentStoreQueryClient, never()).resource(any(URI.class));
    }

    @Test
    public void thatCircularShortenedLinksAreNotTransformed() {
        URI shortUrl = URI.create("http://short.example.com/foobar");
        String body = "<body><p>Blah blah blah <a href=\"" + shortUrl.toASCIIString()
                + "\">usw</a> ...</p></body>";

        CLIENT_MOCK_BUILDER.mockResolverRedirect(resolverClient, shortUrl, shortUrl);

        String actual = processor.process(body, null);
        assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(body));
        verify(documentStoreQueryClient, never()).resource(any(URI.class));
    }

    @Test
    public void thatShortenedLinksWithErrorResponsesAreNotTransformed() {
        URI shortUrl = URI.create("http://short.example.com/foobar");
        String body = "<body><p>Blah blah blah <a href=\"" + shortUrl.toASCIIString()
                + "\">usw</a> ...</p></body>";

        CLIENT_MOCK_BUILDER.mockResolverRedirect(resolverClient, shortUrl, SC_NOT_FOUND);

        String actual = processor.process(body, null);
        assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(body));
        verify(documentStoreQueryClient, never()).resource(any(URI.class));
    }

    @Test
    public void thatShortenedLinksResolvedButNotFoundAreNotTransformed() {
        URI shortUrl = URI.create("http://short.example.com/foobar");
        URI resolvedIdentifier = URI.create("http:/www.ft.com/resolved/foo/bar");
        String body = "<body><p>Blah blah blah <a href=\"" + shortUrl
                + "\">usw</a> ...</p></body>";

        CLIENT_MOCK_BUILDER.mockResolverRedirect(resolverClient, shortUrl, resolvedIdentifier);
        URI queryURI = CLIENT_MOCK_BUILDER.buildDocumentStoreQueryUri(
                DOC_STORE_QUERY,
                URI.create(BLOG_AUTHORITY),
                resolvedIdentifier
        );
        CLIENT_MOCK_BUILDER.mockDocumentStoreQuery(
                documentStoreQueryClient,
                queryURI,
                resolvedIdentifier,
                SC_NOT_FOUND
        );

        String actual = processor.process(body, null);
        assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(body));
    }

    @Test
    public void thatFullWordpressLinksAreResolvedToContent() {
        URI fullUrl = URI.create(FULL_WORDPRESS_URL);
        UUID ftContentUUID = UUID.randomUUID();
        String bodyWithShortLink = "<body><p>Blah blah blah <a href=\"" + fullUrl
                + "\">usw</a> ...</p></body>";

        String expectedTransformed = "<body><p>Blah blah blah <content id=\"" + ftContentUUID
                + "\" type=\"" + ARTICLE_TYPE + "\">usw</content> ...</p></body>";

        URI queryURI = CLIENT_MOCK_BUILDER.buildDocumentStoreQueryUri(
                DOC_STORE_QUERY,
                URI.create(BLOG_AUTHORITY),
                fullUrl
        );
        CLIENT_MOCK_BUILDER.mockDocumentStoreQuery(
                documentStoreQueryClient,
                queryURI,
                URI.create("http://www.ft.com/content/" + ftContentUUID),
                SC_MOVED_PERMANENTLY
        );
        CLIENT_MOCK_BUILDER.mockContentRead(contentReadClient, CONTENT_READ_URI, ftContentUUID.toString(), CONTENT_READ_HOST_HEADER, SC_OK);

        String actual = processor.process(bodyWithShortLink, null);
        assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expectedTransformed));

        InOrder inOrder = inOrder(resolverClient);
        inOrder.verify(resolverClient).setFollowRedirects(false);

        inOrder = inOrder(documentStoreQueryClient);
        inOrder.verify(documentStoreQueryClient).setFollowRedirects(false);
        inOrder.verify(documentStoreQueryClient).resource(queryURI);
    }

    @Test
    public void thatFullWordpressLinksResolvedButNotFoundAreNotTransformed() {
        URI resolvedIdentifier = URI.create("http:/www.ft.com/resolved/foo/bar");
        String body = "<body><p>Blah blah blah <a href=\"" + FULL_WORDPRESS_URL
                + "\">usw</a> ...</p></body>";

        CLIENT_MOCK_BUILDER.mockResolverRedirect(resolverClient, URI.create(FULL_WORDPRESS_URL), resolvedIdentifier);
        CLIENT_MOCK_BUILDER.mockDocumentStoreQuery(
                documentStoreQueryClient,
                DOC_STORE_QUERY,
                URI.create(BLOG_AUTHORITY),
                resolvedIdentifier,
                null,
                SC_MOVED_PERMANENTLY
        );

        String actual = processor.process(body, null);
        assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(body));
    }

    @Test(expected = BodyProcessingException.class)
    public void thatBadlyFormedContentIsRejected() {
        processor.process("<foo>", null);
    }

}
