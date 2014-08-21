package com.ft.fastfttransformer.transformer;

import com.ft.bodyprocessing.xml.eventhandlers.LinkTagXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.PlainTextHtmlEntityReferenceEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.RetainWithoutAttributesXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.RetainXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.SimpleTransformTagXmlEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.StripElementAndContentsXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.StripXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandlerRegistry;

public class BodyTransformationXMLEventRegistry extends XMLEventHandlerRegistry {

    public BodyTransformationXMLEventRegistry() {

        //default is to skip events but leave content - anything not configured below will be handled via this
        registerDefaultEventHandler(new StripXMLEventHandler());
        registerCharactersEventHandler(new RetainXMLEventHandler());
        registerEntityReferenceEventHandler(new PlainTextHtmlEntityReferenceEventHandler());
        // want to be sure to keep the wrapping node
        registerStartAndEndElementEventHandler(new RetainXMLEventHandler(), "body");
        // strip html5 tags whose bodies we don't want
        registerStartElementEventHandler(new StripElementAndContentsXMLEventHandler(),
                "applet", "audio", "base", "basefont", "button", "canvas", "caption", "col",
                "colgroup", "command", "datalist", "del", "dir", "embed", "fieldset", "form",
                "frame", "frameset", "head", "iframe", "input", "keygen", "label", "legend",
                "link", "map", "menu", "meta", "nav", "noframes", "noscript", "object",
                "optgroup", "option", "output", "param", "progress", "rp", "rt", "ruby",
                "s", "script", "select", "source", "strike", "style", "table", "tbody",
                "td", "textarea", "tfoot", "th", "thead", "tr", "track", "video", "wbr"
        );

        registerStartAndEndElementEventHandler(new SimpleTransformTagXmlEventHandler("strong"), "b");
        registerStartAndEndElementEventHandler(new SimpleTransformTagXmlEventHandler("em"), "i");
        registerStartAndEndElementEventHandler(new LinkTagXMLEventHandler("title","alt"),"a");
        registerStartAndEndElementEventHandler(new RetainWithoutAttributesXMLEventHandler(),
                "strong", "em", "sub", "sup", "br",
                "h1", "h2", "h3", "h4", "h5", "h6",
                "ol", "ul", "li", "p"
        );

    }
}