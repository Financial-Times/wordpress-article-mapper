package com.ft.wordpressarticlemapper.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.message.consumer.MessageListener;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.wordpressarticlemapper.exception.InvalidResponseException;
import com.ft.wordpressarticlemapper.exception.UnexpectedStatusFieldException;
import com.ft.wordpressarticlemapper.exception.UnpublishablePostException;
import com.ft.wordpressarticlemapper.exception.WordPressContentException;
import com.ft.wordpressarticlemapper.response.NativeWordPressContent;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.wordpressarticlemapper.response.WordPressPostType;
import com.ft.wordpressarticlemapper.response.WordPressStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.function.Predicate;

public class NativeCmsPublicationEventsListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(NativeCmsPublicationEventsListener.class);
    private static final String ERROR_NOT_FOUND = "Not found."; // DOES include a dot
    private static final Set<String> SUPPORTED_POST_TYPES = WordPressPostType.stringValues();
    private static final String UNSUPPORTED_POST_TYPE =
            "Not a valid post, type [%s] is not in supported types %s, for content with uuid:[%s]";

    private final Predicate<Message> filter;
    private final ObjectMapper objectMapper;
    private final SystemId systemId;
    private final MessageProducingContentMapper contentMapper;

    public NativeCmsPublicationEventsListener(MessageProducingContentMapper contentMapper,
                                              ObjectMapper objectMapper,
                                              String systemCode) {
        this.contentMapper = contentMapper;
        this.systemId = SystemId.systemIdFromCode(systemCode);
        this.filter = msg -> (systemId.equals(msg.getOriginSystemId()));
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean onMessage(Message message, String transactionId) {
        if (filter.test(message)) {
            LOG.info("process message");
            handleMessage(message, transactionId);
        } else {
            LOG.info("Skip message from [{}]", message.getOriginSystemId());
        }
        return true;
    }

    private void handleMessage(Message message, String transactionId) {
        NativeWordPressContent content;
        Date lastModified = message.getMessageTimestamp();

        try {
            content = objectMapper.reader(NativeWordPressContent.class).readValue(message.getMessageBody());
            Post post = content.getPost();
            if (post == null) {
                throw new IllegalArgumentException("No content supplied");
            }
            String status = content.getStatus();
            if (status == null) {
                throw new InvalidResponseException("Native WordPress content is not valid. Status is null");
            }
            String uuid = post.getUuid();
            WordPressStatus wordPressStatus;
            try {
                wordPressStatus = WordPressStatus.valueOf(status);
            } catch (IllegalArgumentException ignored) {
                throw new UnexpectedStatusFieldException(status, uuid);
            }
            switch (wordPressStatus) {
                case error:
                    String error = content.getError();
                    if (ERROR_NOT_FOUND.equals(error)) {
                        contentMapper.mapForDelete(uuid, lastModified, transactionId);
                    }
                    break;
                case ok:
                    if (content.getApiUrl() == null) {
                        throw new IllegalArgumentException("No apiUrl supplied");
                    }

                    if (!isSupportedPostType(content)) {
                        throw new UnpublishablePostException(uuid, String.format(UNSUPPORTED_POST_TYPE,
                                findTheType(content), SUPPORTED_POST_TYPES, uuid));
                    }
                    LOG.info("Importing content [{}] of type [{}] .", post.getUuid(), post.getType());
                    LOG.info("Event for {}.", post.getUuid());
                    contentMapper.mapForPublish(transactionId, post, lastModified);
                    break;

            }
        } catch (IOException e) {
            throw new WordPressContentException("Unable to parse Wordpress content message", e);
        }
    }

    private String findTheType(NativeWordPressContent nativeWordPressContent) {
        if (nativeWordPressContent.getPost() == null) {
            return null;
        }
        return nativeWordPressContent.getPost().getType();
    }

    private boolean isSupportedPostType(NativeWordPressContent nativeWordPressContent) {
        Post post = nativeWordPressContent.getPost();
        if (post != null) {
            LOG.info("post={}, type={}", post.getId(), post.getType());
            return SUPPORTED_POST_TYPES.contains(post.getType());
        } else {
            LOG.info("Post was null");
            return false;
        }
    }


}
