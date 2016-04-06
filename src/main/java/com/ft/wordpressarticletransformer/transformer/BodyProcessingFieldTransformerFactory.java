package com.ft.wordpressarticletransformer.transformer;

import static java.util.Arrays.asList;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.transformer.html.RemoveEmptyElementsBodyProcessor;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.client.Client;

public class BodyProcessingFieldTransformerFactory implements FieldTransformerFactory {

    private final VideoMatcher videoMatcher;
    private final Set<Pattern> shortenerPatterns;
    private final Map<Pattern,Brand> brandMappings;
    private final Client resolverClient;
    private final Client documentStoreClient;
    private final URI documentStoreBaseUri;
    private final int resolverThreadPoolSize;
    private final int maxLinks;
    
    public BodyProcessingFieldTransformerFactory(VideoMatcher videoMatcher,
                                                 Set<Pattern> shortenerPatterns,
                                                 Map<Pattern,Brand> brandMappings,
                                                 Client resolverClient, int resolverThreadPoolSize, int maxLinks,
                                                 Client documentStoreClient, URI documentStoreBaseUri) {
      
        this.videoMatcher = videoMatcher;
        this.shortenerPatterns = ImmutableSet.copyOf(shortenerPatterns);
        this.brandMappings = ImmutableMap.copyOf(brandMappings);
        this.resolverClient = resolverClient;
        this.resolverThreadPoolSize = resolverThreadPoolSize;
        this.documentStoreClient = documentStoreClient;
        this.documentStoreBaseUri = documentStoreBaseUri;
        this.maxLinks = maxLinks;
    }

    @Override
    public FieldTransformer newInstance() {
        BodyProcessorChain bodyProcessorChain = new BodyProcessorChain(bodyProcessors());
        return new BodyProcessingFieldTransformer(bodyProcessorChain);
    }

    private List<BodyProcessor> bodyProcessors() {
        return asList(
                new RegexRemoverBodyProcessor("(\\s|&nbsp;)*<a\\s[^>]*class=\"more-link\"[^>]*>Read more<\\/a\\s*>"),
                new RegexRemoverBodyProcessor("(<p>)\\s*(</p>)|(<p/>)"),
				new TagSoupHtmlBodyProcessor(),
				new TagSoupCleanupHtmlBodyProcessor(),
                stAXTransformingBodyProcessor(),
                new RemoveEmptyElementsBodyProcessor(asList("p"),asList("img")),
                new Html5SelfClosingTagBodyProcessor(),
				new RegexReplacerBodyProcessor("</p>(\\r?\\n)+<p>", "</p>" + System.lineSeparator() + "<p>"),
				new RegexReplacerBodyProcessor("</p> +<p>", "</p><p>")/*,
                new LinkResolverBodyProcessor(shortenerPatterns, resolverClient,
                        brandMappings,
                        documentStoreClient, documentStoreBaseUri, resolverThreadPoolSize, maxLinks)*/
        );
    }

    private BodyProcessor stAXTransformingBodyProcessor() {
        return new StAXTransformingBodyProcessor(new StructuredWordPressSourcedBodyXMLEventHandlerRegistry(videoMatcher));
    }

}
