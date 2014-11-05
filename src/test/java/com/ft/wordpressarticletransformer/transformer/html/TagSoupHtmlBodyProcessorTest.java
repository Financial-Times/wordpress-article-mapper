package com.ft.wordpressarticletransformer.transformer.html;

import com.ft.wordpressarticletransformer.transformer.DefaultTransactionIdBodyProcessingContext;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.XmlMatchers.isEquivalentTo;
import static org.xmlmatchers.transform.XmlConverters.the;
import static org.xmlmatchers.transform.XmlConverters.xml;

public class TagSoupHtmlBodyProcessorTest {
    private TagSoupHtmlBodyProcessor tagSoupProcessorUnderTest = new TagSoupHtmlBodyProcessor();

    @Test
    public void commentIsUnchanged() throws Exception {
        String input = "<!-- some comment -->Some text";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<body>" + input + "</body>")));
    }

    public static DefaultTransactionIdBodyProcessingContext simpleContext() {
        return new DefaultTransactionIdBodyProcessingContext(TagSoupHtmlBodyProcessorTest.class.getSimpleName());
    }

    @Test
    public void wronglyNestedTagsAreFixed() throws Exception {
        String input = "<p>Asdf <em>blah</p> asdfd</em>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), hasXPath("/body/p/em", equalTo("blah")));
    }
    
    // these are tags that are not required to be inside another tag, and are not self-closing
    @Test
    public void simpleTagsAreUnchanged() throws Exception {
    	List<String> tagsToTest = Lists.newArrayList( 
    			"abbr", "acronym", "address", "applet", 
    			"article", "aside", "audio", "b", 
    			"bdi", "bdo", "big", 
    			"blockquote", "canvas", 
    			"center", "cite", "code", 
    			"command", "datalist", "del", 
    			"details", "dfn", "dialog", "div", 
    			"em", "embed", "fieldset", 
    			"figcaption", "figure", "font", "footer", 
    			"header", "hgroup", "h1", "h2", "h3", 
    			"h4", "h5", "h6", "i", 
    			"ins", "kbd", 
    			"keygen", "label", 
    			"mark", "meter", 
    			"nav", "noscript", "object", 
    			"output", "p", 
    			"pre", "progress", "q", "rp", 
    			"ruby", "s", "samp", "script", 
    			"section", "small", "source", 
    			"span", "strike", "strong", "style", "sub", 
    			"summary", "sup", 
    			"textarea", "time", 
    			"track", "tt", "u", 
    			"var", "video");
		for (String tag: tagsToTest) {
			String text = "Text <" + tag + ">content</" + tag + ">More text";
			checkTransformation(text, "<body>" + text + "</body>");
		}
    }
    
    @Test
    public void simpleSelfClosingTagsAreUnchanged() throws Exception {
    	List<String> tagsToTest = Lists.newArrayList( 
    			"basefont", "hr", "img", "wbr");
		for (String tag: tagsToTest) {
			String text = "Text <" + tag + "> content";
			checkTransformation(text, "<body>Text <" + tag + "/> content</body>");
		}
    }   
   
