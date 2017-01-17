package com.ft.wordpressarticlemapper.resources;

import com.ft.wordpressarticlemapper.component.WordPressArticleMapperAppRule;
import com.ft.wordpressarticlemapper.model.Brand;
import com.ft.wordpressarticlemapper.model.Identifier;
import com.ft.wordpressarticlemapper.model.WordPressBlogPostContent;
import com.ft.wordpressarticlemapper.model.WordPressContent;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class WordPressArticleMapperResourceTest {

    private static final String CONFIG_FILE = "wordpress-article-mapper-test.yaml";
    private static final String TRANSACTION_ID = "tid_ptvw9xpnhv";
    private static final String TRANSACTION_ID_HEADER = "X-Request-ID";
    private static final Brand ALPHA_VILLE_BRAND = new Brand("http://api.ft.com/things/89d15f70-640d-11e4-9803-0800200c9a66");


    @ClassRule
    public static WordPressArticleMapperAppRule wordPressArticleTransformerAppRule =
            new WordPressArticleMapperAppRule(CONFIG_FILE, 8080, null);

    private Client client;

    @Before
    public void setup() {
        client = Client.create();
        client.setReadTimeout(50000);
    }

    @Test
    public void shouldUnescapeHtmlNumericalEntityForTitleAndByline() throws Exception {
        final URI uri = buildTransformerUrl();
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
    public void shouldUnescapeHtmlNamedEntityForTitleAndByline() throws Exception {
        final URI uri = buildTransformerUrl();
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
    public void shouldReturn200AndCompleteResponseWhenContentFoundInWordPress() throws Exception {
        wordPressArticleTransformerAppRule.mockContentReadResponse(
                "3fcac834-58ce-11e4-a31b-00144feab7de", SC_OK);

        wordPressArticleTransformerAppRule.mockDocumentStoreQueryResponse(
                "http://api.ft.com/system/FT-LABS-WP-1-335",
                "http://www.ft.com/fastft/2015/12/09/south-african-rand-dives-after-finance-ministers-exit/",
                SC_MOVED_PERMANENTLY, "https://next.ft.com/content/8adad508-077b-3795-8569-18e532cabf96");

        final URI uri = buildTransformerUrl();
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-body-from-wordpress.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

        WordPressBlogPostContent receivedContent = clientResponse.getEntity(WordPressBlogPostContent.class);
        assertThat("title", receivedContent.getTitle(), is(equalTo("The 6am London Cut")));
        assertThat("body", receivedContent.getBody(),
                containsString("<p><strong>Markets: </strong>Bourses around Asia were mixed "));
        assertThat("body", receivedContent.getBody(),
                containsString("<content id=\"3fcac834-58ce-11e4-a31b-00144feab7de\""));

        assertThat("byline", receivedContent.getByline(), is(equalTo("FT Labs Administrator, Jan Majek, Adam Braimbridge")));
        assertThat("brands", receivedContent.getBrands(), hasItem(ALPHA_VILLE_BRAND));
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

        final URI uri = buildTransformerUrl();
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
    public void shouldReturn422WhenTypeNotPostFromWordpressResponse() throws Exception {
        final URI uri = buildTransformerUrl();
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-incorrect-blog-type.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat("response status", clientResponse, hasProperty("status", equalTo(SC_UNPROCESSABLE_ENTITY)));
        assertThat("response message", clientResponse.getEntity(String.class), containsString("Wordpress content is not valid"));
    }

    @Test
    public void shouldReturn422WhenPostContainsOnlyUnsupportedContent() throws Exception {
        final URI uri = buildTransformerUrl();
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-unsupported-content.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat("response status", clientResponse, hasProperty("status", equalTo(422)));
        assertThat("response message", clientResponse.getEntity(String.class), containsString("Wordpress content is not valid"));
    }

    @Test
    public void shouldReturn422WhenUrlIsNotValid() throws Exception {
        final URI uri = buildTransformerUrl();
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-no-apiurl-on-response.json");
        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);
        assertThat("response", clientResponse, hasProperty("status", equalTo(422)));
    }

    @Test
    public void shouldReturn422WhenApiUrlIsMissingFromWordpressResponse() throws Exception {
        final URI uri = buildTransformerUrl();
        final String sourceApiJson = loadFile("wordPress/__files/WILL_RETURN_200-no-apiurl-on-response.json");

        final ClientResponse clientResponse = client.resource(uri)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat("response", clientResponse, hasProperty("status", equalTo(422)));
    }

    private URI buildTransformerUrl() {
        return UriBuilder
                .fromPath("map")
                .scheme("http")
                .host("localhost")
                .port(wordPressArticleTransformerAppRule.getWordPressArticleMapperLocalPort())
                .build();
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
