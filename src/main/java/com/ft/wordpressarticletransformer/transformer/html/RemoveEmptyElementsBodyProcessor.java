package com.ft.wordpressarticletransformer.transformer.html;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;
import com.google.common.base.Strings;
import com.sun.org.apache.xalan.internal.xsltc.trax.SAX2DOM;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
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
import java.util.List;

/**
 * RemoveEmptyElementsBodyProcessor
 *
 * @author Simon
 */
public class RemoveEmptyElementsBodyProcessor implements BodyProcessor {

	private final List<String> removableElements;
	private final List<String> nonTextContentElements;

	public RemoveEmptyElementsBodyProcessor(List<String> removableElements, List<String> nonTextContentElements) {
		this.removableElements = removableElements;
		this.nonTextContentElements = nonTextContentElements;
	}

	@Override
	public String process(String bodyHtml, BodyProcessingContext bodyProcessingContext) throws BodyProcessingException {
		Document doc = createDocument(bodyHtml);
		Element body = (Element) doc.getElementsByTagName("body").item(0);

		int removedElements;
		do {
			removedElements = 0;
			for(String elementName : removableElements) {
				NodeList elements =  body.getElementsByTagName(elementName);
				for(int i = 0; i < elements.getLength(); i++) {
					Element element = (Element) elements.item(i);

					if(hasNonTextContent(element)) {
						continue;
					}

					if(blankNullOrEmpty(element)) {
						element.getParentNode().removeChild(element);
						removedElements++;
					}
				}
			}
		} while(removedElements>0);

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

	private boolean hasNonTextContent(Element element) {
		int nonTextContentElementCount = 0;
		for(String nonTextElement : nonTextContentElements) {
			nonTextContentElementCount += element.getElementsByTagName(nonTextElement).getLength();
		}

		return nonTextContentElementCount>0;
	}

	private boolean blankNullOrEmpty(Element element) {
		return Strings.isNullOrEmpty(Strings.nullToEmpty(element.getTextContent()).trim());
	}

	private Document createDocument(String html) throws BodyProcessingException {

		try {
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(html));
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);

		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new BodyProcessingException(e);
		}
	}

}