    @Test
    public void nonClosedSelfClosingTagsAreFixed() throws Exception {
    	String input = "<p>Some <img>text</p>";
    	String expectedOutput = "<body><p>Some <img/>text</p></body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    // TABLE RELATED TAGS
    @Test
    public void captionIsWrappedInTableTag() throws Exception {
    	String input = "<caption>content</caption>";
    	String expectedOutput = "<body><table><caption>content</caption></table></body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    @Test
    public void tbodyIsWrappedInTableTagAndContentMovedOut() throws Exception {
    	String input = "<tbody>content</tbody>";
    	String expectedOutput = "<body><table><tbody/></table>content</body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    @Test
    public void tfootIsWrappedInTableTagAndContentMovedOut() throws Exception {
    	String input = "<tfoot>content</tfoot>";
    	String expectedOutput = "<body><table><tfoot/></table>content</body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    @Test
    public void theadIsWrappedInTableTagAndContentMovedOut() throws Exception {
    	String input = "<thead>content</thead>";
    	String expectedOutput = "<body><table><thead/></table>content</body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    @Test
    public void trIsWrappedInTbodyAndTableTagAndContentMovedOut() throws Exception {
    	String input = "<tr>content</tr>";
    	String expectedOutput = "<body><table><tbody><tr/></tbody></table>content</body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    //NB: tag soup inserts span, we remove that with the StAXTagSoupCleanupBlogBodyTransformer
    @Test
    public void colIsWrappedInTableTag() throws Exception {
    	String input = "<col>";
    	String expectedOutput = "<body><table><col span=\"1\"/></table></body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    //NB: tag soup inserts span, we remove that with the StAXTagSoupCleanupBlogBodyTransformer
    @Test
    public void colGroupIsWrappedInTableTag() throws Exception {
    	String input = "<colgroup>content";
    	String expectedOutput = "<body><table><colgroup span=\"1\"/></table>content</body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    //NB: tag soup inserts colspan and rowspan, we remove that with the StAXTagSoupCleanupBlogBodyTransformer
    @Test
    public void tdIsWrappedInTableTag() throws Exception {
    	String input = "<td>content</td>";
    	String expectedOutput = "<body><table><tbody><tr><td colspan=\"1\" rowspan=\"1\">content</td></tr></tbody></table></body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    //NB: tag soup inserts  colspan and rowspan, we remove that with the StAXTagSoupCleanupBlogBodyTransformer
    // TODO should we also remove the tbody??
    @Test
    public void thIsWrappedInTableTag() throws Exception {
    	String input = "<th>content</th>";
    	String expectedOutput = "<body><table><tbody><tr><th colspan=\"1\" rowspan=\"1\">content</th></tr></tbody></table></body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    @Test
    public void tagsOnlyValidInHeadAreRemoved() throws Exception {
		String input = "<html>" +
				"<head>" +
				"<title>Title text</title>" +
				"<base href=\"http://www.w3schools.com/images/\" target=\"_blank\">" +
				"<style type=\"text/css\">" +
				"h1 {color:red;}" +
				"p {color:blue;}" +
				"</style>" +
				"<link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\">" +
				"</head>" +
				"<body>content</body>" +
				"</html>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml("<body>content</body>")));
    }  

    // LIST RELATED TAGS
    @Test
    public void ddIsWrappedInDlTag() throws Exception {
    	String input = "<dd>content</dd>";
    	String expectedOutput = "<body><dl><dd>content</dd></dl></body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    @Test
    public void dtIsWrappedInDlTag() throws Exception {
    	String input = "<dt>content</dt>";
    	String expectedOutput = "<body><dl><dt>content</dt></dl></body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    @Test
    public void dlHasDtAdded() throws Exception {
    	String input = "<dt>content</dt>";
    	String expectedOutput = "<body><dl><dt>content</dt></dl></body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    @Test
    public void liIsWrappedInUlTag() throws Exception {
    	String input = "<li>content</li>";
    	String expectedOutput = "<body><ul><li>content</li></ul></body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    @Test
    public void olHasContentMovedOut() throws Exception {
    	String input = "<ol>content</ol>";
    	String expectedOutput = "<body><ol/>content</body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    @Test
    public void ulHasContentMovedOut() throws Exception {
    	String input = "<ul>content</ul>";
    	String expectedOutput = "<body><ul/>content</body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    // NB: tag soup inserts clear attribute, we remove that with the StAXTagSoupCleanupBlogBodyTransformer
    @Test
    public void brHasClearAttributeAdded() throws Exception {
    	String input = "<br>content";
    	String expectedOutput = "<body><br clear=\"none\"/>content</body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    // NB: tag soup inserts frameborder and scrolling attributes, we remove those with the StAXTagSoupCleanupBlogBodyTransformer
    @Test
    public void iframeHasFrameborderAndScrollingAttributesAdded() throws Exception {
    	String input = "<iframe>content</iframe>";
    	String expectedOutput = "<body><iframe frameborder=\"1\" scrolling=\"auto\">content</iframe></body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    // NB: tag soup inserts shape attribute, we remove it with the StAXTagSoupCleanupBlogBodyTransformer
    @Test
    public void aHasShapeAttributeAdded() throws Exception {
    	String input = "<a>content</a>";
    	String expectedOutput = "<body><a shape=\"rect\">content</a></body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }
    
    // NB: tagsoup inserts valuetype attribute, which is not valid in html5. We remove it with the StAXTagSoupCleanupBlogBodyTransformer
    @Test
    public void paramHasValuetypeAttributeAddedAndIsWrappedInObject() throws Exception {
    	String input = "<param>content";
    	String expectedOutput = "<body><object><param valuetype=\"data\"/>content</object></body>";
        String output = tagSoupProcessorUnderTest.process(input, simpleContext());
        assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
    }

	private void checkTransformation(String input, String expectedOutput)  {
		String output = tagSoupProcessorUnderTest.process(input, simpleContext());
		 assertThat(the(output), isEquivalentTo(xml(expectedOutput)));
	}
	
	// the following tags have not been seen in blog body but that aren't just preserved as is
    // select, option, area, map, button, form, frame, frameset, input, legend, map, menu
    // noframes, optgroup, option, rt, select, link
}
