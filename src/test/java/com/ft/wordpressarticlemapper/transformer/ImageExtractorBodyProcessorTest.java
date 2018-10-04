package com.ft.wordpressarticlemapper.transformer;

import com.ft.bodyprocessing.BodyProcessingContext;

import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ImageExtractorBodyProcessorTest {

    private BodyProcessingContext bodyProcessingContext;

    private ImageExtractorBodyProcessor imageExtractorBodyProcessor;

    @Before
    public void setUp() {
        imageExtractorBodyProcessor = new ImageExtractorBodyProcessor();
        bodyProcessingContext = new BodyProcessingContext() {
        };
    }

    @Test
    public void testProcess_EmptyBodyDoesNotGetModified() {
        String result = imageExtractorBodyProcessor.process("", bodyProcessingContext);

        assertThat(result, is(""));
    }

    @Test
    public void testProcess_WhitespaceBodyDoesNotGetModified() {
        String result = imageExtractorBodyProcessor.process("   ", bodyProcessingContext);

        assertThat(result, is("   "));
    }

    @Test
    public void testProcess_ExtractImgWhenItIsTheOnlyThingInTheParagraph() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p><img src=\"img source\"/></p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<img src=\"img source\"/><p/>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }


    @Test
    public void testProcess_ExtractImgWhenItIsNotTheOnlyThingInTheParagraph() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img <img src=\"img source\"/> Text after img</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<img src=\"img source\"/><p>Text before img  Text after img</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_ExtractImgInsideATag() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img <a href=\"\">Lorem ipsum<img src=\"img source\"/></a> Text after image</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<img src=\"img source\"/><p>Text before img  Text after image</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_ExtractImgInsideSpanTag() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img <span><a href=\"\"><img src=\"img source\"/></a></span> Text after img</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<img src=\"img source\"/><p>Text before img  Text after img</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_RemoveImgWithoutSrcInsideParagraph() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img <img/> Text after img</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img  Text after img</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_RemoveImgWithEmptySrcInsideParagraph() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img <img src=\"\"/> Text after img</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img Text after img</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_RemoveImgWithoutSrcInsideATag() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img <a href=\"\"><img/></a> Text after img</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img  Text after img</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_RemoveImgWithEmptySrcInsideATag() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img <a href=\"\"><img src=\"\"/></a> Text after img</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img  Text after img</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_RemoveImgWithoutSrcInsideSpanTag() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img <span><a href=\"\"><img/></a></span> Text after img</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img  Text after img</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_RemoveImgWithEmptySrcInsideSpanTag() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img <span><a href=\"\"><img src=\"\"/></a></span> Text after img</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<p>Text before img  Text after img</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_ExtractMultipleImgTagsInsideATagWithATagRemoval() {
        String body = "<body>" +
                "<p>Text before img <a href=\"\"><img src=\"source\"/><img src=\"source\"/></a> Text after img</p>" +
                "</body>";
        String expected = "<body>" +
                "<img src=\"source\"/><img src=\"source\"/>" +
                "<p>Text before img  Text after img</p>" +
                "</body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_ExtractMultipleImgTagsInsideSpanTagWithSpanTagRemoval() {
        String body = "<body>" +
                "<p>Text before img <span><a href=\"\"><img src=\"source\"/><img src=\"source\"/></a></span> Text after img</p>" +
                "</body>";
        String expected = "<body>" +
                "<img src=\"source\"/><img src=\"source\"/>" +
                "<p>Text before img  Text after img</p>" +
                "</body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_ExtractAllImageTypes() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p>" +
                "Text before img <img src=\"img source\"/> Text after img " +
                "Text before a tag <a href=\"\"><img src=\"img source\"/></a> Text after a tag " +
                "Text before span tag <span><a href=\"\"><img src=\"img source\"/></a></span> Text after span tag" +
                "</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<img src=\"img source\"/>" +
                "<img src=\"img source\"/>" +
                "<img src=\"img source\"/>" +
                "<p>Text before img   Text after img Text before a tag  Text after a tag Text before span tag  Text after span tag</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }
}
