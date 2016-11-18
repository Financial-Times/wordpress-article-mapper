package com.ft.wordpressarticlemapper.messaging;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messagequeueproducer.model.KeyedMessage;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.MessageType;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.wordpressarticlemapper.exception.WordPressContentException;
import com.ft.wordpressarticlemapper.exception.WordPressContentTypeException;
import com.ft.wordpressarticlemapper.model.WordPressBlogPostContent;
import com.ft.wordpressarticlemapper.model.WordPressContent;
import com.ft.wordpressarticlemapper.model.WordPressLiveBlogContent;
import com.ft.wordpressarticlemapper.response.NativeWordPressContent;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.wordpressarticlemapper.transformer.WordPressBlogPostContentMapper;
import com.ft.wordpressarticlemapper.transformer.WordPressLiveBlogContentMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static com.ft.messaging.standards.message.v1.MediaType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageProducingContentMapperTest {

    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();
    private static final SystemId SYSTEM_ID = SystemId.systemId("junit_system");
    private static final UriBuilder URI_BUILDER = UriBuilder.fromUri("http://www.example.org/content").path("{uuid}");
    private static final String PUBLISH_REF = "tid_yoyfkwcs2s";
    private static final MessageType CMS_CONTENT_PUBLISHED = MessageType.messageType("cms-content-published");

    private MessageProducingContentMapper mapper;

    @Mock
    private WordPressBlogPostContentMapper wordPressBlogPostContentMapper;
    @Mock
    private WordPressLiveBlogContentMapper wordPressLiveBlogContentMapper;
    @Mock
    private MessageProducer producer;

    private Post post;

    @Before
    public void setUp() throws Exception {
        NativeWordPressContent content = JACKSON_MAPPER.reader(NativeWordPressContent.class)
                .readValue(loadFile("messaging/native-wordpress-content.json"));
        post = content.getPost();

        mapper = new MessageProducingContentMapper(
                wordPressBlogPostContentMapper,
                wordPressLiveBlogContentMapper,
                JACKSON_MAPPER,
                SYSTEM_ID.toString(),
                producer,
                URI_BUILDER);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMessageIsProducedAndSent() throws Exception {
        Date lastModified = new Date();
        WordPressBlogPostContent content = JACKSON_MAPPER.reader(WordPressBlogPostContent.class)
                .readValue(loadFile("messaging/wordpress-blog-post-content-test.json"));

        when(wordPressBlogPostContentMapper.mapWordPressArticle(
                eq(PUBLISH_REF),
                eq(post),
                eq(lastModified))).thenReturn(content);

        WordPressContent actual = mapper.getWordPressArticleMessage(PUBLISH_REF, post, lastModified);

        assertThat(actual, equalTo(content));

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(producer).send(listCaptor.capture());

        List<Message> messages = listCaptor.getValue();
        assertThat(messages.size(), equalTo(1));

        Message actualMessage = messages.get(0);
        verifyMessage(actualMessage, content);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void verifyMessage(Message actualMessage, WordPressContent expectedContent)
            throws Exception {

        assertThat(actualMessage.getMessageType(), equalTo(CMS_CONTENT_PUBLISHED));
        assertThat(actualMessage.getOriginSystemId(), equalTo(SYSTEM_ID));
        assertThat(actualMessage.getContentType(), equalTo(JSON));
        assertThat(actualMessage.getCustomMessageHeader(TRANSACTION_ID_HEADER), equalTo(PUBLISH_REF));

        Map messageBody = JACKSON_MAPPER.readValue(actualMessage.getMessageBody(), Map.class);
        assertThat(messageBody.get("contentUri"), equalTo("http://www.example.org/content/" + post.getUuid()));

        Map actualContent = (Map) messageBody.get("payload");

        assertThat(actualContent.get("uuid"), equalTo(expectedContent.getUuid()));
        assertThat(actualContent.get("title"), equalTo(expectedContent.getTitle()));
        assertThat(actualContent.get("publishReference"), equalTo(expectedContent.getPublishReference()));
        assertThat(OffsetDateTime.parse((String) actualContent.get("lastModified")).toInstant(), equalTo(expectedContent.getLastModified().toInstant()));

        assertThat(actualMessage instanceof KeyedMessage, equalTo(true));
        assertThat(((KeyedMessage) actualMessage).getKey(), equalTo(post.getUuid()));
    }

    @Test(expected = WordPressContentException.class)
    public void testNoMessageIsSentWhenObjectMapperFails() throws Exception {

        ObjectMapper failing = mock(ObjectMapper.class);

        mapper = new MessageProducingContentMapper(wordPressBlogPostContentMapper,
                wordPressLiveBlogContentMapper,
                failing,
                SYSTEM_ID.toString(),
                producer,
                URI_BUILDER);

        Date lastModified = new Date();
        WordPressBlogPostContent content = JACKSON_MAPPER.reader(WordPressBlogPostContent.class)
                .readValue(loadFile("messaging/wordpress-blog-post-content-test.json"));

        when(wordPressBlogPostContentMapper.mapWordPressArticle(eq(PUBLISH_REF), eq(post), eq(lastModified))).thenReturn(content);
        when(failing.writeValueAsString(any())).thenThrow(new JsonGenerationException("test exception"));

        mapper.getWordPressArticleMessage(PUBLISH_REF, post, lastModified);
        verifyZeroInteractions(producer);
    }

    @Test
    public void testWordPressBlogContentMapperIsCalledForPostMessages() throws Exception {
        Date lastModified = new Date();
        WordPressBlogPostContent content = JACKSON_MAPPER.reader(WordPressBlogPostContent.class)
                .readValue(loadFile("messaging/wordpress-blog-post-content-test.json"));

        when(wordPressBlogPostContentMapper.mapWordPressArticle(eq(PUBLISH_REF), eq(post), eq(lastModified))).thenReturn(content);

        mapper.getWordPressArticleMessage(PUBLISH_REF, post, lastModified);

        verify(wordPressBlogPostContentMapper, times(1)).mapWordPressArticle(PUBLISH_REF, post, lastModified);
    }

    @Test
    public void testWordPressLiveBlogContentMapperIsCalledForLiveMessages() throws Exception {
        Date lastModified = new Date();
        WordPressLiveBlogContent content = JACKSON_MAPPER.reader(WordPressLiveBlogContent.class)
                .readValue(loadFile("messaging/wordpress-live-blog-content-test.json"));
        post.setType("webchat-live-blogs");

        when(wordPressLiveBlogContentMapper.mapWordPressArticle(eq(PUBLISH_REF), eq(post), eq(lastModified))).thenReturn(content);

        mapper.getWordPressArticleMessage(PUBLISH_REF, post, lastModified);

        verify(wordPressLiveBlogContentMapper, times(1)).mapWordPressArticle(PUBLISH_REF, post, lastModified);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testNoContentMapperIsCalledForWrongTypeMessage() throws Exception {
        Date lastModified = new Date();
        WordPressLiveBlogContent content = JACKSON_MAPPER.reader(WordPressLiveBlogContent.class)
                .readValue(loadFile("messaging/wordpress-live-blog-content-test.json"));
        post.setType("invalid-wordpress-article");

        when(wordPressLiveBlogContentMapper.mapWordPressArticle(eq(PUBLISH_REF), eq(post), eq(lastModified))).thenReturn(content);

        thrown.expect(WordPressContentTypeException.class);
        thrown.expectMessage("Unsupported blog post type");

        mapper.getWordPressArticleMessage(PUBLISH_REF, post, lastModified);
    }

    @Test
    public void testNoContentMapperIsCalledForNullTypeMessage() throws Exception {
        Date lastModified = new Date();
        WordPressLiveBlogContent content = JACKSON_MAPPER.reader(WordPressLiveBlogContent.class)
                .readValue(loadFile("messaging/wordpress-live-blog-content-test.json"));
        post.setType(null);

        when(wordPressLiveBlogContentMapper.mapWordPressArticle(eq(PUBLISH_REF), eq(post), eq(lastModified))).thenReturn(content);

        thrown.expect(WordPressContentTypeException.class);
        thrown.expectMessage("Unsupported blog post type");

        mapper.getWordPressArticleMessage(PUBLISH_REF, post, lastModified);
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
