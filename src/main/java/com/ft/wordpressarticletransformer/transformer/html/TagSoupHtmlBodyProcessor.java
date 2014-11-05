package com.ft.wordpressarticletransformer.transformer.html;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;

import com.ft.wordpressarticletransformer.transformer.Xml;
import org.apache.xalan.xsltc.trax.SAX2DOM;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.StringReader;

public class TagSoupHtmlBodyProcessor implements BodyProcessor {
    @Override
    public String process(String bodyHtml, BodyProcessingContext bodyProcessingContext) throws BodyProcessingException {

		if(bodyHtml==null) {
			throw new BodyProcessingException("Body is null");
		}

		if("".equals(bodyHtml.trim())) {
			return "";
		}

        Document doc = createDocument(bodyHtml);
        Element body = (Element) doc.getElementsByTagName("body").item(0);

		return Xml.writeToString(body);
	}



	private Document createDocument(String html) throws BodyProcessingException {
        Parser parser = new Parser();
        try {
            SAX2DOM sax2dom = new SAX2DOM();

            parser.setFeature(Parser.namespacesFeature, false);
            parser.setFeature(Parser.namespacePrefixesFeature, false);
            parser.setContentHandler(sax2dom);
            parser.parse(new InputSource(new StringReader(html)));
            return (Document) sax2dom.getDOM();

        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new BodyProcessingException(e);
        }
    }
}
