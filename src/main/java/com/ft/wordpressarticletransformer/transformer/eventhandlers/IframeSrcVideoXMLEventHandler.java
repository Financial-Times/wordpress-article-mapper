package com.ft.wordpressarticletransformer.transformer.eventhandlers;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.richcontent.RichContentItem;
import com.ft.bodyprocessing.richcontent.Video;
import com.ft.bodyprocessing.richcontent.VideoMatcher;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import java.util.HashMap;
import java.util.Map;

public class IframeSrcVideoXMLEventHandler extends BaseXMLEventHandler {

    private XMLEventHandler fallbackHandler;
    private VideoMatcher videoMatcher;

    private static final String SRC_ATTRIBUTE = "src";
    private static final String NEW_ELEMENT = "a";
    private static final String NEW_ELEMENT_ATTRIBUTE = "href";
    public static final String DATA_ASSET_TYPE = "data-asset-type";
    public static final String VIDEO = "video";
    public static final String DATA_EMBEDDED = "data-embedded";
    public static final String TRUE = "true";

    public IframeSrcVideoXMLEventHandler(XMLEventHandler fallbackHandler, VideoMatcher videoMatcher) {
        this.fallbackHandler = fallbackHandler;
        this.videoMatcher = videoMatcher;
    }

    @Override
    public void handleStartElementEvent(StartElement event, XMLEventReader xmlEventReader, BodyWriter eventWriter,
                                        BodyProcessingContext bodyProcessingContext) throws XMLStreamException {

        Attribute srcValue = event.getAttributeByName(QName.valueOf(SRC_ATTRIBUTE));

        if (srcValue==null) {
            fallbackHandler.handleStartElementEvent(event, xmlEventReader, eventWriter, bodyProcessingContext);
            return;
        }

        Video video = convertToVideo(srcValue);

        if(video==null) {
            fallbackHandler.handleStartElementEvent(event, xmlEventReader, eventWriter, bodyProcessingContext);
            return;
        }

        Map<String, String> attributesToAdd = new HashMap<>();
        attributesToAdd.put(NEW_ELEMENT_ATTRIBUTE, video.getUrl());
        attributesToAdd.put(DATA_ASSET_TYPE, VIDEO);
        attributesToAdd.put(DATA_EMBEDDED, TRUE);

        eventWriter.writeStartTag(NEW_ELEMENT, attributesToAdd);
        eventWriter.writeEndTag(NEW_ELEMENT);
    }

    private Video convertToVideo(Attribute srcValue) {
        String videoLink = srcValue.getValue();
        RichContentItem attachment = new RichContentItem(videoLink, null);
        Video video = videoMatcher.filterVideo(attachment);
        return video;
    }
}
