package com.ft.wordpressarticletransformer.transformer;

import static java.util.Arrays.asList;

import java.util.List;

import com.ft.bodyprocessing.BodyProcessor;
import com.ft.bodyprocessing.BodyProcessorChain;
import com.ft.bodyprocessing.html.Html5SelfClosingTagBodyProcessor;
import com.ft.bodyprocessing.regex.RegexRemoverBodyProcessor;
import com.ft.bodyprocessing.regex.RegexReplacerBodyProcessor;
import com.ft.bodyprocessing.transformer.FieldTransformer;
import com.ft.bodyprocessing.transformer.FieldTransformerFactory;
import com.ft.bodyprocessing.xml.StAXTransformingBodyProcessor;
import com.ft.bodyprocessing.xml.TagSoupCleanupHtmlBodyProcessor;
import com.ft.bodyprocessing.xml.TagSoupHtmlBodyProcessor;
import com.ft.wordpressarticletransformer.transformer.html.RemoveEmptyElementsBodyProcessor;

public class BodyProcessingFieldTransformerFactory implements FieldTransformerFactory {

    public BodyProcessingFieldTransformerFactory() {

    }

    @Override
    public FieldTransformer newInstance() {
        BodyProcessorChain bodyProcessorChain = new BodyProcessorChain(bodyProcessors());
        return new BodyProcessingFieldTransformer(bodyProcessorChain);
    }

    private List<BodyProcessor> bodyProcessors() {
        return asList(
                new RegexRemoverBodyProcessor("(<p>)\\s*(</p>)|(<p/>)"),
				new TagSoupHtmlBodyProcessor(),
				new TagSoupCleanupHtmlBodyProcessor(),
                stAXTransformingBodyProcessor(),
                new RemoveEmptyElementsBodyProcessor(asList("p"),asList("img")),
                new Html5SelfClosingTagBodyProcessor(),
				new RegexReplacerBodyProcessor("</p>(\\r?\\n)+<p>", "</p>" + System.lineSeparator() + "<p>"),
				new RegexReplacerBodyProcessor("</p> +<p>", "</p><p>")
        );
    }

    private BodyProcessor stAXTransformingBodyProcessor() {
        return new StAXTransformingBodyProcessor(new StructuredWordPressSourcedBodyXMLEventHandlerRegistry());
    }

}
