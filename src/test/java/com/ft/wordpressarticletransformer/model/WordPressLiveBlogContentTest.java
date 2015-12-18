package com.ft.wordpressarticletransformer.model;

import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * @author Simon
 */
public class WordPressLiveBlogContentTest {
    @Test
    public void shouldIncludeTemporalBlockInToStringEvenIfNull() throws Exception {
        WordPressLiveBlogContent liveblog = WordPressLiveBlogContent.builder()
                .withUuid(UUID.randomUUID())
                .withTitle("Test").build();

        String result = liveblog.toString();

        assertThat(result,containsString("temporal"));
    }
}
