package com.ft.wordpressarticletransformer.transformer.html;

import com.ft.wordpressarticletransformer.transformer.DefaultTransactionIdBodyProcessingContext;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertThat;

/**
 * FloatingTextWrappingBodyProcessorTest
 *
 * @author Simon.Gibbs
 */
public class FloatingTextWrappingBodyProcessorTest {
    public static final DefaultTransactionIdBodyProcessingContext DUMMY_CONTEXT = new DefaultTransactionIdBodyProcessingContext("test");
    public static final String FULLY_WRAPPED_RESULT = "<p>This is a tweet:</p><blockquote>Some tweet (not to scale)</blockquote><p>Wasn't that illuminating!</p>";
    private FloatingTextWrappingBodyProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new FloatingTextWrappingBodyProcessor();
    }

    @Test
    public void shouldWrapASingleUnWrappedParagraph() throws Exception {
        String example = bodyOf("<p>This is a tweet:</p><blockquote>Some tweet (not to scale)</blockquote>Wasn't that illuminating!");

        String result = processor.process(example, DUMMY_CONTEXT);

        assertThat(result, is(bodyOf(FULLY_WRAPPED_RESULT)));

    }


    @Test
    public void shouldWrapSeparateUnWrappedParagraphsSeparately() throws Exception {
        String example = bodyOf("This is a tweet:<blockquote>Some tweet (not to scale)</blockquote>Wasn't that illuminating!");

        String result = processor.process(example, DUMMY_CONTEXT);

        assertThat(result, is(bodyOf(FULLY_WRAPPED_RESULT)));

    }

    @Test
    public void shouldWrapAdjacentUnWrappedParagraphsSeparately() throws Exception {
        String example = bodyOf("This is a tweet:\n\nSome tweet (not to scale)\n\nWasn't that illuminating!");

        String result = processor.process(example, DUMMY_CONTEXT);

        assertThat(result, equalToIgnoringWhiteSpace(bodyOf("<p>This is a tweet:</p><p>Some tweet (not to scale)</p><p>Wasn't that illuminating!</p>")));
    }

    private String bodyOf(String innerMarkup) {
        return String.format("<body>%s</body>",innerMarkup);
    }
}
