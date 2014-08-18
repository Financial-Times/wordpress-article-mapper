package com.ft.fastfttransformer.transformer;

import static com.ft.bodyprocessing.xml.eventhandlers.RemoveElementEventHandler.attributeNameMatcher;

import com.ft.bodyprocessing.xml.eventhandlers.LinkTagXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.PlainTextHtmlEntityReferenceEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.RemoveElementEventHandler;
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
        // strip methode tags whose bodies we don't want
        registerStartElementEventHandler(new StripElementAndContentsXMLEventHandler(),
                "byline", "editor-choice", "headline", "inlineDwc", "interactive-chart",
                "lead-body", "lead-text", "ln", "photo", "photo-caption", "photo-group",
                "plainHtml", "promo-box", "promo-headline", "promo-image", "promo-intro",
                "promo-link", "promo-title", "promobox-body", "pull-quote", "pull-quote-header",
                "pull-quote-text", "readthrough", "short-body", "skybox-body", "stories",
                "story", "strap", "videoObject", "videoPlayer", "web-alt-picture", "web-background-news",
                "web-background-news-header", "web-background-news-text", "web-inline-picture",
                "web-picture", "web-pull-quote", "web-pull-quote-source", "web-pull-quote-text",
                "web-skybox-picture", "web-subhead", "web-thumbnail", "xref", "xrefs"
        );

        registerStartAndEndElementEventHandler(new SimpleTransformTagXmlEventHandler("strong"), "b");
        registerStartAndEndElementEventHandler(new SimpleTransformTagXmlEventHandler("em"), "i");
        registerStartAndEndElementEventHandler(new LinkTagXMLEventHandler("title","alt"),"a");
        registerStartAndEndElementEventHandler(new RetainWithoutAttributesXMLEventHandler(),
                "strong", "em", "sub", "sup", "br",
                "h1", "h2", "h3", "h4", "h5", "h6",
                "ol", "ul", "li"
        );

        // Handle strikeouts, i.e. where have <p channel="!"> or <span channel="!">
        // For these elements if the attribute is missing use the fallback handler
        registerStartAndEndElementEventHandler(new RemoveElementEventHandler(new RetainWithoutAttributesXMLEventHandler(), attributeNameMatcher("channel")), "p");
        registerStartAndEndElementEventHandler(new RemoveElementEventHandler(new StripXMLEventHandler(), attributeNameMatcher("channel")), "span");

    }
}