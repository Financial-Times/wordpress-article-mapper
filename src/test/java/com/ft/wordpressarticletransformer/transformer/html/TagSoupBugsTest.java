package com.ft.wordpressarticletransformer.transformer.html;

import com.ft.bodyprocessing.BodyProcessingContext;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class TagSoupBugsTest {

    private BodyProcessingContext bodyProcessingContext;

    private TagSoupHtmlBodyProcessor tagSoupHtmlBodyProcessor;

    @Test @Ignore
    public void wanderingATagTest() {
        String body = "<div data-asset-type=\"embed\"><blockquote class=\"twitter-tweet\" lang=\"en\"><p>Learning from Comcast/TWC? AT&amp;T b DirecTV deal includes collar protecting <a href=\"https://twitter.com/search?q=%24DTV&amp;src=ctag\">$DTV</a> shareholders from decline in <a href=\"https://twitter.com/search?q=%24T&amp;src=ctag\">$T<\\/a> stock. (Caps upside, too).</p>&mdash; Liz Hoffman (@lizrhoffman) <a href=\"https://twitter.com/lizrhoffman/statuses/468146880682016769\">May 18, 2014</a></blockquote><script src=\"//platform.twitter.com/widgets.js\" charset=\"utf-8\"></script></div>";
        tagSoupHtmlBodyProcessor = new TagSoupHtmlBodyProcessor();
        String result = tagSoupHtmlBodyProcessor.process(body, new BodyProcessingContext() {});
        assertThat("does not contain", result, containsString("\">$T<\\/a>"));
        //TODO Fix bug where closing <a> tag is closed in wrong position, thus changing the value of the output
    }
}
