package com.ft.wordpressarticlemapper.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.message.consumer.MessageListener;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.wordpressarticlemapper.exception.PostNotFoundException;
import com.ft.wordpressarticlemapper.exception.WordPressContentException;
import com.ft.wordpressarticlemapper.response.NativeWordPressContent;
import com.ft.wordpressarticlemapper.validation.NativeWordPressContentValidator;
import java.io.IOException;
import java.util.Date;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeCmsPublicationEventsListener implements MessageListener {

  private static final Logger LOG =
      LoggerFactory.getLogger(NativeCmsPublicationEventsListener.class);

  private final Predicate<Message> filter;
  private final ObjectMapper objectMapper;
  private final SystemId systemId;
  private final MessageProducingContentMapper contentMapper;
  private final NativeWordPressContentValidator nativeWordPressContentValidator;

  public NativeCmsPublicationEventsListener(
      MessageProducingContentMapper contentMapper,
      ObjectMapper objectMapper,
      String systemCode,
      NativeWordPressContentValidator nativeWordPressContentValidator) {
    this.contentMapper = contentMapper;
    this.systemId = SystemId.systemIdFromCode(systemCode);
    this.filter = msg -> (systemId.equals(msg.getOriginSystemId()));
    this.objectMapper = objectMapper;
    this.nativeWordPressContentValidator = nativeWordPressContentValidator;
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
      content =
          objectMapper.reader(NativeWordPressContent.class).readValue(message.getMessageBody());
      try {
        nativeWordPressContentValidator.validate(content);
        contentMapper.mapForPublish(transactionId, content.getPost(), lastModified);
      } catch (PostNotFoundException e) {
        contentMapper.mapForDelete(content.getPost().getUuid(), lastModified, transactionId);
      }
    } catch (IOException e) {
      throw new WordPressContentException("Unable to parse Wordpress content message", e);
    }
  }
}
