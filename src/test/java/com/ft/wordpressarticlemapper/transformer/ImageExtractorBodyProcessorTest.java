package com.ft.wordpressarticlemapper.transformer;

import com.ft.bodyprocessing.BodyProcessingContext;
import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.junit.Before;
import org.junit.Test;

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
    public void testProcess_ExtractImg() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p><img src=\"img source\"/>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<img src=\"img source\"/><p>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_ExtractImgInsideATag() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p><a href=\"\"><img src=\"img source\"/></a>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<img src=\"img source\"/><p>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_RemoveImgWithoutSrcInsideParagraph() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p><img/>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_RemoveImgWithEmptySrcInsideParagraph() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p><img src=\"\"/>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_RemoveImgWithoutSrcOutsideParagraph() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<img/>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_RemoveImgWithEmptySrcOutsideParagraph() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<img src=\"\"/>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_RemoveImgWithoutSrcInsideATag() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p><a href=\"\"><img/></a>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_RemoveImgWithEmptySrcInsideATag() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p><a href=\"\"><img src=\"\"/></a>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void testProcess_ExtractAllImageTypes() {
        String body = "<body><p>Lorem ipsum</p>" +
                "<p>" +
                "<img src=\"img source\"/>" +
                "<a href=\"\"><img src=\"img source\"/></a>" +
                "Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";
        String expected = "<body><p>Lorem ipsum</p>" +
                "<img src=\"img source\"/>" +
                "<img src=\"img source\"/>" +
                "<p>Lorem ipsum</p>" +
                "<p>Lorem ipsum</p></body>";

        String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
    }
}
