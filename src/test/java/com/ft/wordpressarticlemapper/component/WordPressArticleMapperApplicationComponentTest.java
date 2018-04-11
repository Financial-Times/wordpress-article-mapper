package com.ft.wordpressarticlemapper.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.content.model.Syndication;
import com.ft.message.consumer.MessageListener;
import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.wordpressarticlemapper.exception.UnpublishablePostException;
import com.ft.wordpressarticlemapper.exception.UntransformablePostException;
import com.ft.wordpressarticlemapper.exception.WordPressContentException;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_MOVED_PERMANENTLY;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WordPressArticleMapperApplicationComponentTest {

    private static final String MESSAGE_ID = "bb918201-2058-38a5-bd70-be8126200f2d";
    private static final String TRANSACTION_ID = "tid_ptvw9xpnhv";
    private static final String AUTHORITY = "http%3A%2F%2Fapi.ft.com%2Fsystem%2FFT-LABS-WP-1-24";
    private static final String IDENTIFIER_VALUE = "http%3A%2F%2Fuat.ftalphaville.ft.com%2F2014%2F10%2F20%2F2013232%2" +
            "Fregressing-to-the-mean-in-china-or-why-if-something-cannot-go-on-forever-it-will-stop%2F";
    private static final String CANONICAL_WEB_URL_TEMPLATE = "https://www.ft.com/content/%s";

    private static final MessageProducer producer = mock(MessageProducer.class);
    private static MessageListener listener;
    private ObjectMapper objectMapper;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @ClassRule
    public static WordPressArticleMapperAppRule wordPressArticleMapperAppRule =
            new WordPressArticleMapperAppRule("wordpress-article-mapper-test.yaml", 8080, producer);

    @BeforeClass
    public static void setUpClass() {
        listener = WordPressArticleMapperAppRule.getListener();
    }

    @Before
    public void setUp() {
        wordPressArticleMapperAppRule.reset();
        reset(producer);
        objectMapper = new ObjectMapper();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMessageIsSentWhenContentFoundInWordPress() throws Exception {
        wordPressArticleMapperAppRule.mockContentReadResponse("3fcac834-58ce-11e4-a31b-00144feab7de", SC_OK);
        wordPressArticleMapperAppRule.mockContentReadResponse("8adad508-077b-3795-8569-18e532cabf96", SC_OK);

        wordPressArticleMapperAppRule.mockDocumentStoreQueryResponse(AUTHORITY, IDENTIFIER_VALUE, SC_MOVED_PERMANENTLY,
                "https://next.ft.com/content/8adad508-077b-3795-8569-18e532cabf96");

        String messageBody = loadFile("wordPress/__files/wordpress-content.json");
        SystemId methodeSystemId = SystemId.systemIdFromCode("wordpress");

        Message message = new Message.Builder()
                .withMessageId("bb918201-2058-38a5-bd70-be8126200f2d")
                .withMessageType("cms-content-published")
                .withOriginSystemId(methodeSystemId)
                .withMessageTimestamp(new Date())
                .withContentType("application/json")
                .withMessageBody(messageBody)
                .build();

        message.addCustomMessageHeader(TRANSACTION_ID_HEADER, TRANSACTION_ID);

        listener.onMessage(message, TRANSACTION_ID);

        ArgumentCaptor<List> sent = ArgumentCaptor.forClass(List.class);

        verify(producer).send(sent.capture());
        List<Message> sentMessages = sent.getValue();
        assertThat(sentMessages.size(), equalTo(1));
        checkWordPressArticleMessageMessage(sentMessages.get(0), TRANSACTION_ID);

    }

    private void checkWordPressArticleMessageMessage(Message actual, String txId) throws IOException {
        assertThat(actual.getCustomMessageHeader(TRANSACTION_ID_HEADER), equalTo(txId));
        Map wordPressBlogPostContent = objectMapper.reader(Map.class).readValue(actual.getMessageBody());

        Map jsonPayload = (Map) wordPressBlogPostContent.get("payload");

        String uuid = (String) jsonPayload.get("uuid");
        assertThat((String) wordPressBlogPostContent.get("contentUri"), endsWith("/content/" + uuid));
        assertThat(jsonPayload.get("title"), equalTo("The 6am London Cut"));
        assertThat(jsonPayload.get("description"), equalTo(null));
        assertThat(jsonPayload.get("publishedDate"), equalTo("2014-10-21T04:45:30.000Z"));
        assertThat((String) jsonPayload.get("body"), CoreMatchers.containsString("<content id=\"3fcac834-58ce-11e4-a31b-00144feab7de\""));
        assertThat((String) jsonPayload.get("body"), CoreMatchers.containsString("<content id=\"8adad508-077b-3795-8569-18e532cabf96\""));
        assertThat((String) jsonPayload.get("opening"), CoreMatchers.containsString("<content id=\"3fcac834-58ce-11e4-a31b-00144feab7de\""));
        assertThat((String) jsonPayload.get("opening"), CoreMatchers.containsString("<content id=\"8adad508-077b-3795-8569-18e532cabf96\""));
        assertThat(jsonPayload.get("publishReference"), equalTo(txId));
        assertThat(jsonPayload.get("firstPublishedDate"), equalTo("2014-10-21T04:45:30.000Z"));
        assertThat(jsonPayload.get("canBeDistributed"), equalTo("yes"));
        assertThat(jsonPayload.get("canBeSyndicated"), equalTo(Syndication.VERIFY.getCanBeSyndicated()));
        assertThat(jsonPayload.get("canonicalWebUrl"), equalTo(String.format(CANONICAL_WEB_URL_TEMPLATE, uuid)));
        assertThat(jsonPayload.get("webUrl"), equalTo("http://uat.ftalphaville.ft.com/2014/10/21/2014692/the-6am-london-cut-277/"));
    }

    @Test
    public void testExceptionIsThrownWhenWordPressMessageHasEmptyBody() throws Exception {
        Message message = getMessage(null);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No content supplied");

        listener.onMessage(message, TRANSACTION_ID);
    }

    @Test
    public void testExceptionIsThrownWhenMessageHasBodyNotFromWordPress() throws Exception {
        Message message = getMessage(
                "wordPress/__files/WILL_RETURN_200-body-not-from-wordpress.json");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No content supplied");

        listener.onMessage(message, TRANSACTION_ID);
    }

    @Test
    public void testExceptionIsThrownWhenIncorrectBlogType() throws Exception {
        Message message = getMessage(
                "wordPress/__files/WILL_RETURN_200-incorrect-blog-type.json");

        thrown.expect(UnpublishablePostException.class);

        listener.onMessage(message, TRANSACTION_ID);
    }

    @Test
    public void testExceptionIsThrownWhenIncorrectContentType() throws Exception {
        Message message = getMessage(
                "wordPress/__files/WILL_RETURN_200-incorrect-content-type.json");

        thrown.expect(WordPressContentException.class);
        thrown.expectMessage("Unable to parse Wordpress content message");

        listener.onMessage(message, TRANSACTION_ID);
    }

    @Test
    public void testExceptionIsThrownWhenMessageHasNoApiUrl() throws Exception {
        Message message = getMessage(
                "wordPress/__files/WILL_RETURN_200-no-apiurl-on-response.json");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No apiUrl supplied");

        listener.onMessage(message, TRANSACTION_ID);
    }

    @Test
    public void testExceptionIsThrownWhenMessageContentIsUnsupported() throws Exception {
        Message message = getMessage(
                "wordPress/__files/WILL_RETURN_200-unsupported-content.json");

        thrown.expect(UntransformablePostException.class);
        thrown.expectMessage("Not a valid WordPress article for publication - body of transformed post is empty");

        listener.onMessage(message, TRANSACTION_ID);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void textNoExceptionIsThrownWhenReadContentReturns500() throws Exception {
        testExceptionIsThrownForContentReadStatusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void textExceptionIsThrownWhenReadContentReturns503() throws Exception {
        testExceptionIsThrownForContentReadStatusCode(SC_SERVICE_UNAVAILABLE);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void textNoExceptionIsThrownWhenReadContentReturns404() throws Exception {
        testExceptionIsThrownForContentReadStatusCode(SC_NOT_FOUND);
    }

    @SuppressWarnings("unchecked")
    private void testExceptionIsThrownForContentReadStatusCode(int status) throws Exception {
        wordPressArticleMapperAppRule.mockContentReadResponse(
                "3fcac834-58ce-11e4-a31b-00144feab7de", status);

        Message message = getMessage(
                "wordPress/__files/wordpress-content-read-error.json");

        listener.onMessage(message, TRANSACTION_ID);
        ArgumentCaptor<List> sent = ArgumentCaptor.forClass(List.class);

        verify(producer, times(1)).send(sent.capture());
        List<Message> sentMessages = sent.getValue();
        assertThat(sentMessages.size(), equalTo(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testWhenDocumentStoreApiReturns404() throws Exception {
        wordPressArticleMapperAppRule.mockDocumentStoreQueryResponse(AUTHORITY, IDENTIFIER_VALUE, SC_NOT_FOUND, StringUtils.EMPTY);
        Message message = getMessage("wordPress/__files/wordpress-content-document-store-api-error.json");

        listener.onMessage(message, TRANSACTION_ID);

        ArgumentCaptor<List> sent = ArgumentCaptor.forClass(List.class);

        verify(producer).send(sent.capture());
        List<Message> sentMessages = sent.getValue();
        assertThat(sentMessages.size(), equalTo(1));

        Message sentMessage = sentMessages.get(0);
        assertThat(sentMessage.getCustomMessageHeader(TRANSACTION_ID_HEADER), equalTo(TRANSACTION_ID));
        Map wordPressBlogPostContent = objectMapper.reader(Map.class).readValue(sentMessage.getMessageBody());
        Map jsonPayload = (Map) wordPressBlogPostContent.get("payload");

        assertThat((String) jsonPayload.get("body"), not(CoreMatchers.containsString("<content id=\"8adad508-077b-3795-8569-18e532cabf96\"")));
        assertThat((String) jsonPayload.get("opening"), not(CoreMatchers.containsString("<content id=\"8adad508-077b-3795-8569-18e532cabf96\"")));

    }

    private Message getMessage(String bodyLocation) throws Exception {
        SystemId methodeSystemId = SystemId.systemIdFromCode("wordpress");
        String messageBody;

        if (bodyLocation != null) {
            messageBody = loadFile(bodyLocation);
        } else {
            messageBody = "{}";
        }

        Message message = new Message.Builder()
                .withMessageId(MESSAGE_ID)
                .withMessageType("cms-content-published")
                .withOriginSystemId(methodeSystemId)
                .withMessageTimestamp(new Date())
                .withContentType("application/json")
                .withMessageBody(messageBody)
                .build();
        message.addCustomMessageHeader(TRANSACTION_ID_HEADER, TRANSACTION_ID);
        return message;
    }


    private String loadFile(final String filename) throws Exception {
        URL resource = getClass().getClassLoader().getResource(filename);
        if (resource != null) {
            final URI uri = resource.toURI();
            return new String(Files.readAllBytes(Paths.get(uri)), "UTF-8");
        }
        return null;
    }
}
