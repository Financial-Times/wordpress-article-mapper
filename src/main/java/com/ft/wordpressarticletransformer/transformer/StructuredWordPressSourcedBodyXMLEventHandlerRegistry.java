package com.ft.wordpressarticletransformer.transformer;

import com.ft.bodyprocessing.xml.eventhandlers.*;
import com.ft.wordpressarticletransformer.transformer.eventhandlers.StripEmbeddedTweetXMLEventHandler;

/**
 * StructuredWordPressSourcedBodyXMLEventHandlerRegistry
 *
 * @author Simon
 */
public class StructuredWordPressSourcedBodyXMLEventHandlerRegistry extends XMLEventHandlerRegistry {



	public StructuredWordPressSourcedBodyXMLEventHandlerRegistry() {
		//default is to skip events - any start or end tags or entities not configured below will be excluded, as will comments
		super.registerDefaultEventHandler(new StripXMLEventHandler());

		super.registerStartAndEndElementEventHandler(tweetHandlerWithFallbackTo(new RetainWithoutAttributesXMLEventHandler()),"blockquote");

		//tags to include
		super.registerStartAndEndElementEventHandler(new RetainWithoutAttributesXMLEventHandler(),
				"body",
				"h1","h2", "h3", "h4", "h5", "h6",
				"ol", "ul", "li",
				"br", "strong", "em", "small", "sub", "sup", "u",
				"del", "p",
				"itemBody"); // itemBody included as it will be a root node wrapping the body text so that the xml being written out is valid

		// to be retained with attributes
		super.registerStartElementEventHandler(new LinkTagXMLEventHandler(), "a");
		super.registerEndElementEventHandler(new LinkTagXMLEventHandler(), "a");
		super.registerStartAndEndElementEventHandler(removeForTheTimeBeing(), "img");


		registerStartAndEndElementEventHandler(
				videoHandlerWithFallbackTo(
						captionedImageHandlerWithFallbackTo(
								new BaseXMLEventHandler()
						)
				),
			"div");


		// to be transformed
		super.registerStartAndEndElementEventHandler(new SimpleTransformTagXmlEventHandler("strong"), "b");
		super.registerStartAndEndElementEventHandler(new SimpleTransformTagXmlEventHandler("em"), "i");
		super.registerStartAndEndElementEventHandler(new SimpleTransformTagXmlEventHandler("del"), "s");
		super.registerStartAndEndElementEventHandler(new SimpleTransformTagXmlEventHandler("del"), "strike");

		//html5 tags to remove with all contents
		super.registerStartElementEventHandler(new StripElementAndContentsXMLEventHandler(),
				"applet", "audio",
				"base", "basefont", "button",
				"canvas", "caption",  "col", "colgroup", "command",
				"datalist", "dir",
				"embed",
				"fieldset", "form", "frame", "frameset",
				"head",
				"iframe", "input",
				"keygen",
				"label", "legend", "link",
				"map", "menu", "meta",
				"nav", "noframes", "noscript",
				"object", "optgroup",
				"option", "output",
				"param", "progress",
				"rp", "rt", "ruby",
				"script", "select", "source", "style", "table",
				"tbody", "td", "textarea", "tfoot", "th", "thead", "tr", "track",
				"video",
				"wbr");

		// OLD HANDLING RETAINED as at Nov 2014 as it is expected archives MAY contain it.
		// TODO - replace this with the correct handling for tweets. It's here now because otherwise we get
		// parts of stuff between the comments output
		// for embedded tweets, strip everything between the initial and final comments. Any other comments will just be removed.
		super.registerCommentsEventHandler(new StripEmbeddedTweetXMLEventHandler());
		// END scope of previous comment re OLD HANDLING

		// characters (i.e. normal text) will be output
		super.registerCharactersEventHandler(new RetainXMLEventHandler());
		// specific entity references should be retained
		super.registerEntityReferenceEventHandler(new PlainTextHtmlEntityReferenceEventHandler());  // consistent with FastFT, NOT V1
	}

	private XMLEventHandler captionedImageHandlerWithFallbackTo(XMLEventHandler xmlEventHandler) {
		return new StripElementByClassEventHandler("wp-caption",xmlEventHandler);
	}

	private XMLEventHandler videoHandlerWithFallbackTo(XMLEventHandler fallbackHandler) {
		return new StripElementByClassEventHandler("morevideo",fallbackHandler);
	}

	private XMLEventHandler tweetHandlerWithFallbackTo(BaseXMLEventHandler baseXMLEventHandler) {
		return new RetainElementByClassEventHandler("twitter-tweet", baseXMLEventHandler);
	}

	private XMLEventHandler removeForTheTimeBeing() {
		return new StripXMLEventHandler();
	}



}
