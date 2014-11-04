package com.ft.wordpressarticletransformer.transformer.html;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;
import com.sun.org.apache.xalan.internal.xsltc.trax.SAX2DOM;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

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

        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(body);
            transformer.transform(source, result);
            return result.getWriter().toString();
        } catch (TransformerException e) {
            throw new BodyProcessingException(e);
        }
    }

    private Document createDocument(String html) throws BodyProcessingException {
        Parser parser = new Parser();
        try {
            SAX2DOM sax2dom = new SAX2DOM(true);

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
