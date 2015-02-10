package com.ft.wordpressarticletransformer.transformer.eventhandlers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;

public class WordpressVideoXMLEventHandler extends BaseXMLEventHandler {

    private String targetedHtmlClass;

    private static final String VIDEO_SOURCE_ATTRIBUTE = "data-asset-source";
    private static final String VIDEO_ID_ATTRIBUTE = "data-asset-ref";
    private static final String NEW_ELEMENT = "a";
    private static final String NEW_ELEMENT_ATTRIBUTE = "href";
    private Map<String, String> attributesToAdd;
    private XMLEventHandler fallbackHandler;

    private Map<String, String> sourceToUrlMap;


    public WordpressVideoXMLEventHandler(String targetedHtmlClass, XMLEventHandler fallbackHandler) {
        this.targetedHtmlClass = targetedHtmlClass;
        this.fallbackHandler = fallbackHandler;
        sourceToUrlMap = new HashMap<String, String>();
        sourceToUrlMap.put("Brightcove", "http://video.ft.com/%s");
        sourceToUrlMap.put("YouTube", "http://www.youtube.com/embed/%s?wmode=transparent");
    }

    @Override
    public void handleStartElementEvent(StartElement event, XMLEventReader xmlEventReader, BodyWriter eventWriter,
                                        BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        if(!isTargetedClass(event)) {
            fallbackHandler.handleStartElementEvent(event, xmlEventReader, eventWriter, bodyProcessingContext);
            return;
        }


        XMLEvent found = getEventAndSkipBlock(xmlEventReader, "div", "div", VIDEO_ID_ATTRIBUTE, "[.a-zA-Z0-9]*");


        String source = found.asStartElement().getAttributeByName(QName.valueOf(VIDEO_SOURCE_ATTRIBUTE)).getValue();
        String id = found.asStartElement().getAttributeByName(QName.valueOf(VIDEO_ID_ATTRIBUTE)).getValue();

        if(sourceToUrlMap.get(source)==null || source == null || id == null){
            return;//fallback
        }
        String videoUrl = String.format(sourceToUrlMap.get(source), id);
        attributesToAdd = new HashMap<String, String>();
        attributesToAdd.put(NEW_ELEMENT_ATTRIBUTE, videoUrl);

        eventWriter.writeStartTag(NEW_ELEMENT, attributesToAdd);
        eventWriter.writeEndTag(NEW_ELEMENT);
    }

    @Override
    public void handleEndElementEvent(EndElement event, XMLEventReader xmlEventReader, BodyWriter eventWriter) throws XMLStreamException {
        fallbackHandler.handleEndElementEvent(event, xmlEventReader, eventWriter);
    }

    private boolean isTargetedClass(StartElement event) {
        Attribute classesAttr = event.getAttributeByName(QName.valueOf("class"));
        if(classesAttr==null) {
            return false;
        }

        List<String> classes = Arrays.asList(classesAttr.getValue().split(" "));

        return classes.contains(targetedHtmlClass);
    }


    private XMLEvent getEventAndSkipBlock(XMLEventReader reader, String primaryElementName, String secondaryElementName,
                                                       String secondaryElementAttributeName, String secondaryElementAttributeValueRegex)
            throws XMLStreamException {

        XMLEvent foundSecondaryStartElementEvent = null;
        int primaryOpenElementNameCount = 1; // One, not zero, as we are already in the element.

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isStartElement()) {
                StartElement newStartElement = nextEvent.asStartElement();
                if((primaryElementName).equals(newStartElement.getName().getLocalPart())) {
                    primaryOpenElementNameCount++;
                }

                if ((secondaryElementName).equals(newStartElement.getName().getLocalPart())) {
                    Attribute attribute = newStartElement.getAttributeByName(QName.valueOf(secondaryElementAttributeName));
                    if (attribute!=null && Pattern.matches(secondaryElementAttributeValueRegex, attribute.getValue())) {
                        foundSecondaryStartElementEvent = nextEvent;
                    }
                }
            }
            if(nextEvent.isEndElement()){
                EndElement newEndElement = nextEvent.asEndElement();
                if ((primaryElementName).equals(newEndElement.getName().getLocalPart()) ) {
                    if(primaryOpenElementNameCount ==1){
                        return foundSecondaryStartElementEvent;
                    }
                    primaryOpenElementNameCount--;
                }

            }
        }
        throw new BodyProcessingException("Reached end without encountering closing primary tag : " + primaryElementName);

    }


}
