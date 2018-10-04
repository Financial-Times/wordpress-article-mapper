package com.ft.wordpressarticlemapper.transformer;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class ImageExtractorBodyProcessor implements BodyProcessor {

    private static final String P_TAG = "p";
    private static final String IMG_EMPTY_SRC = "//p//img[@src[not(string())]]";
    private static final String IMG_MISSING_SRC = "//p//img[not(@src)]";
    private static final String IMG_INSIDE_A_TAG = "//p//a/img";
    private static final String IMG_INSIDE_PARAGRAPH_TAG = "//p/img";

    @Override
    public String process(String body, BodyProcessingContext bodyProcessingContext) throws BodyProcessingException {
        if (StringUtils.isBlank(body)) {
            return body;
        }

        try {
            DocumentBuilder documentBuilder = getDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(body)));
            XPath xPath = XPathFactory.newInstance().newXPath();

            deleteNodeIncludingAncestors(IMG_EMPTY_SRC, xPath, document);
            deleteNodeIncludingAncestors(IMG_MISSING_SRC, xPath, document);
            paragraphImageExtractWithAncestorsDeletion(IMG_INSIDE_PARAGRAPH_TAG, xPath, document);
            paragraphImageExtractWithAncestorsDeletion(IMG_INSIDE_A_TAG, xPath, document);

            body = serializeBody(document);
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException | XPathExpressionException e) {
            throw new BodyProcessingException(e);
        }
        return body;
    }

    private void deleteNodeIncludingAncestors(String expression, XPath xPath, Document document) throws XPathExpressionException {
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node imgNode = nodeList.item(i);
            Node imgNodeAncestor = imgNode;
            while (isNotParagraph(imgNodeAncestor.getParentNode())) {
                imgNodeAncestor = imgNodeAncestor.getParentNode();
            }
            Node paragraphNode = imgNodeAncestor.getParentNode();
            paragraphNode.removeChild(imgNodeAncestor);
        }
    }

    private void paragraphImageExtractWithAncestorsDeletion(String expression, XPath xPath, Document document) throws XPathExpressionException {
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
        Set<Node> nodesToDelete = new HashSet<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node imgNode = nodeList.item(i);
            Node imgNodeAncestor = imgNode;
            while (isNotParagraph(imgNodeAncestor.getParentNode())) {
                imgNodeAncestor = imgNodeAncestor.getParentNode();
            }
            nodesToDelete.add(imgNodeAncestor);
            Node paragraphNode = imgNodeAncestor.getParentNode();
            Node paragraphParentNode = paragraphNode.getParentNode();
            Node imageNodeCopy = imgNode.cloneNode(true);
            paragraphParentNode.insertBefore(imageNodeCopy, paragraphNode);
        }

        for (Node nodeToDelete : nodesToDelete) {
            Node parentNode = nodeToDelete.getParentNode();
            parentNode.removeChild(nodeToDelete);
        }
    }

    private boolean isNotParagraph(Node node) {
        return !P_TAG.equals(node.getNodeName());
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        return builderFactory.newDocumentBuilder();
    }

    private String serializeBody(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        transformer.transform(domSource, result);

        writer.flush();
        return writer.toString();
    }
}
