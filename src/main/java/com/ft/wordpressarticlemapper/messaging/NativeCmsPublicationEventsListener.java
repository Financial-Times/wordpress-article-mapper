package com.ft.wordpressarticlemapper.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.message.consumer.MessageListener;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.wordpressarticlemapper.exception.WordPressContentException;
import com.ft.wordpressarticlemapper.response.NativeWordPressContent;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.wordpressarticlemapper.validation.NativeWordPressContentValidator;
import com.ft.wordpressarticlemapper.transformer.ContentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.function.Predicate;

public class NativeCmsPublicationEventsListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(NativeCmsPublicationEventsListener.class);

    private final Predicate<Message> filter;
    private final ObjectMapper objectMapper;
    private final SystemId systemId;
    private final ContentMapper contentMapper;
    private final NativeWordPressContentValidator validator;

    public NativeCmsPublicationEventsListener(ContentMapper contentMapper,
                                              ObjectMapper objectMapper,
                                              String systemCode,
                                              NativeWordPressContentValidator nativeWordPressContentValidator) {
        this.contentMapper = contentMapper;
        this.systemId = SystemId.systemIdFromCode(systemCode);
        this.filter = msg -> (systemId.equals(msg.getOriginSystemId()));
        this.objectMapper = objectMapper;
        this.validator = nativeWordPressContentValidator;
    }

    @Override
    public boolean onMessage(Message message, String transactionId) {
        if (filter.test(message)) {
            LOG.info("process message");
            handleMessage(message, transactionId);
        } else {
            LOG.info("skip message");
            LOG.debug("skip message {}", message);
        }
        return true;
    }

    private void handleMessage(Message message, String transactionId) {
        try {
            NativeWordPressContent content = objectMapper.reader(NativeWordPressContent.class).readValue(message.getMessageBody());
            validator.validateWordPressContent(content);
            Post post = content.getPost();
            LOG.info("Importing content [{}] of type [{}] .", post.getUuid(), post.getType());
            LOG.info("Event for {}.", post.getUuid());
            String postUrl = post.getUrl();
            if (postUrl == null) {
                throw new IllegalArgumentException("No post Url supplied");
            }
            URI requestUri = UriBuilder.fromUri(postUrl).build();
            Date lastModified = message.getMessageTimestamp();
            contentMapper.mapWordPressArticle(transactionId, requestUri, post, lastModified);
        } catch (IOException e) {
            throw new WordPressContentException("Unable to parse Methode content message", e);
        }
    }

}
