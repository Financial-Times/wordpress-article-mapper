package com.ft.wordpressarticletransformer.transformer;


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.transformer.FieldTransformer;
import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BodyProcessingFieldTransformerFactoryTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private FieldTransformer bodyTransformer;

    private static final String TRANSACTION_ID = "tid_test";

    @Before
    public void setup() {
        bodyTransformer = new BodyProcessingFieldTransformerFactory().newInstance();
    }

    @Test
    public void tagsShouldBeTransformed() {
        final String originalBody = "<body><p><web-inline-picture fileref=\"/FT/Graphics/Online/Z_" +
                "Undefined/2013/04/600-Saloua-Raouda-Choucair-02.jpg?uuid=7784185e-a888-11e2-8e5d-00144feabdc0\" " +
                "tmx=\"600 445 600 445\"/>\n</p>\n<p id=\"U1060483110029GKD\">In Paris in the late 1940s, a publicity-hungry gallerist " +
                "invited a young, beautiful, unknown Lebanese artist to pose for a photograph alongside Picasso, “before death overtakes him”. " +
                "Without hesitation, Saloua Raouda Choucair said, “As far as I’m concerned, he’s already dead.”</p>\n<p>Did she protest too much? " +
                "Tate’s poster image for the retrospective <i>Saloua Raouda Choucair</i> is a classic post-cubist self-portrait. The artist " +
                "has simplified her features into a mask-like countenance; her clothes – white turban, green sweater, ochre jacket – are " +
                "composed of angular, geometric elements; a background of interlocking jagged shapes underlines the formality of the endeavour. " +
                "It is an engaging image, dominated by the fierce, unswerving gaze of the almond-eyes and the delicately painted turban, " +
                "enclosing the head as if to announce self-reliance, the containment of an inner life. Daring you to want to know more, " +
                "it also keeps you at a distance.</p>\n<p>Raouda Choucair is still unknown, and you can see why Tate Modern selected this " +
                "image to advertise her first western retrospective, which opened this week. But it is a disingenuous choice: the painting " +
                "is the sole portrait in the show, and a rare figurative work. The only others are nudes, made while Raouda Choucair studied " +
                "with “tubist” painter Fernand Léger; they subvert his muscly female figures into awkwardly posed blocks of flesh, " +
                "breasts and faces sketched rudimentarily, to imply a feminist agenda – models reading about art history " +
                "in “Les Peintres Célèbres”, or occupied with housework in “Chores”.</p>\n</body>";

        //Does include some strange extra spaces in the output file
        final String expectedTransformedBody = "<body>\n<p>In Paris in the late 1940s, a publicity-hungry gallerist invited a young, beautiful, unknown Lebanese artist to pose for a photograph " +
                "alongside Picasso, “before death overtakes him”. Without hesitation, Saloua Raouda Choucair said, “As far as I’m concerned, he’s already dead.”</p>\n" +
                "<p>Did she protest too much? Tate’s poster image for the retrospective <em>Saloua Raouda Choucair</em> is a classic post-cubist self-portrait. " +
                "The artist has simplified her features into a mask-like countenance; her clothes – white turban, green sweater, " +
                "ochre jacket – are composed of angular, geometric elements; a background of interlocking jagged shapes underlines the formality of the endeavour. " +
                "It is an engaging image, dominated by the fierce, unswerving gaze of the almond-eyes and the delicately painted turban, enclosing " +
                "the head as if to announce self-reliance, the containment of an inner life. Daring you to want to know more, it also keeps you at a distance.</p>\n" +
                "<p>Raouda Choucair is still unknown, and you can see why Tate Modern selected this image to advertise her first western retrospective, " +
                "which opened this week. But it is a disingenuous choice: the painting is the sole portrait in the show, and a rare figurative work. " +
                "The only others are nudes, made while Raouda Choucair studied with “tubist” painter Fernand Léger; they subvert his muscly female figures into awkwardly " +
                "posed blocks of flesh, breasts and faces sketched rudimentarily, to imply a feminist agenda – models " +
                "reading about art history in “Les Peintres Célèbres”, or occupied with housework in “Chores”.</p>\n</body>";

        checkTransformation(originalBody, expectedTransformedBody);
    }

    @Test
    public void emptyBodyShouldBeReturnedAsEmptyBody() {
    	checkTransformationToEmpty("");
    }

    @Test
    public void shouldThrowExceptionIfBodyNull() {
        expectedException.expect(BodyProcessingException.class);
        expectedException.expect(hasProperty("message", equalTo("Body is null")));
        checkTransformation(null, "");
    }

    @Test
    public void emptyParagraphOnlyShouldBeRemoved() {
        checkTransformationToEmpty("<p></p>");
        checkTransformationToEmpty("<p/>");
    }

    @Test
    public void checkLinkBreaksAreNotCorrupted() {
        String properLineBreak =  wrapped("<p>Blah<br/>Blah</p>");
        checkTransformation(properLineBreak,properLineBreak); // not changed!
    }

    @Test
    public void commentsShouldBeRemoved() {
        checkTransformation("<body>Sentence <!--...-->ending. Next sentence</body>",
                "<body>Sentence ending. Next sentence</body>");
    }
 
    @Test
    public void nameSpacesShouldBeIgnored() {
        checkTransformation(wrapped("<p v:vs=\"|1|\" v:n=\"15\" v:idx=\"11\">Text</p>"), wrapped("<p>Text</p>"));
    }

	private String wrapped(String body) {
		return String.format("<body>%s</body>",body);
	}

	@Test
    public void nbspShouldBeReplacedWithSpace() {
        checkTransformation("<body>This is a sentence&nbsp;.</body>",
                String.format("<body>This is a sentence%s.</body>", String.valueOf('\u00A0')));
    }

    @Test
    public void paraWithOnlyNewlineShouldBeRemoved() {
        checkTransformationToEmpty("<p>\n</p>");
    }

    @Test
    public void paraWithAllElementsRemovedShouldBeRemoved() {
        checkTransformationToEmpty(wrapped("<p><canvas>Canvas is removed</canvas></p>"));
    }

    @Test
    public void encodedNbspShouldBeReplacedWithSpace() {
        checkTransformation("<body>This is a sentence .</body>",
                String.format("<body>This is a sentence%s.</body>", String.valueOf('\u00A0')));
    }

    @Test
    public void htmlEntityReferencesShouldBeUnescaped() {
        String expectedSentence = String.format("<body>This is a sentence%s.</body>", String.valueOf('\u20AC'));
        checkTransformation("<body>This is a sentence&euro;.</body>",expectedSentence);
    }

    @Test
    public void shouldRetainBlockQuotes() {
        String expectedSentence = "<body><p>Pork porchetta landjaeger hamburger sausage turducken leberkas tongue" +
                "tenderloin rump doner. Doner andouille ball tip rump jowl porchetta. Meatball andouille bacon doner," +
                "drumstick filet mignon ball tip frankfurter tail turkey ribeye boudin. Chicken beef swine shank" +
                "sausage flank salami pastrami.</p> <blockquote><p>This is a fine quote. Cometh the man, cometh the" +
                "hour.</p></blockquote> <p>Boudin shoulder</p></body>";
        String straightOutOfWordPress = "<body><p>Pork porchetta landjaeger hamburger sausage turducken leberkas tongue" +
                "tenderloin rump doner. Doner andouille ball tip rump jowl porchetta. Meatball andouille bacon doner," +
                "drumstick filet mignon ball tip frankfurter tail turkey ribeye boudin. Chicken beef swine shank" +
                "sausage flank salami pastrami.</p> <blockquote><p>This is a fine quote. Cometh the man, cometh the" +
                "hour.</p></blockquote> <p>Boudin shoulder</p></body>";

        checkTransformation(straightOutOfWordPress, expectedSentence);
    }

    @Test
    public void shouldNotBarfOnMultipleBlockQuotes() {
        String expectedSentence = "<body><p>Pork porchetta landjaeger hamburger sausage turducken leberkas tongue" +
                "tenderloin rump doner. Doner andouille ball tip rump jowl porchetta. Meatball andouille bacon doner," +
                "drumstick filet mignon ball tip frankfurter tail turkey ribeye boudin. Chicken beef swine shank" +
                "sausage flank salami pastrami.</p> <blockquote><p>This is a fine quote. Cometh the man, cometh the hour." +
                "</p></blockquote> <blockquote><p>This is a fine quote. Cometh the man, cometh the hour.</p>" +
                "</blockquote> <p>Boudin shoulder</p></body>";
        String straightOutOfWordPress = "<body><p>Pork porchetta landjaeger hamburger sausage turducken leberkas tongue" +
                "tenderloin rump doner. Doner andouille ball tip rump jowl porchetta. Meatball andouille bacon doner," +
                "drumstick filet mignon ball tip frankfurter tail turkey ribeye boudin. Chicken beef swine shank" +
                "sausage flank salami pastrami.</p> <blockquote><p>This is a fine quote. Cometh the man, cometh the hour." +
                "</p></blockquote> <blockquote><p>This is a fine quote. Cometh the man, cometh the hour.</p>" +
                "</blockquote> <p>Boudin shoulder</p></body>";

        checkTransformation(straightOutOfWordPress, expectedSentence);
    }
    
    @Test
    public void shouldTransformTweet() {
        String tweetFromWordPress = "<body><div data-asset-type=\"embed\"><blockquote class=\"twitter-tweet\" lang=\"en\">" +
                "<p>Learning from Comcast/TWC? AT&amp;T b DirecTV deal includes collar protecting " +
                "<a href=\"https://twitter.com/search?q=%24DTV&amp;src=ctag\">$DTV</a> shareholders from decline in " +
                "<a href=\"https://twitter.com/search?q=%24T&amp;src=ctag\">$T</a> stock. (Caps upside, too).</p>&mdash; Liz Hoffman (@lizrhoffman) " +
                "<a href=\"https://twitter.com/lizrhoffman/statuses/468146880682016769\">May 18, 2014</a></blockquote>" +
                "<script src=\"//platform.twitter.com/widgets.js\" charset=\"utf-8\"></script></div></body>";

        String expectedSentence = "<body><blockquote class=\"twitter-tweet\" lang=\"en\">" +
                "<p>Learning from Comcast/TWC? AT&amp;T b DirecTV deal includes collar protecting " +
                "<a href=\"https://twitter.com/search?q=%24DTV&amp;src=ctag\">$DTV</a> shareholders from decline in " +
                "<a href=\"https://twitter.com/search?q=%24T&amp;src=ctag\">$T</a> stock. (Caps upside, too).</p>— Liz Hoffman (@lizrhoffman) " +
                "<a href=\"https://twitter.com/lizrhoffman/statuses/468146880682016769\">May 18, 2014</a></blockquote></body>";
        
        checkTransformation(tweetFromWordPress, expectedSentence);

    }

    @Test
    public void shouldNotBarfOnMultipleTweets() {
        String tweetFromWordPress = "<body><div data-asset-type=\"embed\"><blockquote class=\"twitter-tweet\" lang=\"en\">" +
                "<p>Learning from Comcast/TWC? AT&amp;T b DirecTV deal includes collar protecting " +
                "<a href=\"https://twitter.com/search?q=%24DTV&amp;src=ctag\">$DTV</a> shareholders from decline in " +
                "<a href=\"https://twitter.com/search?q=%24T&amp;src=ctag\">$T</a> stock. (Caps upside, too).</p>&mdash; Liz Hoffman (@lizrhoffman) " +
                "<a href=\"https://twitter.com/lizrhoffman/statuses/468146880682016769\">May 18, 2014</a></blockquote>" +
                "<blockquote class=\"twitter-tweet\" lang=\"en\"> <p>Learning from Comcast/TWC? AT&amp;T b DirecTV deal " +
                "includes collar protecting <a href=\"https://twitter.com/search?q=%24DTV&amp;src=ctag\">$DTV</a> " +
                "shareholders from decline in <a href=\"https://twitter.com/search?q=%24T&amp;src=ctag\">$T</a> stock. " +
                "(Caps upside, too).</p>&mdash; Liz Hoffman (@lizrhoffman) <a href=\"https://twitter.com/lizrhoffman/statuses/468146880682016769\">" +
                "May 18, 2014</a></blockquote><script src=\"//platform.twitter.com/widgets.js\" charset=\"utf-8\"></script></div></body>";

        String expectedSentence = "<body><blockquote class=\"twitter-tweet\" lang=\"en\">" +
                "<p>Learning from Comcast/TWC? AT&amp;T b DirecTV deal includes collar protecting " +
                "<a href=\"https://twitter.com/search?q=%24DTV&amp;src=ctag\">$DTV</a> shareholders from decline in " +
                "<a href=\"https://twitter.com/search?q=%24T&amp;src=ctag\">$T</a> stock. (Caps upside, too).</p>— Liz Hoffman (@lizrhoffman) " +
                "<a href=\"https://twitter.com/lizrhoffman/statuses/468146880682016769\">May 18, 2014</a></blockquote><blockquote " +
                "class=\"twitter-tweet\" lang=\"en\"> <p>Learning from Comcast/TWC? AT&amp;T b DirecTV deal includes collar protecting " +
                "<a href=\"https://twitter.com/search?q=%24DTV&amp;src=ctag\">$DTV</a> shareholders from decline in " +
                "<a href=\"https://twitter.com/search?q=%24T&amp;src=ctag\">$T</a> stock. (Caps upside, too).</p>— Liz Hoffman (@lizrhoffman) " +
                "<a href=\"https://twitter.com/lizrhoffman/statuses/468146880682016769\">May 18, 2014</a></blockquote></body>";

        checkTransformation(tweetFromWordPress, expectedSentence);
    }

    private void checkTransformation(String originalBody, String expectedTransformedBody) {
        String actualTransformedBody = bodyTransformer.transform(originalBody, TRANSACTION_ID);
		assertThat(actualTransformedBody, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expectedTransformedBody));
	}


    private void checkTransformationToEmpty(String originalBody) {
        String actualTransformedBody = bodyTransformer.transform(originalBody, TRANSACTION_ID);
        assertThat(actualTransformedBody, is(""));
    }

    @Test
    public void shouldProcessVideoTagCorrectly() {

        String wordpressVideoText = "<body><div class=\"video-container video-container-ftvideo\" data-aspect-ratio=\"16:9\">\n" +
                "   <div data-asset-type=\"video\" data-asset-source=\"Brightcove\" data-asset-ref=\"3791005080001\">\n" +
                "      <object class=\"BrightcoveExperience\" id=\"ft_video_54b3b83e95a74\">\n" +
                "         <param name=\"bgcolor\" value=\"#fff1e0\"/>\n" +
                "         <param name=\"width\" value=\"590\"/>\n" +
                "         <param name=\"height\" value=\"331\"/>\n" +
                "         <param name=\"wmode\" value=\"transparent\"/>\n" +
                "         <param name=\"playerID\" value=\"754609517001\"/>\n" +
                "         <param name=\"playerKey\" value=\"AQ~~,AAAACxbljZk~,eD0zYozylZ0BsBE0lwVQCchDhI4xG0tl\"/>\n" +
                "         <param name=\"isVid\" value=\"true\"/>\n" +
                "         <param name=\"isUI\" value=\"true\"/>\n" +
                "         <param name=\"dynamicStreaming\" value=\"true\"/>\n" +
                "         <param name=\"@videoPlayer\" value=\"3791005080001\"/>\n" +
                "         <param name=\"linkBaseURL\" value=\"http://video.ft.com/v/3791005080001\"/>\n" +
                "         <param name=\"includeAPI\" value=\"true\"/>\n" +
                "         <param name=\"templateLoadHandler\" value=\"onTemplateLoaded\"/>\n" +
                "      </object>\n" +
                "      <script> BrightcoveFT.Init.createExperience(\"ft_video_54b3b83e95a74\"); BrightcoveFT.eventHandlers[\"ft_video_54b3b83e95a74\"].extend({ onTemplateReady:function (e) { this._super(\"onTemplateReady\", e); BrightcoveFT.experiences[this.experienceID].mod.videoPlayer.getCurrentVideo(function (currentVideo) { if (currentVideo === null) { var container = document.getElementById(\"ft_video_54b3b83e95a74\"); container.style.display = \"none\"; } }); } });</script>\n" +
                "   </div>\n" +
                "   \n" +
                "</div></body>" ;
        String expectedVideo =  "<body><a data-asset-type=\"video\" data-embedded=\"true\" href=\"http://video.ft.com/3791005080001\"></a></body>";
        checkTransformation(wordpressVideoText, expectedVideo);
    }

    @Test
    public void shouldProcessVideoTagCorrectlyYouTube() {
        String wordpressYouTube = "<div class=\"video-container video-container-youtube\" data-aspect-ratio=\"16:9\">" +
                "<div data-asset-type=\"video\" data-asset-source=\"YouTube\" data-asset-ref=\"fRqCVcSWbDc\">" +
                "<iframe width=\"590\" height=\"331\" src=\"http://www.youtube.com/embed/fRqCVcSWbDc?wmode=transparent\" frameborder=\"0\">" +
                "</iframe>" +
                "</div>\n" +
                "</div>" ;

        String expectedYouTube = "<body><a data-asset-type=\"video\" data-embedded=\"true\" href=\"http://www.youtube.com/embed/fRqCVcSWbDc?wmode=transparent\"></a></body>";
        checkTransformation(wordpressYouTube, expectedYouTube);
    }

    @Test
    public void shouldProcessVideoTagCorrectlyYouTube2() {
        String videoText = "<body><div class=\"video-container video-container-youtube\" data-aspect-ratio=\"16:9\"><div data-asset-type=\"video\" data-asset-source=\"YouTube\" data-asset-ref=\"fRqCVcSWbDc\">" +
                "<iframe width=\"590\" height=\"331\" src=\"http://www.youtube.com/embed/fRqCVcSWbDc?wmode=transparent\" frameborder=\"0\" >" +
                "</iframe>" +
                "</div></div></body>" ;

        String expectedYouTube = "<body><a data-asset-type=\"video\" data-embedded=\"true\" href=\"http://www.youtube.com/embed/fRqCVcSWbDc?wmode=transparent\"></a></body>";
        checkTransformation(videoText, expectedYouTube);
    }


    @Test
    public void shouldProcessVideoCombinedVideoTagsCorrectly() {
        String videoText = "<body><p>Some video (brightcove):</p>" +
                "<div class=\"video-container video-container-ftvideo\" data-aspect-ratio=\"16:9\">" +
                "<div data-asset-type=\"video\" data-asset-source=\"Brightcove\" data-asset-ref=\"3791005080001\">" +
                "<object class=\"BrightcoveExperience\" id=\"ft_video_54b69ec485a00\">" +
                "<param name=\"bgcolor\" value=\"#fff1e0\"/><param name=\"width\" value=\"590\"/><param name=\"height\" value=\"331\"/><param name=\"wmode\" value=\"transparent\"/><param name=\"playerID\" value=\"754609517001\"/><param name=\"playerKey\" value=\"AQ~~,AAAACxbljZk~,eD0zYozylZ0BsBE0lwVQCchDhI4xG0tl\"/><param name=\"isVid\" value=\"true\"/><param name=\"isUI\" value=\"true\"/><param name=\"dynamicStreaming\" value=\"true\"/><param name=\"@videoPlayer\" value=\"3791005080001\"/><param name=\"linkBaseURL\" value=\"http://video.ft.com/v/3791005080001\"/><param name=\"includeAPI\" value=\"true\"/><param name=\"templateLoadHandler\" value=\"onTemplateLoaded\"/>" +
                "</object>" +
                "<script> BrightcoveFT.Init.createExperience(\"ft_video_54b69ec485a00\"); BrightcoveFT.eventHandlers[\"ft_video_54b69ec485a00\"].extend({ onTemplateReady:function (e) { this._super(\"onTemplateReady\", e); BrightcoveFT.experiences[this.experienceID].mod.videoPlayer.getCurrentVideo(function (currentVideo) { if (currentVideo === null) { var container = document.getElementById(\"ft_video_54b69ec485a00\"); container.style.display = \"none\"; } }); } });</script></div>\n</div>\n" +
                "<div class=\"morevideo\"><a href=\"http://video.ft.com/\">More video</a></div>" +
                "<p>Some YouTube video:</p>" +
                "<div class='video-container video-container-youtube' data-aspect-ratio='16:9'>" +
                "<div data-asset-type='video' data-asset-source='YouTube' data-asset-ref='fRqCVcSWbDc'>" +
                "<iframe width='590' height='331' src='http://www.youtube.com/embed/fRqCVcSWbDc?wmode=transparent' frameborder='0'></iframe></div></div>" +
                "<p>An Image:</p>" +
                "<div id=\"attachment_2048592\" class=\"wp-caption align none\" style=\"width: 282px\">" +
                "<a href=\"http://int.ftalphaville.ft.com/files/2014/11/PubQuizGoldman.jpg\" target=\"_blank\">" +
                "<img class=\"size-medium wp-image-2048592\" title=\"PubQuizGoldman\" src=\"http://int.ftalphaville.ft.com/files/2014/11/PubQuizGoldman-272x188.jpg\" alt=\"Alternate Text\" width=\"272\" height=\"188\" data-img-id=\"2048592\" /></a>" +
                "<p class=\"wp-caption-text\" data-img-id=\"2048592\">Caption for this image</p></div></body>";
        String expectedYouTube = "<body><p>Some video (brightcove):</p><a data-asset-type=\"video\" data-embedded=\"true\" href=\"http://video.ft.com/3791005080001\"></a> <p>Some YouTube video:</p><a data-asset-type=\"video\" data-embedded=\"true\" href=\"http://www.youtube.com/embed/fRqCVcSWbDc?wmode=transparent\"></a><p>An Image:</p></body>";
        checkTransformation(videoText, expectedYouTube);
    }

}
