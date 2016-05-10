package com.ft.wordpressarticletransformer.transformer;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;
import com.ft.wordpressarticletransformer.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticletransformer.model.Identifier;
import com.ft.wordpressarticletransformer.resources.IdentifierBuilder;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.RequestBuilder;
import com.sun.jersey.api.client.UniformInterface;
import com.sun.jersey.api.client.WebResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_TEMPORARILY;
import static javax.servlet.http.HttpServletResponse.SC_OK;


public class LinkResolverBodyProcessor
        implements BodyProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(LinkResolverBodyProcessor.class);

    private static final Pattern FT_COM = Pattern.compile("https?:\\/\\/[^/]+\\.ft\\.com\\/(.*)");
    private static final Cookie NEXT_COOKIE = new Cookie("FT_SITE", "NEXT", "/", ".ft.com");
    private static final String UUID_REGEX = "([0-9a-f]{8}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{12})";
    private static final Pattern CONTENT_UUID = Pattern.compile(".*\\/content\\/" + UUID_REGEX + "$");
    private static final Pattern HREF_UUID = Pattern.compile(".*ft\\.com\\/\\S*" + UUID_REGEX + ".*");
    private static final Pattern FT_WORDPRESS_URL = Pattern.compile("https?:\\/\\/([^.]+\\.)?[^.]+\\.ft\\.com\\/(\\S*\\/)?\\d{4}\\/\\d{2}\\/\\d{2}\\/.*\\/");
    private static final String ARTICLE_TYPE = "http://www.ft.com/ontology/content/Article";

    private final Set<Pattern> urlShortenerPatterns;
    private final IdentifierBuilder identifierBuilder;
    private final Client resolverClient;
    private final Client documentStoreClient;
    private final UriBuilder documentStoreContentUriBuilder;
    private final URI documentStoreQueryURI;
    private final int poolSize;
    private final int maxLinks;

    public LinkResolverBodyProcessor(Set<Pattern> urlShortenerPatterns, Client resolverClient,
                                     BlogApiEndpointMetadataManager blogApiEndpointMetadataManager,
                                     Client documentStoreClient, URI documentStoreBaseURI, int queryThreadPoolSize, int maxLinks) {

        this.urlShortenerPatterns = ImmutableSet.copyOf(urlShortenerPatterns);

        this.resolverClient = resolverClient;
        this.resolverClient.setFollowRedirects(false);

        this.identifierBuilder = new IdentifierBuilder(blogApiEndpointMetadataManager);

        this.documentStoreClient = documentStoreClient;
        this.documentStoreClient.setFollowRedirects(false);

        this.documentStoreContentUriBuilder = UriBuilder.fromUri(documentStoreBaseURI).path("/content/{uuid}");
        this.documentStoreQueryURI = UriBuilder.fromUri(documentStoreBaseURI).path("/content-query").build();
        this.poolSize = queryThreadPoolSize;
        this.maxLinks = maxLinks;
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
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new BodyProcessingException(e);
        }

        List<Element> ftContentLinks = new ArrayList<>();
        List<Element> ftWordpressLinks = new ArrayList<>();

        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            final NodeList aTags = (NodeList) xpath.evaluate("//a", document, XPathConstants.NODESET);
            for (int i = 0; i < aTags.getLength(); i++) {
                final Element aTag = (Element) aTags.item(i);

                if (isFTContentLink(aTag)) {
                    ftContentLinks.add(aTag);
                } else if (isShortenedLink(aTag) || isFtWordpressLink(aTag)) {
                    ftWordpressLinks.add(aTag);
                }
            }

            boolean anyLinkChanged = false;
            for (Element aTag : ftContentLinks) {
                UUID uuid = extractUUID(aTag);
                if (uuid != null) {
                    replaceTag(aTag, uuid);
                    anyLinkChanged = true;
                }
            }

            int linksCount = ftWordpressLinks.size();
            if (linksCount > maxLinks) {
                LOG.warn("Article contains too many links to resolve. Omitting {}",
                        ftWordpressLinks.subList(maxLinks, linksCount).stream()
                                .map(el -> el.getAttribute("href"))
                                .collect(Collectors.toList()));

            }

            ForkJoinPool pool = new ForkJoinPool(poolSize);
            ForkJoinTask<Boolean> task = pool.submit(() -> {
                Optional<Boolean> shortLinkChanged = ftWordpressLinks.subList(0, Math.min(linksCount, maxLinks))
                        .parallelStream()
                        .map(link -> {
                            try {
                                return resolveAndReplaceTag(link);
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .reduce((t, u) -> t | u);

                return shortLinkChanged.orElse(false);
            });

            anyLinkChanged |= task.join();

            if (anyLinkChanged) {
                body = serializeBody(document);
            }
        } catch (XPathExpressionException e) {
            throw new BodyProcessingException(e);
        }

        return body;
    }

    private boolean isFtWordpressLink(Element aTag) {
        String url = aTag.getAttribute("href");
        if (Strings.isNullOrEmpty(url)) {
            return false;
        }
        return FT_WORDPRESS_URL.matcher(url).matches();
    }

    private DocumentBuilder getDocumentBuilder()
            throws ParserConfigurationException {

        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        return documentBuilderFactory.newDocumentBuilder();
    }

    private boolean isFTContentLink(Element aTag) {
        String url = aTag.getAttribute("href");
        if (Strings.isNullOrEmpty(url)) {
            return false;
        }

        return HREF_UUID.matcher(url).matches();
    }

    private UUID extractUUID(Element aTag) {
        String url = aTag.getAttribute("href");
        UUID uuid = null;

        Matcher m = HREF_UUID.matcher(url);
        if (m.matches()) {
            uuid = UUID.fromString(m.group(1));
        }

        if (!validateFTContentExists(uuid)) {
            uuid = null;
        }

        return uuid;
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

        replaceTag(aTag, uuid);
        return true;
    }

    private void replaceTag(Element aTag, UUID uuid) {
        LOG.info("replace link href={} with FT content UUID={}", aTag.getAttribute("href"), uuid);
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
    }

    private Identifier resolveToFTIdentifier(final URI source) {
        Identifier identifier = null;

        if (FT_WORDPRESS_URL.matcher(source.toASCIIString()).matches()) {
            identifier = identifierBuilder.build(source);
            if (identifier != null) {
                return identifier;
            }
        }
        Set<URI> visited = new LinkedHashSet<>();
        URI url = source;


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

                response = ((UniformInterface) resource).head();

                visited.add(url);

                int status = response.getStatus();
                if ((status == SC_MOVED_PERMANENTLY) || (status == SC_MOVED_TEMPORARILY)) {
                    url = url.resolve(response.getLocation());
                    try {
                        URI location = url.toURL().toURI();
                        identifier = identifierBuilder.build(location);

                    } catch (MalformedURLException | URISyntaxException e) {
                        LOG.warn("{} was resolved to {}, which was not a valid URL", source, url);
                        identifier = identifierBuilder.build(source.toString());
                    }
                } else {
                    identifier = identifierBuilder.build(source.toString());
                    if (status != SC_OK) {
                        LOG.warn("{} was resolved to {}, which returned unexpected status {}", source, url, status);
                    }
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }

        } while (identifier == null);

        return identifier;
    }

    private boolean validateFTContentExists(UUID uuid) {
        try {
            LOG.info("look up content by UUID: {}", uuid);
            URI queryURI = documentStoreContentUriBuilder.build(uuid);

            LOG.info("query URI: {}", queryURI);
            ClientResponse response = documentStoreClient.resource(queryURI)
                    .header("Host", "document-store-api")
                    .head();
            int status = response.getStatus();
            LOG.info("query response: {}", status);
            return (status == SC_OK);
        } catch (ClientHandlerException e) {
            LOG.warn("failed to query document store", e);
        }

        return false;
    }

    private UUID findFTContent(Identifier identifier) {
        UUID uuid = null;
        try {
            LOG.info("look up content by identifier: {}", identifier);

            URI queryURI = UriBuilder.fromUri(documentStoreQueryURI)
                    .queryParam("identifierAuthority", URLEncoder.encode(identifier.getAuthority(), "UTF-8"))
                    .queryParam("identifierValue", URLEncoder.encode(identifier.getIdentifierValue(), "UTF-8"))
                    .build();

            LOG.info("query URI: {}", queryURI);
            ClientResponse response = documentStoreClient.resource(queryURI)
                    .header("Host", "document-store-api")
                    .head();

            int status = response.getStatus();
            LOG.info("query response: {}", status);
            if ((status == SC_MOVED_PERMANENTLY) || (status == SC_MOVED_TEMPORARILY)) {
                String contentURI = response.getLocation().toString();
                LOG.info("query response redirected to: {}", contentURI);
                Matcher m = CONTENT_UUID.matcher(contentURI);
                if (m.matches()) {
                    uuid = UUID.fromString(m.group(1));
                }
            }
        } catch (ClientHandlerException | UnsupportedEncodingException e) {
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
        } catch (TransformerException e) {
            throw new BodyProcessingException(e);
        }
    }
}
