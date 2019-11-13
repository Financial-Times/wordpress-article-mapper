package com.ft.wordpressarticlemapper.resources;

import com.ft.messagequeueproducer.MessageProducer;
import com.ft.wordpressarticlemapper.component.WordPressArticleMapperAppRule;
import com.ft.wordpressarticlemapper.model.Brand;
import com.ft.wordpressarticlemapper.model.Identifier;
import com.ft.wordpressarticlemapper.model.WordPressBlogPostContent;
import com.ft.wordpressarticlemapper.model.WordPressContent;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.apache.http.HttpStatus.SC_MOVED_PERMANENTLY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class WordPressArticleMapperResourceTest {

    private static final String CONFIG_FILE = "wordpress-article-mapper-test.yaml";
    private static final String TRANSACTION_ID = "tid_ptvw9xpnhv";
    private static final String TRANSACTION_ID_HEADER = "X-Request-ID";
    private static final String TYPE_ARTICLE = "Article";
    private static final Brand ALPHA_VILLE_BRAND = new Brand("http://api.ft.com/things/89d15f70-640d-11e4-9803-0800200c9a66");

    private static MessageProducer messageProducer = mock(MessageProducer.class);

    @ClassRule
    public static WordPressArticleMapperAppRule wordPressArticleTransformerAppRule =
            new WordPressArticleMapperAppRule(CONFIG_FILE, 8080, messageProducer);

    private Client client;

    @Before
    public void setup() {
        client = Client.create();
        client.setReadTimeout(50000);
    }

    @Test
    public void mapShouldUnescapeHtmlNumericalEntityForTitleAndByline() throws Exception {
        final URI uri = buildMapperUrl("map");
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-body-no-html-entity-number-from-wordpress.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

        WordPressContent receivedContent = clientResponse.getEntity(WordPressBlogPostContent.class);
        assertThat("title", receivedContent.getTitle(), is(equalTo("The 6am “London Cut”…")));
        assertThat("byline", receivedContent.getByline(), is(equalTo("<FT Labs Administrator>, <Jan Majek>, <Adam Braimbridge>")));
    }

    @Test
    public void mapShouldUnescapeHtmlNamedEntityForTitleAndByline() throws Exception {
        final URI uri = buildMapperUrl("map");
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-body-no-html-entity-name-from-wordpress.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

        WordPressContent receivedContent = clientResponse.getEntity(WordPressBlogPostContent.class);
        assertThat("title", receivedContent.getTitle(), is(equalTo("The £64 million pound question & what it means for the EU…")));
        assertThat("byline", receivedContent.getByline(), is(equalTo("€FT Labs Administrator‰, £Jan Majek™, ¥Adam Braimbridge¾")));
    }

    @Test
    public void mapShouldReturn200AndCompleteResponseWhenContentFoundInWordPress() throws Exception {
        wordPressArticleTransformerAppRule.mockContentReadResponse(
                "3fcac834-58ce-11e4-a31b-00144feab7de", SC_OK);

        wordPressArticleTransformerAppRule.mockDocumentStoreQueryResponse(
                "http://api.ft.com/system/FT-LABS-WP-1-335",
                "http://www.ft.com/fastft/2015/12/09/south-african-rand-dives-after-finance-ministers-exit/",
                SC_MOVED_PERMANENTLY, "https://next.ft.com/content/8adad508-077b-3795-8569-18e532cabf96");

        final URI uri = buildMapperUrl("map");
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-body-from-wordpress.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

        WordPressBlogPostContent receivedContent = clientResponse.getEntity(WordPressBlogPostContent.class);
        assertThat("title", receivedContent.getTitle(), is(equalTo("The 6am London Cut")));
        assertThat("type", receivedContent.getType(), is(equalTo(TYPE_ARTICLE)));
        assertThat("body", receivedContent.getBody(),
                containsString("<p><strong>Markets: </strong>Bourses around Asia were mixed "));
        assertThat("body", receivedContent.getBody(),
                containsString("<content id=\"3fcac834-58ce-11e4-a31b-00144feab7de\""));

        assertThat("byline", receivedContent.getByline(), is(equalTo("FT Labs Administrator, Jan Majek, Adam Braimbridge")));
        assertThat("identifier", receivedContent.getIdentifiers(), hasItem(new Identifier("http://api.ft.com/system/FT-LABS-WP-1-24", "http://uat.ftalphaville.ft.com/2014/10/21/2014692/the-6am-london-cut-277/")));
        assertThat("identifier", receivedContent.getIdentifiers(), hasItem(new Identifier("http://api.ft.com/system/FT-LABS-WP-1-24", "http://uat.ftalphaville.ft.com/?p=2014692")));
        assertThat("uuid", receivedContent.getUuid(), is(equalTo("5c652c7e-c81e-4be7-8669-adeb5a5621dd")));
        assertThat("comments", receivedContent.getComments().isEnabled(), is(true));
    }

    @Test
    public void inputAndOutputPublishedDateWhenFormattedShouldBeUTC() throws Exception {
        String notExpectedOutputDate = "2014-10-21T08:45:30.000Z";
        String expectedOutputDate = "2014-10-21T04:45:30.000Z";
        String expectedOutputPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(expectedOutputPattern);

        final URI uri = buildMapperUrl("map");
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-body-from-wordpress.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);
        WordPressContent receivedContent = clientResponse.getEntity(WordPressBlogPostContent.class);

        // matches "date" gmt
        assertThat("published date",
                receivedContent.getPublishedDate().toInstant().atOffset(ZoneOffset.UTC).format(fmt),
                is(expectedOutputDate));

        // does not match "modified" gmt
        assertThat("published date",
                receivedContent.getPublishedDate().toInstant().atOffset(ZoneOffset.UTC).format(fmt),
                is(not(notExpectedOutputDate)));
    }

    @Test
    public void mapShouldReturn422WhenTypeNotPostFromWordpressResponse() throws Exception {
        final URI uri = buildMapperUrl("map");
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-incorrect-blog-type.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat("response status", clientResponse, hasProperty("status", equalTo(SC_UNPROCESSABLE_ENTITY)));
        assertThat("response message", clientResponse.getEntity(String.class), containsString("Wordpress content is not valid"));
    }

    @Test
    public void mapShouldReturn422WhenPostContainsOnlyUnsupportedContent() throws Exception {
        final URI uri = buildMapperUrl("map");
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-unsupported-content.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat("response status", clientResponse, hasProperty("status", equalTo(422)));
        assertThat("response message", clientResponse.getEntity(String.class), containsString("Wordpress content is not valid"));
    }

    @Test
    public void mapShouldReturn422WhenApiUrlIsMissingFromWordpressResponse() throws Exception {
        final URI uri = buildMapperUrl("map");
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-no-apiurl-on-response.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat("response", clientResponse, hasProperty("status", equalTo(422)));
    }

    @Test
    public void mapShouldReturn404ForWordpressDeleteEvent() throws Exception {
        final URI uri = buildMapperUrl("map");
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_404-delete-event.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void ingestShouldReturn204WhenContentIsValid() throws Exception {
        wordPressArticleTransformerAppRule.mockContentReadResponse(
                "3fcac834-58ce-11e4-a31b-00144feab7de", SC_OK);

        wordPressArticleTransformerAppRule.mockDocumentStoreQueryResponse(
                "http://api.ft.com/system/FT-LABS-WP-1-335",
                "http://www.ft.com/fastft/2015/12/09/south-african-rand-dives-after-finance-ministers-exit/",
                SC_MOVED_PERMANENTLY, "https://next.ft.com/content/8adad508-077b-3795-8569-18e532cabf96");

        final URI uri = buildMapperUrl("ingest");
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-body-from-wordpress.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat("response", clientResponse, hasProperty("status", equalTo(204)));
        ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(messageProducer).send(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().size(), equalTo(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void ingestShouldReturn204ForWordpressDeleteEvent() throws Exception {
        final URI uri = buildMapperUrl("ingest");
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_404-delete-event.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat("response", clientResponse, hasProperty("status", equalTo(204)));
        ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(messageProducer).send(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().size(), equalTo(1));
    }

    @Test
    public void ingestShouldReturn422WhenApiUrlIsMissingAndNoMessageShouldBeSent() throws Exception {
        final URI uri = buildMapperUrl("ingest");
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-no-apiurl-on-response.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat("response", clientResponse, hasProperty("status", equalTo(422)));
        verifyNoMoreInteractions(messageProducer);
    }

    @Test
    public void ingestShouldReturn422WhenPostContainsUnsupportedContentAndNoMessageShouldBeSent() throws Exception {
        final URI uri = buildMapperUrl("ingest");
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-unsupported-content.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat("response status", clientResponse, hasProperty("status", equalTo(422)));
        assertThat("response message", clientResponse.getEntity(String.class), containsString("Wordpress content is not valid"));
        verifyNoMoreInteractions(messageProducer);
    }

    private URI buildMapperUrl(String path) {
        return UriBuilder
                .fromPath(path)
                .scheme("http")
                .host("localhost")
                .port(wordPressArticleTransformerAppRule.getWordPressArticleMapperLocalPort())
                .build();
    }

    @After
    public void tearDown() {
        reset(messageProducer);
    }

    private String loadFile(final String fileName) throws Exception {
        URL url = getClass().getClassLoader().getResource(fileName);
        if (url == null) {
            return StringUtils.EMPTY;
        }
        final URI uri = url.toURI();
        return new String(Files.readAllBytes(Paths.get(uri)), "UTF-8");
    }

}
