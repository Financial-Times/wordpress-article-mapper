package com.ft.wordpressarticlemapper.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messagequeueproducer.model.KeyedMessage;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.wordpressarticlemapper.exception.WordPressContentException;
import com.ft.wordpressarticlemapper.exception.WordPressContentTypeException;
import com.ft.wordpressarticlemapper.model.WordPressContent;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.wordpressarticlemapper.response.WordPressPostType;
import com.ft.wordpressarticlemapper.transformer.WordPressBlogPostContentMapper;
import com.ft.wordpressarticlemapper.transformer.WordPressContentMapper;
import com.ft.wordpressarticlemapper.transformer.WordPressLiveBlogContentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static java.time.ZoneOffset.UTC;

public class MessageProducingContentMapper {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProducingContentMapper.class);
    private static final String CMS_CONTENT_PUBLISHED = "cms-content-published";
    private static final DateTimeFormatter RFC3339_FMT =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.withResolverStyle(ResolverStyle.STRICT);

    private final MessageProducer producer;
    private final ObjectMapper objectMapper;
    private final String systemId;
    private final UriBuilder contentUriBuilder;
    private final WordPressBlogPostContentMapper blogTransformer;
    private final WordPressLiveBlogContentMapper liveBlogTransformer;

    public MessageProducingContentMapper(WordPressBlogPostContentMapper blogTransformer,
                                         WordPressLiveBlogContentMapper liveBlogTransformer,
                                         ObjectMapper objectMapper, String systemId,
                                         MessageProducer producer, UriBuilder contentUriBuilder) {
        this.blogTransformer = blogTransformer;
        this.liveBlogTransformer = liveBlogTransformer;
        this.objectMapper = objectMapper;
        this.systemId = systemId;
        this.producer = producer;
        this.contentUriBuilder = contentUriBuilder;
    }

    public WordPressContent mapForPublish(String transactionId, Post post, Date lastModified) {
        WordPressContent content = mapperFor(post).mapWordPressArticle(transactionId, post, lastModified);
        List<WordPressContent> contents = Collections.singletonList(content);
        producer.send(contents.stream().map(this::createMessageForPublish).collect(Collectors.toList()));
        LOG.info("sent {} messages", contents.size());
        return content;
    }

    public void mapForDelete(String uuid, Date lastModifiedDate, String transactionId) {
        Map<String, Object> messageBody = getCommonMessageBody(uuid, lastModifiedDate);

        try {
            Message msg = getMessage(messageBody, uuid, transactionId);
            producer.send(Collections.singletonList(msg));
        } catch (JsonProcessingException e) {
            handleJsonProcessingException(e);
        }
    }

    private Map<String, Object> getCommonMessageBody(String uuid, Date lastModifiedDate) {
        Map<String, Object> messageBody = new LinkedHashMap<>();
        messageBody.put("contentUri", contentUriBuilder.build(uuid).toString());
        messageBody.put("lastModified", RFC3339_FMT.format(OffsetDateTime.ofInstant(lastModifiedDate.toInstant(), UTC)));
        return messageBody;
    }

    private Message createMessageForPublish(WordPressContent content) {
        Message msg = null;
        String uuid = content.getUuid();
        Map<String, Object> messageBody = getCommonMessageBody(uuid, content.getLastModified());
        messageBody.put("payload", content);

        try {
            msg = getMessage(messageBody, uuid, content.getPublishReference());
        } catch (JsonProcessingException e) {
            handleJsonProcessingException(e);
        }
        return msg;
    }

    private Message getMessage(Map<String, Object> messageBody, String uuid, String transactionId) throws JsonProcessingException {
        Message msg = new Message.Builder().withMessageId(UUID.randomUUID())
                .withMessageType(CMS_CONTENT_PUBLISHED)
                .withMessageTimestamp(new Date())
                .withOriginSystemId(systemId)
                .withContentType("application/json")
                .withMessageBody(objectMapper.writeValueAsString(messageBody))
                .build();

        msg.addCustomMessageHeader(TRANSACTION_ID_HEADER, transactionId);
        msg = KeyedMessage.forMessageAndKey(msg, uuid);

        return msg;
    }

    private WordPressContentMapper<?> mapperFor(Post post) {
        WordPressPostType wordPressPostType = null;
        try {
            wordPressPostType = WordPressPostType.fromString(post.getType());
        } catch (IllegalArgumentException e) {/* ignore and throw as below */}

        if (wordPressPostType == null) {
            throw new WordPressContentTypeException("Unsupported blog post type");
        }

        switch (wordPressPostType) {
            case POST:
                return blogTransformer;
            case MARKETS_LIVE:
            case LIVE_Q_AND_A:
            case LIVE_BLOG:
                return liveBlogTransformer;
            default:
                throw new WordPressContentTypeException("Unsupported blog post type");
        }
    }

    private void handleJsonProcessingException(JsonProcessingException e) {
        LOG.error("unable to write JSON for message", e);
        throw new WordPressContentException("unable to write JSON for message", e);
    }
}
