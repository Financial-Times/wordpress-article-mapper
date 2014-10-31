package com.ft.fastfttransformer.transformer;

import static java.util.Arrays.asList;

import java.util.List;

import com.ft.bodyprocessing.BodyProcessor;
import com.ft.bodyprocessing.BodyProcessorChain;
import com.ft.bodyprocessing.regex.RegexRemoverBodyProcessor;
import com.ft.bodyprocessing.regex.RegexReplacerBodyProcessor;
import com.ft.bodyprocessing.transformer.FieldTransformer;
import com.ft.bodyprocessing.transformer.FieldTransformerFactory;
import com.ft.bodyprocessing.xml.StAXTransformingBodyProcessor;

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
                stAXTransformingBodyProcessor(),
                new RegexRemoverBodyProcessor("(<p>)\\s*(</p>)"),
                new RegexReplacerBodyProcessor("(?:</p>)\\s*(?:<p>)", "</p><p>")
        );
    }

    private BodyProcessor stAXTransformingBodyProcessor() {
        return new StAXTransformingBodyProcessor(new BodyTransformationXMLEventRegistry());
    }

}
