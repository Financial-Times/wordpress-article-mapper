package com.ft.wordpressarticletransformer.transformer.html;

import java.util.List;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;
import com.ft.bodyprocessing.Xml;
import com.google.common.base.Strings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

		if(bodyHtml==null) {
			throw new BodyProcessingException("Body is null");
		}

		if("".equals(bodyHtml.trim())) {
			return "";
		}

		Document doc = Xml.createDocument(bodyHtml);
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

		if(!body.hasChildNodes()) {
			return "";
		}

		return Xml.writeToString(body);
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



}
