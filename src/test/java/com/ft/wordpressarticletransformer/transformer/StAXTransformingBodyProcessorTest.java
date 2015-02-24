package com.ft.wordpressarticletransformer.transformer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.xml.StAXTransformingBodyProcessor;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandlerRegistry;
import com.ft.wordpressarticletransformer.transformer.eventhandlers.WordpressVideoXMLEventHandler;
import org.junit.Test;

public class StAXTransformingBodyProcessorTest {
    private StAXTransformingBodyProcessor bodyProcessor;

    @Test
    public void shouldProcessVideoTagCorrectly() {
        XMLEventHandlerRegistry eventHandlerRegistry = new XMLEventHandlerRegistry() {
            { super.registerStartAndEndElementEventHandler(new WordpressVideoXMLEventHandler("video-container", new BaseXMLEventHandler()), "div");}
        };
        bodyProcessor = new StAXTransformingBodyProcessor(eventHandlerRegistry);

        String videoText = "<div class=\"video-container video-container-ftvideo\" data-aspect-ratio=\"16:9\">\n" +
                "   \n\n" +
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
                "</div>" ;

        String processedBody = bodyProcessor.process(videoText, new BodyProcessingContext(){});
        assertThat("processedBody", processedBody, is(equalTo("<a data-asset-type=\"video\" href=\"http://video.ft.com/3791005080001\"></a>")));
    }

    @Test
    public void shouldProcessVideoTagCorrectlyYouTube() {
        XMLEventHandlerRegistry eventHandlerRegistry = new XMLEventHandlerRegistry() {
            { super.registerStartAndEndElementEventHandler(new WordpressVideoXMLEventHandler("video-container", new BaseXMLEventHandler()), "div");}
        };
        bodyProcessor = new StAXTransformingBodyProcessor(eventHandlerRegistry);

        String videoText = "<div class=\"video-container video-container-youtube\" data-aspect-ratio=\"16:9\">" +
                "<div data-asset-type=\"video\" data-asset-source=\"YouTube\" data-asset-ref=\"fRqCVcSWbDc\">" +
                "<iframe width=\"590\" height=\"331\" src=\"http://www.youtube.com/embed/fRqCVcSWbDc?wmode=transparent\" frameborder=\"0\">" +
                "</iframe>" +
                "</div>\n" +
                "</div>" ;

        String processedBody = bodyProcessor.process(videoText, new BodyProcessingContext(){});
        assertThat("processedBody", processedBody, is(equalTo("<a data-asset-type=\"video\" href=\"http://www.youtube.com/embed/fRqCVcSWbDc?wmode=transparent\"></a>")));
    }

    @Test
    public void shouldProcessVideoTagCorrectlyYouTube2() {
        XMLEventHandlerRegistry eventHandlerRegistry = new XMLEventHandlerRegistry() {
            { super.registerStartAndEndElementEventHandler(new WordpressVideoXMLEventHandler("video-container", new BaseXMLEventHandler()), "div");}
        };
        bodyProcessor = new StAXTransformingBodyProcessor(eventHandlerRegistry);

        String videoText = "<div class=\"video-container video-container-youtube\" data-aspect-ratio=\"16:9\"><div data-asset-type=\"video\" data-asset-source=\"YouTube\" data-asset-ref=\"fRqCVcSWbDc\">" +
                "<iframe width=\"590\" height=\"331\" src=\"http://www.youtube.com/embed/fRqCVcSWbDc?wmode=transparent\" frameborder=\"0\" >" +
                "</iframe>" +
                "</div></div>" ;

        String processedBody = bodyProcessor.process(videoText, new BodyProcessingContext(){});
        assertThat("processedBody", processedBody, is(equalTo("<a data-asset-type=\"video\" href=\"http://www.youtube.com/embed/fRqCVcSWbDc?wmode=transparent\"></a>")));
    }


    @Test
    public void shouldProcessVideoTagCorrectlyYouTube3() {

        XMLEventHandlerRegistry eventHandlerRegistry = new XMLEventHandlerRegistry() {
            { super.registerStartAndEndElementEventHandler(new WordpressVideoXMLEventHandler("video-container", new BaseXMLEventHandler()), "div");}
        };
        bodyProcessor = new StAXTransformingBodyProcessor(eventHandlerRegistry);

        String videoText = "<p>Some video (brightcove):</p>\n<div class=\"video-container video-container-ftvideo\" data-aspect-ratio=\"16:9\">\n<div data-asset-type=\"video\" data-asset-source=\"Brightcove\" data-asset-ref=\"3791005080001\"><object class=\"BrightcoveExperience\" id=\"ft_video_54b69ec485a00\"><param name=\"bgcolor\" value=\"#fff1e0\"/><param name=\"width\" value=\"590\"/><param name=\"height\" value=\"331\"/><param name=\"wmode\" value=\"transparent\"/><param name=\"playerID\" value=\"754609517001\"/><param name=\"playerKey\" value=\"AQ~~,AAAACxbljZk~,eD0zYozylZ0BsBE0lwVQCchDhI4xG0tl\"/><param name=\"isVid\" value=\"true\"/><param name=\"isUI\" value=\"true\"/><param name=\"dynamicStreaming\" value=\"true\"/><param name=\"@videoPlayer\" value=\"3791005080001\"/><param name=\"linkBaseURL\" value=\"http://video.ft.com/v/3791005080001\"/><param name=\"includeAPI\" value=\"true\"/><param name=\"templateLoadHandler\" value=\"onTemplateLoaded\"/></object><script> BrightcoveFT.Init.createExperience(\"ft_video_54b69ec485a00\"); BrightcoveFT.eventHandlers[\"ft_video_54b69ec485a00\"].extend({ onTemplateReady:function (e) { this._super(\"onTemplateReady\", e); BrightcoveFT.experiences[this.experienceID].mod.videoPlayer.getCurrentVideo(function (currentVideo) { if (currentVideo === null) { var container = document.getElementById(\"ft_video_54b69ec485a00\"); container.style.display = \"none\"; } }); } });</script></div>\n</div>\n<div class=\"morevideo\"><a href=\"http://video.ft.com/\">More video</a></div>\n<p>Some YouTube video:</p>\n<div class='video-container video-container-youtube' data-aspect-ratio='16:9'>\n<div data-asset-type='video' data-asset-source='YouTube' data-asset-ref='fRqCVcSWbDc'><iframe width='590' height='331' src='http://www.youtube.com/embed/fRqCVcSWbDc?wmode=transparent' frameborder='0'></iframe></div>\n</div>\n<p>An Image:</p>\n<div id=\"attachment_2048592\" class=\"wp-caption alignnone\" style=\"width: 282px\"><a href=\"http://int.ftalphaville.ft.com/files/2014/11/PubQuizGoldman.jpg\" target=\"_blank\"><img class=\"size-medium wp-image-2048592\" title=\"PubQuizGoldman\" src=\"http://int.ftalphaville.ft.com/files/2014/11/PubQuizGoldman-272x188.jpg\" alt=\"Alternate Text\" width=\"272\" height=\"188\" data-img-id=\"2048592\" /></a><p class=\"wp-caption-text\" data-img-id=\"2048592\">Caption for this image</p></div>\n";
        String processedBody = bodyProcessor.process(videoText, new BodyProcessingContext(){});
        assertThat("processedBody", processedBody, is(equalTo("<a data-asset-type=\"video\" href=\"http://video.ft.com/3791005080001\"></a><a data-asset-type=\"video\" href=\"http://www.youtube.com/embed/fRqCVcSWbDc?wmode=transparent\"></a>")));

    }
}
