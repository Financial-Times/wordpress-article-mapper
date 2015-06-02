package com.ft.wordpressarticletransformer.transformer.eventhandlers;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.richcontent.RichContentItem;
import com.ft.bodyprocessing.richcontent.Video;
import com.ft.bodyprocessing.richcontent.VideoMatcher;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.StripElementAndContentsXMLEventHandler;
import com.ft.wordpressarticletransformer.transformer.BaseXMLEventHandlerTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IframeSrcVideoXMLEventHandlerTest extends BaseXMLEventHandlerTest {

    private IframeSrcVideoXMLEventHandler eventHandler;
    private Video video;

    @Mock private StripElementAndContentsXMLEventHandler fallbackHandler;
    @Mock private XMLEventReader mockXMLEventReader;
    @Mock private BodyWriter mockBodyWriter;
    @Mock private BodyProcessingContext mockBodyProcessingContext;
    @Mock private VideoMatcher videoMatcher;

    private static final String IFRAME = "iframe";
    private static final String STANDARDIZED_VIMEO = "https://www.vimeo.com/77761436";
    private static final String STANDARDIZED_YOUTUBE = "https://www.youtube.com/watch?v=OQzJR3BqS7o";
    private static final String NEW_ELEMENT = "a";
    private static final String NEW_ELEMENT_ATTRIBUTE = "href";
    private static final String DATA_ASSET_TYPE = "data-asset-type";
    private static final String VIDEO = "video";
    private static final String DATA_EMBEDDED = "data-embedded";
    private static final String TRUE = "true";

    @Before
    public void setUp() {
        eventHandler = new IframeSrcVideoXMLEventHandler(fallbackHandler, videoMatcher);
        video = new Video();
    }

    @Test
    public void shouldUseFallbackHandlerIfSrcAttributeNotFound() throws Exception {
        StartElement startElement = getStartElement(IFRAME);
        eventHandler.handleStartElementEvent(startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(fallbackHandler).handleStartElementEvent(startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldUseFallbackHandlerIfSrcIsNotVimeoOrBrightcove() throws Exception {
        StartElement startElement = getCompactStartElement("<iframe src=\"http://www.dailymotion.com/video/x2gsis0_the-best-of-the-2015-grammys_lifestyle\" width=\"500\" height=\"208\" frameborder=\"0\"></iframe>", "iframe");
        eventHandler.handleStartElementEvent(startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(fallbackHandler).handleStartElementEvent(startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldTransformAndWriteVimeoContentIfAllConditionsAreMet() throws Exception {
        StartElement startElement = getCompactStartElement("<iframe src=\"//player.vimeo.com/video/77761436\" width=\"500\" height=\"208\" frameborder=\"0\"></iframe>", "iframe");

        Map<String, String> transformedElementAttributes = new HashMap<>();
        transformedElementAttributes.put(NEW_ELEMENT_ATTRIBUTE, STANDARDIZED_VIMEO);
        transformedElementAttributes.put(DATA_ASSET_TYPE, VIDEO);
        transformedElementAttributes.put(DATA_EMBEDDED, TRUE);

        video.setUrl(STANDARDIZED_VIMEO);
        video.setEmbedded(true);
        video.setTitle(null);

        when(videoMatcher.filterVideo(any(RichContentItem.class))).thenReturn(video);
        eventHandler.handleStartElementEvent(startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag(NEW_ELEMENT, transformedElementAttributes);
        verify(mockBodyWriter).writeEndTag(NEW_ELEMENT);
    }

    @Test
    public void shouldTransformAndWriteYoutubeContentIfAllConditionsAreMet() throws Exception {
        StartElement startElement = getCompactStartElement("<iframe src=\"http://www.youtube.com/embed/OQzJR3BqS7o\" width=\"500\" height=\"208\" frameborder=\"0\"></iframe>", "iframe");

        Map<String, String> transformedElementAttributes = new HashMap<>();
        transformedElementAttributes.put(NEW_ELEMENT_ATTRIBUTE, STANDARDIZED_YOUTUBE);
        transformedElementAttributes.put(DATA_ASSET_TYPE, VIDEO);
        transformedElementAttributes.put(DATA_EMBEDDED, TRUE);

        video.setUrl(STANDARDIZED_YOUTUBE);
        video.setEmbedded(true);
        video.setTitle(null);

        when(videoMatcher.filterVideo(any(RichContentItem.class))).thenReturn(video);
        eventHandler.handleStartElementEvent(startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag(NEW_ELEMENT, transformedElementAttributes);
        verify(mockBodyWriter).writeEndTag(NEW_ELEMENT);
    }
}
