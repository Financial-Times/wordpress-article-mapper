package com.ft.wordpressarticletransformer.transformer.html;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;
import com.ft.bodyprocessing.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.StringTokenizer;


/**
 * RemoveEmptyElementsBodyProcessor
 *
 * @author Simon
 */
public class FloatingTextWrappingBodyProcessor implements BodyProcessor {

	public FloatingTextWrappingBodyProcessor() {

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

        NodeList children = body.getChildNodes();
        for(int i=0;i<children.getLength();i++) {
            Node node = children.item(i);
            if(node.getNodeType()==Node.TEXT_NODE) {

                Node insertionPoint = node.getNextSibling();

                StringTokenizer paragraphs = new StringTokenizer(node.getNodeValue(),"\r\n");
                while(paragraphs.hasMoreTokens()) {
                    String paragraphText = paragraphs.nextToken();
                    Element paragraphElement = doc.createElement("p");

                    paragraphElement.appendChild(doc.createTextNode(paragraphText));

                    body.insertBefore(paragraphElement, insertionPoint);
                    if(paragraphs.hasMoreTokens()) {
                        body.insertBefore(doc.createTextNode(System.lineSeparator()),insertionPoint); // replace the delimiter
                    }
                }

                body.removeChild(node);

            }
        }

		if(!body.hasChildNodes()) {
			return "";
		}

		return Xml.writeToString(body);
	}



}
