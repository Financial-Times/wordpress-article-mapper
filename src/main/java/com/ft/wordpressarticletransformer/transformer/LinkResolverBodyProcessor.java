package com.ft.wordpressarticletransformer.transformer;

import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_TEMPORARILY;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;
import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.Identifier;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.RequestBuilder;
import com.sun.jersey.api.client.UniformInterface;
import com.sun.jersey.api.client.WebResource;


public class LinkResolverBodyProcessor
        implements BodyProcessor {
    
    private static final Logger LOG = LoggerFactory.getLogger(LinkResolverBodyProcessor.class);
    
    private static final Pattern FT_COM = Pattern.compile("https?:\\/\\/[^/]+\\.ft\\.com\\/(.*)");
    private static final Cookie NEXT_COOKIE = new Cookie("FT_SITE", "NEXT", "/", ".ft.com");
    private static final Pattern CONTENT_UUID = Pattern.compile(".*\\/content\\/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})$");
    private static final String ARTICLE_TYPE = "http://www.ft.com/ontology/content/Article";
    
    private final Set<Pattern> urlShortenerPatterns;
    private final Client resolverClient;
    private final Map<Pattern,Brand> urlPatternToBrandMapping;
    private final Client documentStoreClient;
    private final URI documentStoreQueryURI;
    
    public LinkResolverBodyProcessor(Set<Pattern> urlShortenerPatterns, Client resolverClient,
            Map<Pattern,Brand> urlPatternToBrandMapping,
            Client documentStoreClient, URI documentStoreQueryURI) {
        
        this.urlShortenerPatterns = ImmutableSet.copyOf(urlShortenerPatterns);
        
        this.resolverClient = resolverClient;
        this.resolverClient.setFollowRedirects(false);
        
        this.urlPatternToBrandMapping = ImmutableMap.copyOf(urlPatternToBrandMapping);
        
        this.documentStoreClient = documentStoreClient;
        this.documentStoreClient.setFollowRedirects(false);
        
        this.documentStoreQueryURI = documentStoreQueryURI;
    }
    
    @Override
    public String process(String body, BodyProcessingContext bodyProcessingContext)
            throws BodyProcessingException {
        
        if (Strings.isNullOrEmpty(body)) {
            return body;
        }
        
        Document document;
        try {
            DocumentBuilder documentBuilder = getDocumentBuilder();
            document = documentBuilder.parse(new InputSource(new StringReader(body)));
        }
        catch (ParserConfigurationException | SAXException | IOException e) {
            throw new BodyProcessingException(e);
        }
        
        List<Node> shortenedLinks = new ArrayList<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            final NodeList aTags = (NodeList) xpath.evaluate("//a", document, XPathConstants.NODESET);
            for (int i = 0; i < aTags.getLength(); i++) {
                final Element aTag = (Element)aTags.item(i);
                
                if (isShortenedLink(aTag)) {
                    shortenedLinks.add(aTag);
                }
            }
            
            boolean changed = false;
            for (Node n : shortenedLinks) {
                changed |= resolveAndReplaceTag((Element)n);
            }
            
            if (changed) {
                body = serializeBody(document);
            }
        }
        catch (XPathExpressionException e) {
            throw new BodyProcessingException(e);
        }
        
        return body;
    }
    
    private DocumentBuilder getDocumentBuilder()
            throws ParserConfigurationException {
        
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        
        return documentBuilderFactory.newDocumentBuilder();
    }
    
    private boolean isShortenedLink(Element aTag) {
        String url = aTag.getAttribute("href");
        if (Strings.isNullOrEmpty(url)) {
            return false;
        }
        
        for (Pattern p : urlShortenerPatterns) {
            if (p.matcher(url).matches()) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean resolveAndReplaceTag(Element aTag) {
        String url = aTag.getAttribute("href");
        
        Identifier identifier = resolveToFTIdentifier(URI.create(url));
        if (identifier.getAuthority() == null) {
            return false;
        }
        
        UUID uuid = findFTContent(identifier);
        if (uuid == null) {
            return false;
        }
        
        LOG.info("replace link href={} with FT content UUID={}", url, uuid);
        Node parent = aTag.getParentNode();
        Element content = aTag.getOwnerDocument().createElement("content");
        content.setAttribute("id", uuid.toString());
        // TODO should we retrieve the document to get the type and title?
        content.setAttribute("type", ARTICLE_TYPE);
        // content.setAttribute("title", title);
        
        NodeList children = aTag.getChildNodes();
        Node n = children.item(0);
        while (n != null) {
            aTag.removeChild(n);
            content.appendChild(n);
            n = children.item(0);
        }
        
        parent.insertBefore(content, aTag);
        parent.removeChild(aTag);
        
        return true;
    }
    
    private Identifier resolveToFTIdentifier(final URI source) {
        Set<URI> visited = new LinkedHashSet<>();
        URI url = source;
        Identifier identifier = null;
        
        do {
            ClientResponse response = null;
            try {
                if (visited.contains(url)) {
                    LOG.warn("encountered circular redirection for {}: {}, {}", source, visited, url);
                    identifier = new Identifier(null, source.toString());
                    break;
                }
                
                RequestBuilder<WebResource.Builder> resource = resolverClient.resource(url);
                if (FT_COM.matcher(url.toString()).matches()) {
                    resource = resource.cookie(NEXT_COOKIE);
                }
                
                response = ((UniformInterface)resource).head();
                
                visited.add(url);
                
                int status = response.getStatus();
                if ((status == SC_MOVED_PERMANENTLY) || (status == SC_MOVED_TEMPORARILY)) {
                    url = url.resolve(response.getLocation());
                    try {
                        String location = url.toURL().toExternalForm();
                        for (Map.Entry<Pattern,Brand> en : urlPatternToBrandMapping.entrySet()) {
                            if (en.getKey().matcher(location).matches()) {
                                identifier = new Identifier(en.getValue().getId(), FT_COM.matcher(location).replaceAll("http://www.ft.com/$1"));
                                break;
                            }
                        }
                    }
                    catch (MalformedURLException e) {
                        LOG.warn("{} was resolved to {}, which was not a valid URL", source, url);
                        identifier = new Identifier(null, source.toString());
                    }
                }
                else {
                    identifier = new Identifier(null, source.toString());
                    if (status != SC_OK) {
                        LOG.warn("{} was resolved to {}, which returned unexpected status {}", source, url, status);
                    }
                }
            }
            finally {
                if (response != null) {
                    response.close();
                }
            }
            
        } while (identifier == null);
        
        return identifier;
    }
    
    private UUID findFTContent(Identifier identifier) {
        UUID uuid = null;
        try {
            LOG.info("look up content by identifier: {}", identifier);
            URI queryURI = UriBuilder.fromUri(documentStoreQueryURI)
                                     .queryParam("identifierAuthority", identifier.getAuthority())
                                     .queryParam("identifierValue", identifier.getIdentifierValue())
                                     .build();
            
            ClientResponse response = documentStoreClient.resource(queryURI)
                                                         .header("Host", "document-store-api")
                                                         .head();
            
            int status = response.getStatus();
            if ((status == SC_MOVED_PERMANENTLY) || (status == SC_MOVED_TEMPORARILY)) {
                String contentURI = response.getLocation().toString();
                Matcher m = CONTENT_UUID.matcher(contentURI);
                if (m.matches()) {
                    uuid = UUID.fromString(m.group(1));
                }
            }
        }
        catch (ClientHandlerException e) {
            LOG.warn("failed to query document store", e);
        }
        
        return uuid;
    }
    
    private String serializeBody(Document document) {
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            transformer.setOutputProperty("standalone", "yes");
            transformer.transform(domSource, result);
            writer.flush();
            String body = writer.toString();
            return body;
        }
        catch (TransformerException e) {
            throw new BodyProcessingException(e);
        }
    }
}
