package com.ft.wordpressarticletransformer.resources;

import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_NO_REQUEST_TO_WORD_PRESS_EXPECTED;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORDPRESS_NO_APIURL_ON_RESPONSE;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NOT_TYPE_POST;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NO_HTML_NAME_ENTITY;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NO_HTML_NUMBER_ENTITY;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_404;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_500;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_502;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_CANNOT_CONNECT;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_ERROR_NOT_FOUND;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_ERROR_UNKNOWN;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_INVALID_CONTENT_TYPE;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_NON_WORD_PRESS_RESPONSE;
import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.UUID_MAP_TO_REQUEST_TO_WORD_PRESS_STATUS_UNKNOWN;
import static org.apache.http.HttpStatus.SC_MOVED_PERMANENTLY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import com.ft.wordpressarticletransformer.component.ErbTemplatingHelper;
import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.WordPressBlogPostContent;
import com.ft.wordpressarticletransformer.model.WordPressContent;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class WordPressArticleTransformerResourceTest {

    private static final Brand ALPHA_VILLE_BRAND = new Brand("http://api.ft.com/things/89d15f70-640d-11e4-9803-0800200c9a66");
    private static final String CONFIG_FILE = "config-component-tests.yml";
    private static final int NATIVERW_PORT;
    private static final int DOC_STORE_PORT;
    
    static {
      NATIVERW_PORT = WordPressArticleTransformerAppRule.findAvailableWireMockPort();
      DOC_STORE_PORT = WordPressArticleTransformerAppRule.findAvailableWireMockPort();
      
      Map<String, Object> hieraData = new HashMap<>();
      hieraData.put("httpPort", "22040");
      hieraData.put("adminPort", "22041");
      hieraData.put("jerseyClientTimeout", "5000ms");
      hieraData.put("nativeReaderPrimaryNodes", String.format("[\"localhost:%s:%s\"]", NATIVERW_PORT, NATIVERW_PORT));
      hieraData.put("queryClientTimeout", "5000ms");
      hieraData.put("queryReaderPrimaryNodes", String.format("[\"localhost:%s:%s\"]", DOC_STORE_PORT, DOC_STORE_PORT));
      hieraData.put("alphavilleHost", "localhost");
      
      try {
        ErbTemplatingHelper.generateConfigFile("ft-wordpress_article_transformer/templates/config.yml.erb", hieraData,
            CONFIG_FILE);
      } catch (Exception e) {
        throw new ExceptionInInitializerError(e);
      }
    }

    @ClassRule
    public static WordPressArticleTransformerAppRule wordPressArticleTransformerAppRule =
      new WordPressArticleTransformerAppRule(CONFIG_FILE, NATIVERW_PORT, DOC_STORE_PORT);

    private Client client;

    @Before
    public void setup() {
        client = Client.create();
        client.setReadTimeout(50000);
    }

    @Test
    public void shouldUnescapeHtmlNumericalEntityForTitleAndByline() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NO_HTML_NUMBER_ENTITY);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

        WordPressContent receivedContent = clientResponse.getEntity(WordPressBlogPostContent.class);
        assertThat("title", receivedContent.getTitle(), is(equalTo("The 6am “London Cut”…")));
        assertThat("byline", receivedContent.getByline(), is(equalTo("<FT Labs Administrator>, <Jan Majek>, <Adam Braimbridge>")));
    }

    @Test
    public void shouldUnescapeHtmlNamedEntityForTitleAndByline() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NO_HTML_NAME_ENTITY);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

        WordPressContent receivedContent = clientResponse.getEntity(WordPressBlogPostContent.class);
        assertThat("title", receivedContent.getTitle(), is(equalTo("The £64 million pound question & what it means for the EU…")));
        assertThat("byline", receivedContent.getByline(), is(equalTo("€FT Labs Administrator‰, £Jan Majek™, ¥Adam Braimbridge¾")));
    }

    @Test
    public void shouldReturn200AndCompleteResponseWhenContentFoundInWordPress() {
      wordPressArticleTransformerAppRule.mockDocumentStoreContentResponse(
          "3fcac834-58ce-11e4-a31b-00144feab7de", SC_OK);
      
      wordPressArticleTransformerAppRule.mockDocumentStoreQueryResponse(
          "http://api.ft.com/system/FT-LABS-WP-1-335",
          "http://www.ft.com/fastft/2015/12/09/south-african-rand-dives-after-finance-ministers-exit/",
          SC_MOVED_PERMANENTLY, "https://next.ft.com/content/8adad508-077b-3795-8569-18e532cabf96");
      
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

        WordPressBlogPostContent receivedContent = clientResponse.getEntity(WordPressBlogPostContent.class);
        assertThat("title", receivedContent.getTitle(), is(equalTo("The 6am London Cut")));
        assertThat("body", receivedContent.getBody(), allOf(
            containsString("<p><strong>Markets: </strong>Bourses around Asia were mixed ")/*,
            containsString("<content id=\"3fcac834-58ce-11e4-a31b-00144feab7de\""),
            containsString("<content id=\"8adad508-077b-3795-8569-18e532cabf96\"")*/
            ));
        
        assertThat("byline", receivedContent.getByline(), is(equalTo("FT Labs Administrator, Jan Majek, Adam Braimbridge")));
        assertThat("brands", receivedContent.getBrands(), hasItem(ALPHA_VILLE_BRAND));
        assertThat("identifier authority", receivedContent.getIdentifiers().first().getAuthority(), is(equalTo("http://api.ft.com/system/FT-LABS-WP-1-24")));
        assertThat("identifier value", receivedContent.getIdentifiers().first().getIdentifierValue(), is(equalTo("http://uat.ftalphaville.ft.com/2014/10/21/2014692/the-6am-london-cut-277/")));
        assertThat("uuid", receivedContent.getUuid(), is(equalTo(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS)));
        assertThat("comments", receivedContent.getComments().isEnabled(), is(true));
    }

    @Test
    public void inputAndOutputPublishedDateWhenFormattedShouldBeUTC() {
        String notExpectedOutputDate = "2014-10-21T08:45:30.000Z";
        String expectedOutputDate = "2014-10-21T04:45:30.000Z";
        String expectedOutputPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(expectedOutputPattern);

        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
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
    // this is what happens for posts that are in status=Pending, status=Draft, or visibility=Private....and deleted?
    public void shouldReturn500WithUuidWhenWordpressReturnsUnexpectedWordpressResponseStatus() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_STATUS_UNKNOWN);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
        assertThat("response", clientResponse.getEntity(String.class),
                containsString(String.format("Unexpected WordPress status=\\\"Unknown\\\" for uuid=\\\"%s\\\"", UUID_MAP_TO_REQUEST_TO_WORD_PRESS_STATUS_UNKNOWN)));
    }

    @Test
    public void shouldReturn500WithUuidWhenWordpressReturnsStatusErrorAndUnknownErrorMessage() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_ERROR_UNKNOWN);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
        assertThat("response", clientResponse.getEntity(String.class),
                containsString(String.format("Unexpected error from WordPress: [Unknown error occurred.] for uuid [%s].", UUID_MAP_TO_REQUEST_TO_WORD_PRESS_ERROR_UNKNOWN)));
    }

    @Test
    public void shouldReturn404WithUuidAndLastModifiedTimeStampWhenWordpressReturnsStatusErrorAndErrorNotFound() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_ERROR_NOT_FOUND);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        String responseBody = clientResponse.getEntity(String.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
        assertThat("response", responseBody, containsString("2016-01-07T12:22:56.821Z"));
        assertThat("response", responseBody,
                containsString(String.format("Error. Content with uuid: [%s] not found", UUID_MAP_TO_REQUEST_TO_WORD_PRESS_ERROR_NOT_FOUND))
        );

    }

    @Test
    public void shouldReturn404When404ReturnedFromNativeRw() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_404);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
    }

    @Test
    public void shouldReturn500WhenContentTypeNotJsonReturnedFromNativeRw() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_INVALID_CONTENT_TYPE);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
        assertThat("response", clientResponse.getEntity(String.class), containsString("server error"));
    }

    @Test
    public void shouldReturn422WhenTypeNotPostFromWordpressResponse() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NOT_TYPE_POST);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response status", clientResponse, hasProperty("status", equalTo(SC_UNPROCESSABLE_ENTITY)));
        assertThat("response message", clientResponse.getEntity(String.class), containsString("foo"));
    }

    @Test
    public void shouldReturn400WhenUuidIsNotValid() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_NO_REQUEST_TO_WORD_PRESS_EXPECTED);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response status", clientResponse, hasProperty("status", equalTo(400)));
    }

    @Test
    public void shouldReturn405WhenNoUuidSupplied() {
        final URI uri = buildTransformerUrlWithIdMissing();

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(405)));
    }

    @Test
    public void shouldReturn400WhenUrlIsNotValid() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORDPRESS_NO_APIURL_ON_RESPONSE);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    }

    @Test
    public void shouldReturn400WhenApiUrlIsMissingFromWordpressResponse() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_NON_WORD_PRESS_RESPONSE);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    }

    @Test
    public void shouldReturn500When500ReturnedFromNativeRw() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_500);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
    }

    @Test
    public void shouldReturn503WhenCannotConnectToNativeRw() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_CANNOT_CONNECT);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
    }

    @Test
    public void shouldReturn500WhenUnexpectedHttpStatusReturnedFromNativeRw() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_502);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
        assertThat("response message", clientResponse.getEntity(String.class), containsString("Unexpected error status from Native Reader: [502]."));
    }

    private URI buildTransformerUrl(String uuid) {
        return UriBuilder
                .fromPath("content")
                .path("{uuid}")
                .scheme("http")
                .host("localhost")
                .port(wordPressArticleTransformerAppRule.getWordPressArticleTransformerLocalPort())
                .build(uuid);
    }

    private URI buildTransformerUrlWithIdMissing() {
        return UriBuilder
                .fromPath("content")
                .scheme("http")
                .host("localhost")
                .port(wordPressArticleTransformerAppRule.getWordPressArticleTransformerLocalPort())
                .build();
    }

}
