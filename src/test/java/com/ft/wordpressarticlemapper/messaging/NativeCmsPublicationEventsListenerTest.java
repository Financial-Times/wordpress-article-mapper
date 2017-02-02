package com.ft.wordpressarticlemapper.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.wordpressarticlemapper.exception.PostNotFoundException;
import com.ft.wordpressarticlemapper.exception.WordPressContentException;
import com.ft.wordpressarticlemapper.response.NativeWordPressContent;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.wordpressarticlemapper.validation.NativeWordPressContentValidator;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NativeCmsPublicationEventsListenerTest {

    private static final String SYSTEM_CODE = "junit";
    private static final String TX_ID = "junittx";
    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();
    private static final String MESSAGE_BODY_FOR_DELETE = "{\"error\":\"Not found.\",\"lastModified\":\"2016-12-13T08:45:35.151Z\",\"post\":{\"uuid\":\"8c4d6fea-1c49-33f7-5500-53dfc3335d88\"},\"status\":\"error\"}";

    private NativeCmsPublicationEventsListener listener;
    private NativeWordPressContent nativeWordPressContent;

    @Mock
    private MessageProducingContentMapper mapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ObjectReader objectReader;

    @Mock
    private NativeWordPressContentValidator contentValidator;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        listener = new NativeCmsPublicationEventsListener(mapper, objectMapper, SYSTEM_CODE, contentValidator);
        nativeWordPressContent = createSampleWordpressArticle();
        when(objectReader.readValue(loadFile("messaging/native-wordpress-content.json"))).thenReturn(nativeWordPressContent);
        when(objectReader.readValue(StringUtils.EMPTY)).thenThrow(new IOException());
        when(objectMapper.reader(NativeWordPressContent.class)).thenReturn(objectReader);
    }

    @Test
    public void thatMapForPublishIsCalledWhenStatusIsOk() throws Exception {
        Message message = getMessage();

        listener.onMessage(message, TX_ID);
        ArgumentCaptor<Post> c = ArgumentCaptor.forClass(Post.class);
        verify(mapper, times(1)).mapForPublish(eq(TX_ID), c.capture(), eq(message.getMessageTimestamp()));

        Post actual = c.getValue();
        assertThat(actual, notNullValue());
        Post initial = nativeWordPressContent.getPost();
        assertThat(actual.getUuid(), equalTo(initial.getUuid()));

    }

    @Test
    public void thatMapForDeleteIsCalledWhenStatusIsError() throws ParseException, IOException {
        Message message = new Message();
        Date messageTimestamp = new Date();
        message.setMessageTimestamp(messageTimestamp);
        message.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        message.setMessageBody(MESSAGE_BODY_FOR_DELETE);


        String uuid = "8c4d6fea-1c49-33f7-5500-53dfc3335d88";
        NativeWordPressContent expectedNativeContent = getNativeContentForDelete(uuid, messageTimestamp);
        doThrow(PostNotFoundException.class).when(contentValidator).validate(any(NativeWordPressContent.class));
        when(objectReader.readValue(MESSAGE_BODY_FOR_DELETE)).thenReturn(expectedNativeContent);

        listener.onMessage(message, TX_ID);

        ArgumentCaptor<String> c = ArgumentCaptor.forClass(String.class);
        verify(mapper, times(1)).mapForDelete(c.capture(), eq(messageTimestamp), eq(TX_ID));

        String actual = c.getValue();
        assertThat(actual, notNullValue());
        assertThat(actual, equalTo(uuid));
    }

    @Test
    public void thatMapperIsNotCalledWhenMessageHasNonMatchingSystemCode() {
        Message msg = new Message();
        msg.setOriginSystemId(SystemId.systemIdFromCode("foo"));
        assertThat(listener.onMessage(msg, TX_ID), is(true));
        verifyZeroInteractions(mapper);
    }

    @Test(expected = WordPressContentException.class)
    public void thatMapperThrowsExceptionWhenMessageCannotBeParsed() throws Exception {
        Date lastModified = new Date();
        Message message = new Message();
        message.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        message.setMessageTimestamp(lastModified);
        message.setMessageBody(StringUtils.EMPTY);
        listener.onMessage(message, TX_ID);
    }

    private Message getMessage() throws Exception {
        Date lastModified = new Date();
        Message message = new Message();
        message.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        message.setMessageTimestamp(lastModified);
        String messageBody = loadFile("messaging/native-wordpress-content.json");
        message.setMessageBody(messageBody != null ? messageBody : StringUtils.EMPTY);
        return message;
    }

    private NativeWordPressContent createSampleWordpressArticle() throws Exception {
        final String attributes = loadFile("messaging/native-wordpress-content.json");
        return JACKSON_MAPPER.reader(NativeWordPressContent.class).readValue(attributes);
    }

    private String loadFile(final String filename) throws Exception {
        URL resource = getClass().getClassLoader().getResource(filename);
        if (resource != null) {
            final URI uri = resource.toURI();
            return new String(Files.readAllBytes(Paths.get(uri)), "UTF-8");
        }
        return null;
    }

    private NativeWordPressContent getNativeContentForDelete(String uuid, Date messageTimestamp) {
        NativeWordPressContent expectedNativeContent = new NativeWordPressContent();
        Post post = new Post();
        post.setUuid(uuid);
        expectedNativeContent.setPost(post);
        expectedNativeContent.setStatus("error");
        expectedNativeContent.setError("Not found.");
        expectedNativeContent.setLastModified(messageTimestamp);

        return expectedNativeContent;
    }
}
