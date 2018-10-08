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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private static final String IMG_INSIDE_PARAGRAPH_TAG = "//p//img";

    private static final List<String> TAGS_TO_DELETE = new ArrayList<String>() {
        {
            add("a");
            add("span");
            add("img");
        }
    };

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
            paragraphImageExtractWithAncestorsDeletion(xPath, document);

            body = serializeBody(document);
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException | XPathExpressionException e) {
            throw new BodyProcessingException(e);
        }
        return body;
    }

    private void deleteNodeIncludingAncestors(String expression, XPath xPath, Document document) throws XPathExpressionException {
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
        Set<Node> nodesToDelete = new HashSet<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node deletableNode = nodeList.item(i);

            while (nodeCanBeDeleted(deletableNode.getParentNode())) {
                deletableNode = deletableNode.getParentNode();
            }
            nodesToDelete.add(deletableNode);
        }

        for (Node nodeToDelete : nodesToDelete) {
            Node parentNode = nodeToDelete.getParentNode();
            parentNode.removeChild(nodeToDelete);
        }
    }

    private void paragraphImageExtractWithAncestorsDeletion(XPath xPath, Document document) throws XPathExpressionException {
        NodeList nodeList = (NodeList) xPath.compile(IMG_INSIDE_PARAGRAPH_TAG).evaluate(document, XPathConstants.NODESET);
        Set<Node> nodesToDelete = new HashSet<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node imgNode = nodeList.item(i);
            Node deletableNode = imgNode;

            while (nodeCanBeDeleted(deletableNode.getParentNode())) {
                deletableNode = deletableNode.getParentNode();
            }
            nodesToDelete.add(deletableNode);

            Node paragraphNode = getParagraphNode(deletableNode);
            Node paragraphParentNode = paragraphNode.getParentNode();
            Node imageNodeCopy = imgNode.cloneNode(true);
            paragraphParentNode.insertBefore(imageNodeCopy, paragraphNode);
        }

        for (Node nodeToDelete : nodesToDelete) {
            Node parentNode = nodeToDelete.getParentNode();
            parentNode.removeChild(nodeToDelete);
            parentNode.getChildNodes();
        }
    }

    private Node getParagraphNode(Node node) {
        while (!P_TAG.equals(node.getNodeName())) {
            node = node.getParentNode();
        }
        return node;
    }

    private boolean nodeCanBeDeleted(Node node) {
        if (!TAGS_TO_DELETE.contains(node.getNodeName())) {
            return false;
        }
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (!TAGS_TO_DELETE.contains(childNodes.item(i).getNodeName())) {
                return false;
            }
        }
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (!nodeCanBeDeleted(childNodes.item(i))) {
                return false;
            }
        }
        return true;
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
