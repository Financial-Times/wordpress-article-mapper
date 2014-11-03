package com.ft.wordpressarticletransformer.transformer.html;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;
import com.google.common.collect.ImmutableMap;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.evt.XMLEventFactory2;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;


import static com.ft.wordpressarticletransformer.transformer.html.AttributeNameValuePredicate.attributeWithNameAndValue;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterators.filter;

public class TagSoupCleanupHtmlBodyProcessor implements BodyProcessor {

    private XMLInputFactory xmlInputFactory = XMLInputFactory2.newInstance();
    private XMLOutputFactory xmlOutputFactory = XMLOutputFactory2.newInstance();
    private XMLEventFactory xmlEventFactory = XMLEventFactory2.newInstance();

    @Override
    public String process(String bodyHtml, BodyProcessingContext bodyProcessingContext) throws BodyProcessingException {
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(bodyHtml));
            StringWriter stringWriter = new StringWriter();
            XMLEventWriter xmlEventWriter = xmlOutputFactory.createXMLEventWriter(stringWriter);


            while (xmlEventReader.hasNext()) {
                XMLEvent event = xmlEventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();

                    String tagName = startElement.getName().getLocalPart();
                    if ("a".equalsIgnoreCase(tagName)) {
                    	xmlEventWriter.add(eventWithoutAttribute(startElement, "shape", "rect"));
                    } else if ("col".equalsIgnoreCase(tagName)) {
                    	xmlEventWriter.add(eventWithoutAttribute(startElement, "span", "1"));
                    } else if ("colgroup".equalsIgnoreCase(tagName)) {
                    	xmlEventWriter.add(eventWithoutAttribute(startElement, "span", "1"));
                    } else if ("td".equalsIgnoreCase(tagName)) {
                    	xmlEventWriter.add(eventWithoutAttributes(startElement, ImmutableMap.of("colspan", "1", "rowspan", "1")));
                    } else if ("th".equalsIgnoreCase(tagName)) {
                    	xmlEventWriter.add(eventWithoutAttributes(startElement, ImmutableMap.of("colspan", "1", "rowspan", "1")));
                    } else if ("br".equalsIgnoreCase(tagName)) {
                    	xmlEventWriter.add(eventWithoutAttribute(startElement, "clear", "none"));
                    } else if ("param".equalsIgnoreCase(tagName)) {
                    	xmlEventWriter.add(eventWithoutAttribute(startElement, "valuetype", "data"));
                    } else {
                    	xmlEventWriter.add(startElement);
                    }
                } else if (!event.isStartDocument()) { // we avoid start doc events to prevent output of xml prolog
                    xmlEventWriter.add(event);
                }
            }
            xmlEventWriter.close();
            return stringWriter.toString();
        } catch (XMLStreamException e) {
            throw new BodyProcessingException(e);
        }
    }

	private XMLEvent eventWithoutAttribute(StartElement startElement, String name, String value) {
		Map<String,String> attributes = ImmutableMap.of(name, value);
    	return eventWithoutAttributes(startElement, attributes);
    }

	private XMLEvent eventWithoutAttributes(StartElement startElement, Map<String,String> attributes) {
		Iterator attributesWithoutAttributeToRemove = filter(startElement.getAttributes(), not(attributeWithNameAndValue(attributes)));
        XMLEvent xmlEventAnchorWithoutShape = xmlEventFactory.createStartElement(startElement.getName(), attributesWithoutAttributeToRemove, startElement.getNamespaces());
        return xmlEventAnchorWithoutShape;
    }


}
