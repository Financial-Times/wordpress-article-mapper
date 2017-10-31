package com.ft.wordpressarticlemapper.transformer;

import com.ft.content.model.Syndication;
import com.ft.wordpressarticlemapper.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticlemapper.model.BlogApiEndpointMetadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SyndicationManagerTest {

    @Mock
    private BlogApiEndpointMetadataManager blogApiEndpointMetadataManager;

    private SyndicationManager syndicationManager;

    @Before
    public void setUp() throws Exception {
        syndicationManager = new SyndicationManager(blogApiEndpointMetadataManager);
    }

    @Test
    public void testGetSyndicationByAuthorityReturnsVerifyWhenAuthorityIsNull() throws Exception {
        Syndication syndication = syndicationManager.getSyndicationByAuthority(null);
        assertThat(syndication, is(equalTo(Syndication.VERIFY)));
    }

    @Test
    public void testGetSyndicationByAuthorityReturnsVerifyWhenAuthorityIsEmpty() throws Exception {
        Syndication syndication = syndicationManager.getSyndicationByAuthority(" ");
        assertThat(syndication, is(equalTo(Syndication.VERIFY)));
    }

    @Test
    public void testGetSyndicationByAuthorityReturnsTheConfiguredSyndication() throws Exception {
        when(blogApiEndpointMetadataManager.getBlogApiEndpointMetadata())
                .thenReturn(Collections.singletonList(
                        new BlogApiEndpointMetadata("host", Collections.emptySet(), "id", Syndication.NO.getCanBeSyndicated())));
        Syndication syndication = syndicationManager.getSyndicationByAuthority("http://api.ft.com/system/id");
        assertThat(syndication, is(equalTo(Syndication.NO)));
    }

    @Test
    public void testGetSyndicationByAuthorityReturnsVerifyIfAuthorityIdIsMissingFromConfiguration() throws Exception {
        when(blogApiEndpointMetadataManager.getBlogApiEndpointMetadata())
                .thenReturn(Collections.singletonList(
                        new BlogApiEndpointMetadata("host", Collections.emptySet(), "FT-LABS-WP-1-335", Syndication.YES.getCanBeSyndicated())));
        Syndication syndication = syndicationManager.getSyndicationByAuthority("http://api.ft.com/system/missing-id");
        assertThat(syndication, is(equalTo(Syndication.VERIFY)));
    }

    @Test
    public void testGetSyndicationByAuthorityReturnsVerifyIfSyndicationFieldIsMissingFromConfiguration() throws Exception {
        when(blogApiEndpointMetadataManager.getBlogApiEndpointMetadata())
                .thenReturn(Collections.singletonList(
                        new BlogApiEndpointMetadata("host", Collections.emptySet(), "FT-LABS-WP-1-335", null)));
        Syndication syndication = syndicationManager.getSyndicationByAuthority("http://api.ft.com/system/FT-LABS-WP-1-335");
        assertThat(syndication, is(equalTo(Syndication.VERIFY)));
    }

    @Test
    public void testGetSyndicationByAuthorityReturnsVerifyIfSyndicationFieldHasInvalidValue() throws Exception {
        when(blogApiEndpointMetadataManager.getBlogApiEndpointMetadata())
                .thenReturn(Collections.singletonList(
                        new BlogApiEndpointMetadata("host", Collections.emptySet(), "FT-LABS-WP-1-335", "invalid")));
        Syndication syndication = syndicationManager.getSyndicationByAuthority("http://api.ft.com/system/FT-LABS-WP-1-335");
        assertThat(syndication, is(equalTo(Syndication.VERIFY)));
    }

    @Test
    public void testGetSyndicationByAuthorityReturnsVerifyIfBlogApiEndpointMetadataManagerIsNull() throws Exception {
        syndicationManager = new SyndicationManager(null);
        Syndication syndication = syndicationManager.getSyndicationByAuthority("http://api.ft.com/system/FT-LABS-WP-1-335");
        assertThat(syndication, is(equalTo(Syndication.VERIFY)));
    }

    @Test
    public void testGetSyndicationByAuthorityReturnsVerifyIfBlogApiEndpointMetadataListIsNull() throws Exception {
        when(blogApiEndpointMetadataManager.getBlogApiEndpointMetadata()).thenReturn(null);
        Syndication syndication = syndicationManager.getSyndicationByAuthority("http://api.ft.com/system/FT-LABS-WP-1-335");
        assertThat(syndication, is(equalTo(Syndication.VERIFY)));
    }
}
