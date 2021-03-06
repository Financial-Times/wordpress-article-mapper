package com.ft.wordpressarticlemapper.transformer.eventhandlers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.wordpressarticlemapper.transformer.BaseXMLEventHandlerTest;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClassVideoContainerXMLEventHandlerTest extends BaseXMLEventHandlerTest {

  private ClassVideoContainerXMLEventHandler eventHandler;

  @Mock private BaseXMLEventHandler fallBackHandler;
  @Mock private XMLEventReader mockXMLEventReader;
  @Mock private BodyWriter mockBodyWriter;
  @Mock private BodyProcessingContext mockBodyProcessingContext;

  private static final String ORIGINAL_ELEMENT = "div";
  private static final String ORIGINAL_ELEMENT_ATTRIBUTE = "class";
  private static final String TARGETED_CLASS_VALUE = "video-container";
  private static final String INCORRECT_TARGETED_CLASS_VALUE = "wrong-value";
  private static final String VIDEO_ID_ATTRIBUTE = "data-asset-ref";
  private static final String BRIGHTCOVE_ATTRIBUTE_VALUE = "4035805662001";
  private static final String BRIGHTCOVE_ATTRIBUTE_UUID_VALUE =
      "0ab9ef5d-b98f-3948-a6a7-c26178783cbf";
  private static final String YOUTUBE_ATTRIBUTE_VALUE = "OQ-JR3B_S7o";
  private static final String VIDEO_SOURCE_ATTRIBUTE = "data-asset-source";
  private static final String BRIGHTCOVE_SOURCE = "https://www.ft.com/video/";
  private static final String VIDEO_SOURCE_ATTRIBUTE_BRIGHTCOVE = "Brightcove";
  private static final String FT_VIDEO_SOURCE_ATTRIBUTE = "FTVideo";
  private static final String YOUTUBE_SOURCE = "https://www.youtube.com/watch?v=";
  private static final String VIDEO_SOURCE_ATTRIBUTE_YOUTUBE = "YouTube";
  private static final String TRANSFORMED_ELEMENT = "a";
  private static final String TRANSFORMED_ELEMENT_ATTRIBUTE = "href";

  @Before
  public void setUp() {
    eventHandler = new ClassVideoContainerXMLEventHandler(fallBackHandler);
  }

  @Test
  public void shouldUseFallBackHandlerIfTargetedClassIsNotPresent() throws Exception {
    StartElement startElement = getStartElement(ORIGINAL_ELEMENT);
    eventHandler.handleStartElementEvent(
        startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
    verify(fallBackHandler)
        .handleStartElementEvent(
            startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
  }

  @Test
  public void shouldUseFallBackHandlerIfTargetedClassDoesNotHaveExpectedAttributes()
      throws Exception {
    Map<String, String> attributes = new HashMap<>();
    attributes.put(ORIGINAL_ELEMENT_ATTRIBUTE, INCORRECT_TARGETED_CLASS_VALUE);
    StartElement startElement = getStartElementWithAttributes(ORIGINAL_ELEMENT, attributes);
    eventHandler.handleStartElementEvent(
        startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
    verify(fallBackHandler)
        .handleStartElementEvent(
            startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
  }

  @Test
  public void shouldTransformAndWriteBrightcoveContentIfAllConditionsAreMet() throws Exception {
    Map<String, String> firstElementAttributes = new HashMap<>();
    firstElementAttributes.put(ORIGINAL_ELEMENT_ATTRIBUTE, TARGETED_CLASS_VALUE);

    Map<String, String> secondElementAttributes = new HashMap<>();
    secondElementAttributes.put(VIDEO_ID_ATTRIBUTE, BRIGHTCOVE_ATTRIBUTE_VALUE);
    secondElementAttributes.put(VIDEO_SOURCE_ATTRIBUTE, VIDEO_SOURCE_ATTRIBUTE_BRIGHTCOVE);

    String videoUrl = BRIGHTCOVE_SOURCE + BRIGHTCOVE_ATTRIBUTE_UUID_VALUE;
    Map<String, String> transformedElementAttributes = new HashMap<>();
    transformedElementAttributes.put(TRANSFORMED_ELEMENT_ATTRIBUTE, videoUrl);
    transformedElementAttributes.put(
        ClassVideoContainerXMLEventHandler.DATA_ASSET_TYPE,
        ClassVideoContainerXMLEventHandler.VIDEO);
    transformedElementAttributes.put(
        ClassVideoContainerXMLEventHandler.DATA_EMBEDDED, ClassVideoContainerXMLEventHandler.TRUE);

    StartElement firstElement =
        getStartElementWithAttributes(ORIGINAL_ELEMENT, firstElementAttributes);
    StartElement secondElement =
        getStartElementWithAttributes(ORIGINAL_ELEMENT, secondElementAttributes);
    EndElement closeSecondElement = getEndElement(ORIGINAL_ELEMENT);
    EndElement closeFirstElement = getEndElement(ORIGINAL_ELEMENT);

    when(mockXMLEventReader.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true);
    when(mockXMLEventReader.nextEvent())
        .thenReturn(secondElement)
        .thenReturn(closeSecondElement)
        .thenReturn(closeFirstElement);
    eventHandler.handleStartElementEvent(
        firstElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
    verify(mockBodyWriter).writeStartTag(TRANSFORMED_ELEMENT, transformedElementAttributes);
    verify(mockBodyWriter).writeEndTag(TRANSFORMED_ELEMENT);
  }

  public void shouldTransformAndWriteFTVideoContentIfAllConditionsAreMet() throws Exception {
    Map<String, String> firstElementAttributes = new HashMap<>();
    firstElementAttributes.put(ORIGINAL_ELEMENT_ATTRIBUTE, TARGETED_CLASS_VALUE);

    Map<String, String> secondElementAttributes = new HashMap<>();
    secondElementAttributes.put(VIDEO_ID_ATTRIBUTE, BRIGHTCOVE_ATTRIBUTE_UUID_VALUE);
    secondElementAttributes.put(VIDEO_SOURCE_ATTRIBUTE, FT_VIDEO_SOURCE_ATTRIBUTE);

    String videoUrl = BRIGHTCOVE_SOURCE + BRIGHTCOVE_ATTRIBUTE_UUID_VALUE;
    Map<String, String> transformedElementAttributes = new HashMap<>();
    transformedElementAttributes.put(TRANSFORMED_ELEMENT_ATTRIBUTE, videoUrl);
    transformedElementAttributes.put(
        ClassVideoContainerXMLEventHandler.DATA_ASSET_TYPE,
        ClassVideoContainerXMLEventHandler.VIDEO);
    transformedElementAttributes.put(
        ClassVideoContainerXMLEventHandler.DATA_EMBEDDED, ClassVideoContainerXMLEventHandler.TRUE);

    StartElement firstElement =
        getStartElementWithAttributes(ORIGINAL_ELEMENT, firstElementAttributes);
    StartElement secondElement =
        getStartElementWithAttributes(ORIGINAL_ELEMENT, secondElementAttributes);
    EndElement closeSecondElement = getEndElement(ORIGINAL_ELEMENT);
    EndElement closeFirstElement = getEndElement(ORIGINAL_ELEMENT);

    when(mockXMLEventReader.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true);
    when(mockXMLEventReader.nextEvent())
        .thenReturn(secondElement)
        .thenReturn(closeSecondElement)
        .thenReturn(closeFirstElement);
    eventHandler.handleStartElementEvent(
        firstElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
    verify(mockBodyWriter).writeStartTag(TRANSFORMED_ELEMENT, transformedElementAttributes);
    verify(mockBodyWriter).writeEndTag(TRANSFORMED_ELEMENT);
  }

  @Test
  public void shouldTransformAndWriteYoutubeContentIfAllConditionsAreMet() throws Exception {
    Map<String, String> firstElementAttributes = new HashMap<>();
    firstElementAttributes.put(ORIGINAL_ELEMENT_ATTRIBUTE, TARGETED_CLASS_VALUE);

    Map<String, String> secondElementAttributes = new HashMap<>();
    secondElementAttributes.put(VIDEO_ID_ATTRIBUTE, YOUTUBE_ATTRIBUTE_VALUE);
    secondElementAttributes.put(VIDEO_SOURCE_ATTRIBUTE, VIDEO_SOURCE_ATTRIBUTE_YOUTUBE);

    String videoUrl = YOUTUBE_SOURCE + YOUTUBE_ATTRIBUTE_VALUE;
    Map<String, String> transformedElementAttributes = new HashMap<>();
    transformedElementAttributes.put(TRANSFORMED_ELEMENT_ATTRIBUTE, videoUrl);
    transformedElementAttributes.put(
        ClassVideoContainerXMLEventHandler.DATA_ASSET_TYPE,
        ClassVideoContainerXMLEventHandler.VIDEO);
    transformedElementAttributes.put(
        ClassVideoContainerXMLEventHandler.DATA_EMBEDDED, ClassVideoContainerXMLEventHandler.TRUE);

    StartElement firstElement =
        getStartElementWithAttributes(ORIGINAL_ELEMENT, firstElementAttributes);
    StartElement secondElement =
        getStartElementWithAttributes(ORIGINAL_ELEMENT, secondElementAttributes);
    EndElement closeSecondElement = getEndElement(ORIGINAL_ELEMENT);
    EndElement closeFirstElement = getEndElement(ORIGINAL_ELEMENT);

    when(mockXMLEventReader.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true);
    when(mockXMLEventReader.nextEvent())
        .thenReturn(secondElement)
        .thenReturn(closeSecondElement)
        .thenReturn(closeFirstElement);
    eventHandler.handleStartElementEvent(
        firstElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
    verify(mockBodyWriter).writeStartTag(TRANSFORMED_ELEMENT, transformedElementAttributes);
    verify(mockBodyWriter).writeEndTag(TRANSFORMED_ELEMENT);
  }
}
