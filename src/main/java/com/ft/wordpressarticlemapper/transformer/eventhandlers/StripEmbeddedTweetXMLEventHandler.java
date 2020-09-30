package com.ft.wordpressarticlemapper.transformer.eventhandlers;

import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.XMLEvent;

public class StripEmbeddedTweetXMLEventHandler extends BaseXMLEventHandler {

  private static final String END_OF_TWEET_MARKER = "end of tweet";
  private static final String START_TWEET_MARKER = "tweet id :";
  XMLEventHandler fallbackEventHandler;

  @Override
  public void handleCommentEvent(
      Comment event, XMLEventReader xmlEventReader, BodyWriter eventWriter)
      throws XMLStreamException {
    String commentText = event.getText();
    if (commentText.contains(START_TWEET_MARKER)) {
      skipUntilMatchingEndComment(xmlEventReader);
    }
    // else, we strip it
  }

  private void skipUntilMatchingEndComment(XMLEventReader xmlEventReader)
      throws XMLStreamException {
    while (xmlEventReader.hasNext()) {
      XMLEvent event = xmlEventReader.nextEvent();
      if (event.getEventType() == XMLEvent.COMMENT) {
        Comment comment = (Comment) event;
        String commentText = comment.getText();
        if (commentText.contains(END_OF_TWEET_MARKER)) {
          return;
        }
      }
    }
  }
}
