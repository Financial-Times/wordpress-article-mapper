package com.ft.wordpressarticletransformer.transformer;

import com.ft.bodyprocessing.richcontent.ConvertParameters;
import com.ft.bodyprocessing.richcontent.RichContentItem;
import com.ft.bodyprocessing.richcontent.Video;
import com.ft.bodyprocessing.richcontent.VideoMatcher;
import com.ft.bodyprocessing.richcontent.VideoSiteConfiguration;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RetainYoutubeParametersTest {

    private static final List<String> T = Collections.singletonList("t");
    private static final List<String> START = Collections.singletonList("start");
    private static final List<String> NONE = Collections.emptyList();

    private static final String STARTING_PARAMETER = "start";
    private static final String CONVERTED_PARAMETER = "t";
    private static final String CONVERSION_TEMPLATE = "s";
    private static final ConvertParameters CONVERT_PARAMETERS = new ConvertParameters(STARTING_PARAMETER, CONVERTED_PARAMETER, CONVERSION_TEMPLATE);
    private static final List<ConvertParameters> CONVERT_PARAMETERS_LIST = ImmutableList.of(CONVERT_PARAMETERS);

    public static List<VideoSiteConfiguration> DEFAULTS = Arrays.asList(
            new VideoSiteConfiguration("https?://www.youtube.com/watch\\?v=(?<id>[A-Za-z0-9_-]+)", "https://www.youtube.com/watch?v=%s", true, T, null, true),
            new VideoSiteConfiguration("https?://www.youtube.com/embed/(?<id>[A-Za-z0-9_-]+)", "https://www.youtube.com/watch?v=%s", false, START, CONVERT_PARAMETERS_LIST, true),
            new VideoSiteConfiguration("https?://youtu.be/(?<id>[A-Za-z0-9_-]+)", "https://www.youtube.com/watch?v=%s", false, T, null, true),
            new VideoSiteConfiguration("https?://www.vimeo.com/(?<id>[0-9]+)", null, false, NONE, null, true),
            new VideoSiteConfiguration("//player.vimeo.com/video/(?<id>[0-9]+)", "https://www.vimeo.com/%s", true, NONE, null, true),
            new VideoSiteConfiguration("https?://video.ft.com/(?<id>[0-9]+)/", null, false, NONE, null, true)
    );

    private List<VideoSiteConfiguration> videoSiteConfigurationList = DEFAULTS;

    @Mock private VideoSiteConfiguration videoSiteConfiguration;

    @Test
    public void shouldRetainTimeParameterOnYoutubeWatchUrls() {
        RichContentItem attachment = new RichContentItem("http://www.youtube.com/watch?v=V8B4CjOkcck&t=30s", "Title");
        String result = matchVideoFormat(attachment);
        assertThat(result, is("https://www.youtube.com/watch?v=V8B4CjOkcck&t=30s"));
    }

    @Test
    public void shouldNotRetainOtherParameterOnYoutubeWatchUrls() {
        RichContentItem attachment = new RichContentItem("http://www.youtube.com/watch?v=V8B4CjOkcck&feature=youtu.be&color=red", "Title");
        String result = matchVideoFormat(attachment);
        assertThat(result, is("https://www.youtube.com/watch?v=V8B4CjOkcck"));
    }

    @Test
         public void shouldRetainStartParameterAndConvertToTimeOnYoutubeEmbedUrls() {
        RichContentItem attachment = new RichContentItem("http://www.youtube.com/embed/V8B4CjOkcck?start=30", "Title");
        String result = matchVideoFormat(attachment);
        assertThat(result, is("https://www.youtube.com/watch?v=V8B4CjOkcck&t=30s"));
    }

    @Test
    public void shouldNotRetainTimeParameterOnYoutubeEmbedUrls() {
        RichContentItem attachment = new RichContentItem("http://www.youtube.com/embed/V8B4CjOkcck?t=30s", "Title");
        String result = matchVideoFormat(attachment);
        assertThat(result, is("https://www.youtube.com/watch?v=V8B4CjOkcck"));
    }

    @Test
    public void shouldNotRetainOtherParameterOnYoutubeEmbedUrls() {
        RichContentItem attachment = new RichContentItem("http://www.youtube.com/embed/V8B4CjOkcck?feature=youtu.be&color=red", "Title");
        String result = matchVideoFormat(attachment);
        assertThat(result, is("https://www.youtube.com/watch?v=V8B4CjOkcck"));
    }

    @Test
    public void shouldRetainTimeParameterOnYoutubeShortUrls() {
        RichContentItem attachment = new RichContentItem("http://youtu.be/V8B4CjOkcck?t=30s", "Title");
        String result = matchVideoFormat(attachment);
        assertThat(result, is("https://www.youtube.com/watch?v=V8B4CjOkcck&t=30s"));
    }

    @Test
    public void shouldNotRetainOtherParameterOnYoutubeShortUrls() {
        RichContentItem attachment = new RichContentItem("http://youtu.be/V8B4CjOkcck?feature=youtu.be&color=red", "Title");
        String result = matchVideoFormat(attachment);
        assertThat(result, is("https://www.youtube.com/watch?v=V8B4CjOkcck"));
    }

    private String matchVideoFormat(RichContentItem attachment) {
        VideoMatcher matcher = new VideoMatcher(videoSiteConfigurationList);
        Video video = matcher.filterVideo(attachment);
        return video.getUrl();
    }
}
