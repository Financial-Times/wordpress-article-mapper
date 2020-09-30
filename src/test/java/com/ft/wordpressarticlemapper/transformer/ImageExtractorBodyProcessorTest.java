package com.ft.wordpressarticlemapper.transformer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.ft.bodyprocessing.BodyProcessingContext;
import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.junit.Before;
import org.junit.Test;

public class ImageExtractorBodyProcessorTest {

  private BodyProcessingContext bodyProcessingContext;

  private ImageExtractorBodyProcessor imageExtractorBodyProcessor;

  @Before
  public void setUp() {
    imageExtractorBodyProcessor = new ImageExtractorBodyProcessor();
    bodyProcessingContext = new BodyProcessingContext() {};
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
  public void testProcess_DeleteImageWithEmptySrcFromParagraph() {
    String body =
        "<body><p>Lorem ipsum</p>"
            + "<p><img src=\"\"/></p>"
            + "<p><img src=\"\"/><img src=\"source\"/></p>"
            + "<p>Before img<img src=\"\"/></p>"
            + "<p><img src=\"\"/>After img</p>"
            + "<p><img src=\"\"/><non-deletable/></p>"
            + "<p><non-deletable/><img src=\"\"/></p>"
            + "<p><a href=\"\"><img src=\"\"/></a></p>"
            + "<p>Lorem ipsum</p></body>";

    String expected =
        "<body><p>Lorem ipsum</p>"
            + "<p/>"
            + "<img src=\"source\"/><p/>"
            + "<p>Before img</p>"
            + "<p>After img</p>"
            + "<p><non-deletable/></p>"
            + "<p><non-deletable/></p>"
            + "<p/>"
            + "<p>Lorem ipsum</p></body>";

    String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

    assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
  }

  @Test
  public void testProcess_DeleteImageWithMissingSrcFromParagraph() {
    String body =
        "<body><p>Lorem ipsum</p>"
            + "<p><img/></p>"
            + "<p><img/><img src=\"source\"/></p>"
            + "<p>Before img<img src=\"\"/></p>"
            + "<p><img/>After img</p>"
            + "<p><img/><non-deletable/></p>"
            + "<p><non-deletable/><img/></p>"
            + "<p>Lorem ipsum</p></body>";

    String expected =
        "<body><p>Lorem ipsum</p>"
            + "<p/>"
            + "<img src=\"source\"/><p/>"
            + "<p>Before img</p>"
            + "<p>After img</p>"
            + "<p><non-deletable/></p>"
            + "<p><non-deletable/></p>"
            + "<p>Lorem ipsum</p></body>";

    String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

    assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
  }

  @Test
  public void testProcess_ExtractImageFromParagraph() {
    String body =
        "<body><p>Lorem ipsum</p>"
            + "<p><img src=\"source\"/></p>"
            + "<p><img src=\"source\"/><img src=\"source\"/></p>"
            + "<p>Before img<img src=\"source\"/></p>"
            + "<p><img src=\"source\"/>After img</p>"
            + "<p><img src=\"source\"/><non-deletable/></p>"
            + "<p><non-deletable/><img src=\"source\"/></p>"
            + "<p>Lorem ipsum</p></body>";

    String expected =
        "<body><p>Lorem ipsum</p>"
            + "<img src=\"source\"/><p/>"
            + "<img src=\"source\"/><img src=\"source\"/><p/>"
            + "<img src=\"source\"/><p>Before img</p>"
            + "<img src=\"source\"/><p>After img</p>"
            + "<img src=\"source\"/><p><non-deletable/></p>"
            + "<img src=\"source\"/><p><non-deletable/></p>"
            + "<p>Lorem ipsum</p></body>";

    String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

    assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
  }

  @Test
  public void testProcess_ExtractImageFromNonDeletableTag() {
    String body =
        "<body><p>Lorem ipsum</p>"
            + "<p><em><span><a href=\"\"><img src=\"source\"/></a></span></em></p>"
            + "<p><em><span><a href=\"\"><img src=\"source\"/><img src=\"source\"/></a></span></em></p>"
            + "<p><em><span><a href=\"\"><img src=\"source\"/></a></span><span><a href=\"\"><img src=\"source\"/></a></span></em></p>"
            + "<p><em>Before span<span><a href=\"\"><img src=\"source\"/></a></span> After span</em></p>"
            + "<p><em><span>Before a tag<a href=\"\"><img src=\"source\"/></a> After a tag</span></em></p>"
            + "<p><em><span><a href=\"\">Before img<img src=\"source\"/> After img</a> After a tag</span></em></p>"
            + "<p>Lorem ipsum</p></body>";

    String expected =
        "<body><p>Lorem ipsum</p>"
            + "<img src=\"source\"/><p><em/></p>"
            + "<img src=\"source\"/><img src=\"source\"/><p><em/></p>"
            + "<img src=\"source\"/><img src=\"source\"/><p><em/></p>"
            + "<img src=\"source\"/><p><em>Before span After span</em></p>"
            + "<img src=\"source\"/><p><em><span>Before a tag After a tag</span></em></p>"
            + "<img src=\"source\"/><p><em><span><a href=\"\">Before img After img</a> After a tag</span></em></p>"
            + "<p>Lorem ipsum</p></body>";

    String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

    assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
  }

  @Test
  public void testProcess_ExtractImageFromSpan() {
    String body =
        "<body><p>Lorem ipsum</p>"
            + "<p><span><a href=\"\"><img src=\"source\"/></a></span></p>"
            + "<p><span><a href=\"\"><img src=\"source\"/><img src=\"source\"/></a></span></p>"
            + "<p><span><a href=\"\"><img src=\"source\"/></a><a href=\"\"><img src=\"source\"/><non-deletable/></a></span></p>"
            + "<p><span><a href=\"\"><img src=\"source\"/></a><a href=\"\"><img src=\"source\"/>After img</a></span></p>"
            + "<p><span>Before a tag<a href=\"\"><img src=\"source\"/></a></span></p>"
            + "<p><span><a href=\"\"><img src=\"source\"/></a>After a tag</span></p>"
            + "<p><span><a href=\"\">Before img<img src=\"source\"/></a></span></p>"
            + "<p><span><a href=\"\"><img src=\"source\"/>After img</a></span></p>"
            + "<p>Lorem ipsum</p></body>";

    String expected =
        "<body><p>Lorem ipsum</p>"
            + "<img src=\"source\"/><p/>"
            + "<img src=\"source\"/><img src=\"source\"/><p/>"
            + "<img src=\"source\"/><img src=\"source\"/><p><span><a href=\"\"><non-deletable/></a></span></p>"
            + "<img src=\"source\"/><img src=\"source\"/><p><span><a href=\"\">After img</a></span></p>"
            + "<img src=\"source\"/><p><span>Before a tag</span></p>"
            + "<img src=\"source\"/><p><span>After a tag</span></p>"
            + "<img src=\"source\"/><p><span><a href=\"\">Before img</a></span></p>"
            + "<img src=\"source\"/><p><span><a href=\"\">After img</a></span></p>"
            + "<p>Lorem ipsum</p></body>";

    String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

    assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
  }

  @Test
  public void testProcess_ExtractImageFromAnchor() {
    String body =
        "<body><p>Lorem ipsum</p>"
            + "<p><a href=\"\"><img src=\"source\"/></a></p>"
            + "<p><a href=\"\"><img src=\"source\"/><img src=\"source\"/></a></p>"
            + "<p><a href=\"\"><img src=\"source\"/>After img</a></p>"
            + "<p><a href=\"\">Before img<img src=\"source\"/></a></p>"
            + "<p><a href=\"\"><img src=\"source\"/><non-deletable/></a></p>"
            + "<p><a href=\"\"><non-deletable/><img src=\"source\"/></a></p>"
            + "<p><a href=\"\"><img src=\"source\"/></a><a href=\"\"><img src=\"source\"/><non-deletable/></a></p>"
            + "<p><a href=\"\"><img src=\"source\"/></a><a href=\"\"><img src=\"source\"/>After img</a></p>"
            + "<p>Lorem ipsum</p></body>";

    String expected =
        "<body><p>Lorem ipsum</p>"
            + "<img src=\"source\"/><p/>"
            + "<img src=\"source\"/><img src=\"source\"/><p/>"
            + "<img src=\"source\"/><p><a href=\"\">After img</a></p>"
            + "<img src=\"source\"/><p><a href=\"\">Before img</a></p>"
            + "<img src=\"source\"/><p><a href=\"\"><non-deletable/></a></p>"
            + "<img src=\"source\"/><p><a href=\"\"><non-deletable/></a></p>"
            + "<img src=\"source\"/><img src=\"source\"/><p><a href=\"\"><non-deletable/></a></p>"
            + "<img src=\"source\"/><img src=\"source\"/><p><a href=\"\">After img</a></p>"
            + "<p>Lorem ipsum</p></body>";

    String result = imageExtractorBodyProcessor.process(body, bodyProcessingContext);

    assertThat(result, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expected));
  }
}
