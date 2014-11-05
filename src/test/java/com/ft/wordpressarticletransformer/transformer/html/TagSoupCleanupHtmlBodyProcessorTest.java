package com.ft.wordpressarticletransformer.transformer.html;

import org.junit.Test;

import static com.ft.wordpressarticletransformer.transformer.html.TagSoupHtmlBodyProcessorTest.simpleContext;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.XmlMatchers.isEquivalentTo;
import static org.xmlmatchers.transform.XmlConverters.the;
import static org.xmlmatchers.transform.XmlConverters.xml;

public class TagSoupCleanupHtmlBodyProcessorTest {

    private TagSoupCleanupHtmlBodyProcessor transformerUnderTest = new TagSoupCleanupHtmlBodyProcessor();

    @Test
    public void shapeRectIsRemovedFromAnchorTags() {
        String input = "<a shape='rect'>text</a>";
        String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<a>text</a>")));
    }

    @Test
    public void anchorTagsWithoutShapeAttrAreUnchanged() {
        String input = "<a>text</a>";
        String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(the(input)));
    }

    @Test
    public void anchorTagsWithShapeCircleAreUnchanged() {
        String input = "<a shape='circle'>text</a>";
        String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(the(input)));
    }

    @Test
    public void anchorTagsWithOtherAttributesAreUnchanged() {
        String input = "<a href='http://my url'>text</a>";
        String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(the(input)));
    }
    
    @Test
    public void spanOneIsRemovedFromColTags() {
    	String input = "<col span=\"1\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<col/>")));
    }
    
    @Test
    public void colTagsWithoutSpanAreUnchanged() {
    	String input = "<col/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<col/>")));
    }
    
    @Test
    public void colTagsWithSpanMoreThanOneAreUnchanged() {
    	String input = "<col span=\"2\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<col span=\"2\"/>")));
    }
    
    @Test
    public void spanOneIsRemovedFromColgroupTags() {
    	String input = "<colgroup span=\"1\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<colgroup/>")));
    }
    
    @Test
    public void colgroupTagsWithoutSpanAreUnchanged() {
    	String input = "<colgroup/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<colgroup/>")));
    }
    
    @Test
    public void colgroupTagsWithSpanMoreThanOneAreUnchanged() {
    	String input = "<colgroup span=\"2\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<colgroup span=\"2\"/>")));
    }
    
    @Test
    public void colspanOneIsRemovedFromTdTags() {
    	String input = "<td colspan=\"1\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<td/>")));
    }
    
    @Test
    public void rowspanOneIsRemovedFromTdTags() {
    	String input = "<td rowspan=\"1\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<td/>")));
    }
    
    @Test
    public void tdTagsWithoutColspanOrRowspanAreUnchanged() {
    	String input = "<td/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<td/>")));
    }
    
    @Test
    public void tdTagsWithColspanMoreThanOneAreUnchanged() {
    	String input = "<td colspan=\"2\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<td colspan=\"2\"/>")));
    }
    
    @Test
    public void tdTagsWithRowspanMoreThanOneAreUnchanged() {
    	String input = "<td rowspan=\"2\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<td rowspan=\"2\"/>")));
    }
    
    @Test
    public void colspanOneIsRemovedFromThTags() {
    	String input = "<th colspan=\"1\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<th/>")));
    }
    
    @Test
    public void rowspanOneIsRemovedFromThTags() {
    	String input = "<th rowspan=\"1\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<th/>")));
    }
    
    @Test
    public void thTagsWithoutColspanOrRowspanAreUnchanged() {
    	String input = "<th/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<th/>")));
    }
    
    @Test
    public void thTagsWithColspanMoreThanOneAreUnchanged() {
    	String input = "<th colspan=\"2\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<th colspan=\"2\"/>")));
    }
    
    @Test
    public void thTagsWithRowspanMoreThanOneAreUnchanged() {
    	String input = "<th rowspan=\"2\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<th rowspan=\"2\"/>")));
    }
    
    @Test
    public void clearIsRemovedFromBrTag() {
    	String input = "<br clear=\"none\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<br/>")));
    }
    
    @Test
    public void valuetypeIsRemovedFromParamTag() {
    	String input = "<param valuetype=\"data\"/>";
    	String output = transformerUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<param/>")));
    }

    @Test
    public void xmlPrologIsNotOutput() {
        String input = "<p>text</p>";
        String output = transformerUnderTest.process(input, simpleContext());
        assertThat(output, not(startsWith("<?xml")));
    }

}
