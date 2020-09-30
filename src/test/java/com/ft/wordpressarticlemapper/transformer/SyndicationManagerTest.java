package com.ft.wordpressarticlemapper.transformer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.ft.content.model.Syndication;
import com.ft.wordpressarticlemapper.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticlemapper.model.BlogApiEndpointMetadata;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SyndicationManagerTest {

  @Mock private BlogApiEndpointMetadataManager blogApiEndpointMetadataManager;

  private SyndicationManager syndicationManager;

  @Before
  public void setUp() throws Exception {
    syndicationManager = new SyndicationManager(blogApiEndpointMetadataManager);
  }

  @Test
  public void testGetSyndicationByUriVerifyWhenNullUriIsPassed() {
    Syndication syndication = syndicationManager.getSyndicationByUri(null);
    assertThat(syndication, is(equalTo(Syndication.VERIFY)));
  }

  @Test
  public void testGetSyndicationByUriVerifyWhenEmptyUriIsPassed() throws Exception {
    Syndication syndication = syndicationManager.getSyndicationByUri(new URI(""));
    assertThat(syndication, is(equalTo(Syndication.VERIFY)));
  }

  @Test
  public void testGetSyndicationByUriReturnsTheConfiguredSyndication() throws URISyntaxException {
    when(blogApiEndpointMetadataManager.getBlogApiEndpointMetadataByUri(any(URI.class)))
        .thenReturn(
            new BlogApiEndpointMetadata(
                "host", Collections.emptySet(), "id", Syndication.NO.getCanBeSyndicated()));
    Syndication syndication =
        syndicationManager.getSyndicationByUri(
            new URI("http://www.ft.com/fastft/api/get_post/?id=704836"));
    assertThat(syndication, is(equalTo(Syndication.NO)));
  }

  @Test
  public void testGetSyndicationByUriReturnsVerifyIfIdIsMissingFromConfiguration()
      throws Exception {
    when(blogApiEndpointMetadataManager.getBlogApiEndpointMetadataByUri(any(URI.class)))
        .thenReturn(null);
    Syndication syndication =
        syndicationManager.getSyndicationByUri(
            new URI("http://www.ft.com/fastft/api/get_post/?id=704836"));
    assertThat(syndication, is(equalTo(Syndication.VERIFY)));
  }

  @Test
  public void testGetSyndicationByUriReturnsVerifyIfSyndicationFieldIsMissingFromConfiguration()
      throws Exception {
    when(blogApiEndpointMetadataManager.getBlogApiEndpointMetadataByUri(any(URI.class)))
        .thenReturn(new BlogApiEndpointMetadata("host", Collections.emptySet(), "id", null));
    Syndication syndication =
        syndicationManager.getSyndicationByUri(
            new URI("http://www.ft.com/fastft/api/get_post/?id=704836"));
    assertThat(syndication, is(equalTo(Syndication.VERIFY)));
  }

  @Test
  public void testGetSyndicationByUriReturnsVerifyIfSyndicationFieldHasInvalidValue()
      throws Exception {
    when(blogApiEndpointMetadataManager.getBlogApiEndpointMetadataByUri(any(URI.class)))
        .thenReturn(new BlogApiEndpointMetadata("host", Collections.emptySet(), "id", "invalid"));
    Syndication syndication =
        syndicationManager.getSyndicationByUri(
            new URI("http://www.ft.com/fastft/api/get_post/?id=704836"));
    assertThat(syndication, is(equalTo(Syndication.VERIFY)));
  }

  @Test
  public void testGetSyndicationByUriReturnsVerifyIfBlogApiEndpointMetadataManagerIsNull()
      throws Exception {
    syndicationManager = new SyndicationManager(null);
    Syndication syndication =
        syndicationManager.getSyndicationByUri(
            new URI("http://www.ft.com/fastft/api/get_post/?id=704836"));
    assertThat(syndication, is(equalTo(Syndication.VERIFY)));
  }

  @Test
  public void testGetSyndicationByUriReturnsVerifyIfBlogApiEndpointMetadataIsNull()
      throws Exception {
    when(blogApiEndpointMetadataManager.getBlogApiEndpointMetadataByUri(any(URI.class)))
        .thenReturn(null);
    Syndication syndication =
        syndicationManager.getSyndicationByUri(
            new URI("http://www.ft.com/fastft/api/get_post/?id=704836"));
    assertThat(syndication, is(equalTo(Syndication.VERIFY)));
  }
}
