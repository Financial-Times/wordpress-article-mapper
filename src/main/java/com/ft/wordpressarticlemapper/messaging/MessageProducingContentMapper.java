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
import java.util.*;
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


    public WordPressContent getWordPressArticleMessage(String transactionId, Post post, Date lastModified) {
        List<WordPressContent> contents = Collections.singletonList(
                mapperFor(post).mapWordPressArticle(transactionId, post, lastModified));
        producer.send(contents.stream().map(this::createMessage).collect(Collectors.toList()));
        LOG.info("sent {} messages", contents.size());
        return contents.get(0);
    }

    private Message createMessage(WordPressContent content) {
        Message msg;
        LOG.info("Last Modified Date is: " + content.getLastModified());

        Map<String, Object> messageBody = new LinkedHashMap<>();
        messageBody.put("contentUri", contentUriBuilder.build(content.getUuid()).toString());
        messageBody.put("payload", content);
        String lastModified = RFC3339_FMT.format(OffsetDateTime.ofInstant(content.getLastModified().toInstant(), UTC));
        messageBody.put("lastModified", lastModified);

        try {
            msg = new Message.Builder().withMessageId(UUID.randomUUID())
                    .withMessageType(CMS_CONTENT_PUBLISHED)
                    .withMessageTimestamp(new Date())
                    .withOriginSystemId(systemId)
                    .withContentType("application/json")
                    .withMessageBody(objectMapper.writeValueAsString(messageBody))
                    .build();

            msg.addCustomMessageHeader(TRANSACTION_ID_HEADER, content.getPublishReference());

            msg = KeyedMessage.forMessageAndKey(msg, content.getUuid());
        } catch (JsonProcessingException e) {
            LOG.error("unable to write JSON for message", e);
            throw new WordPressContentException("unable to write JSON for message", e);
        }
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
}
