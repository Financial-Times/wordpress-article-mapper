package com.ft.wordpressarticletransformer.transformer;

import com.ft.bodyprocessing.BodyProcessor;
import com.ft.bodyprocessing.BodyProcessorChain;
import com.ft.bodyprocessing.html.Html5SelfClosingTagBodyProcessor;
import com.ft.bodyprocessing.regex.RegexRemoverBodyProcessor;
import com.ft.bodyprocessing.regex.RegexReplacerBodyProcessor;
import com.ft.bodyprocessing.richcontent.VideoMatcher;
import com.ft.bodyprocessing.transformer.FieldTransformer;
import com.ft.bodyprocessing.transformer.FieldTransformerFactory;
import com.ft.bodyprocessing.xml.StAXTransformingBodyProcessor;
import com.ft.bodyprocessing.xml.TagSoupCleanupHtmlBodyProcessor;
import com.ft.bodyprocessing.xml.TagSoupHtmlBodyProcessor;
import com.ft.wordpressarticletransformer.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticletransformer.transformer.html.RemoveEmptyElementsBodyProcessor;

import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.client.Client;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class BodyProcessingFieldTransformerFactory implements FieldTransformerFactory {

    private final VideoMatcher videoMatcher;
    private final Set<Pattern> shortenerPatterns;
    private final BlogApiEndpointMetadataManager blogApiEndpointMetadataManager;

    private final Client resolverClient;

    private final Client documentStoreClient;
    private final URI documentStoreBaseUri;
    private String documentStoreHostHeader;

    private final Client contentReadClient;
    private final URI contentReadBaseUri;
    private String contentReadHostHeader;

    private final int resolverThreadPoolSize;
    private final int maxLinks;


    public BodyProcessingFieldTransformerFactory(VideoMatcher videoMatcher,
                                                 Set<Pattern> shortenerPatterns,
                                                 BlogApiEndpointMetadataManager blogApiEndpointMetadataManager,
                                                 Client resolverClient,
                                                 int resolverThreadPoolSize,
                                                 int maxLinks,
                                                 Client documentStoreClient,
                                                 URI documentStoreBaseUri,
                                                 String documentStoreHostHeader,
                                                 Client contentReadClient,
                                                 URI contentReadBaseUri,
                                                 String contentReadHostHeader) {
      
        this.videoMatcher = videoMatcher;
        this.shortenerPatterns = ImmutableSet.copyOf(shortenerPatterns);
        this.blogApiEndpointMetadataManager = blogApiEndpointMetadataManager;
        this.resolverClient = resolverClient;
        this.resolverThreadPoolSize = resolverThreadPoolSize;
        this.documentStoreClient = documentStoreClient;
        this.contentReadClient = contentReadClient;
        this.documentStoreBaseUri = documentStoreBaseUri;
        this.contentReadBaseUri = contentReadBaseUri;
        this.contentReadHostHeader = contentReadHostHeader;
        this.documentStoreHostHeader = documentStoreHostHeader;
        this.maxLinks = maxLinks;
    }

    @Override
    public FieldTransformer newInstance() {
        BodyProcessorChain bodyProcessorChain = new BodyProcessorChain(bodyProcessors());
        return new BodyProcessingFieldTransformer(bodyProcessorChain);
    }

    private List<BodyProcessor> bodyProcessors() {
        return asList(
                new RegexRemoverBodyProcessor("(\\s|&nbsp;)*<a\\s[^>]*class=\"more-link\"[^>]*>.*?<\\/a\\s*>"),
                new RegexRemoverBodyProcessor("(<p>)\\s*(</p>)|(<p/>)"),
				new TagSoupHtmlBodyProcessor(),
				new TagSoupCleanupHtmlBodyProcessor(),
                stAXTransformingBodyProcessor(),
                new RemoveEmptyElementsBodyProcessor(asList("p"),asList("img")),
                new Html5SelfClosingTagBodyProcessor(),
				new RegexReplacerBodyProcessor("</p>(\\r?\\n)+<p>", "</p>" + System.lineSeparator() + "<p>"),
                new RegexReplacerBodyProcessor("</p> +<p>", "</p><p>"),
                new LinkResolverBodyProcessor(
                        shortenerPatterns,
                        resolverClient,
                        blogApiEndpointMetadataManager,
                        documentStoreClient,
                        documentStoreBaseUri,
                        documentStoreHostHeader,
                        contentReadClient,
                        contentReadBaseUri,
                        contentReadHostHeader,
                        resolverThreadPoolSize,
                        maxLinks)
        );
    }

    private BodyProcessor stAXTransformingBodyProcessor() {
        return new StAXTransformingBodyProcessor(new StructuredWordPressSourcedBodyXMLEventHandlerRegistry(videoMatcher));
    }

}
