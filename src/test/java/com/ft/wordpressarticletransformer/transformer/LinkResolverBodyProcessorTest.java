package com.ft.wordpressarticletransformer.transformer;

import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_TEMPORARILY;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.ws.rs.core.UriBuilder;

import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.junit.Test;
import org.mockito.InOrder;

import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.wordpressarticletransformer.model.Brand;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class LinkResolverBodyProcessorTest {
  static final String ARTICLE_TYPE = "http://www.ft.com/ontology/content/Article";
  
  private static final Pattern SHORT_URL_PATTERN = Pattern.compile("http:\\/\\/short\\.example\\.com\\/.*");
  private static final String BRAND_ID = "http://api.ft.com/system/JUNIT";
  private static final URI DOC_STORE_QUERY = URI.create("http://localhost:8080/content-query");
  
  private Client resolverClient = mock(Client.class);
  private Client documentStoreQueryClient = mock(Client.class);
  
  private LinkResolverBodyProcessor processor = new LinkResolverBodyProcessor(
    Collections.singleton(SHORT_URL_PATTERN), resolverClient,
    Collections.singletonMap(Pattern.compile("http:\\/www\\.ft\\.com\\/resolved\\/.*"), new Brand(BRAND_ID)),
    documentStoreQueryClient, DOC_STORE_QUERY);
  
  
  @Test
  public void thatShortenedLinksAreResolvedToContent() {
    URI shortUrl = URI.create("http://short.example.com/foobar");
    String resolvedIdentifier = "http:/www.ft.com/resolved/foo/bar";
    UUID ftContentUUID = UUID.randomUUID();
    String bodyWithShortLink = "<body><p>Blah blah blah <a href=\"" + shortUrl
        + "\">usw</a> ...</p></body>";
    
    String expectedTransformed = "<body><p>Blah blah blah <content id=\"" + ftContentUUID
        + "\" type=\"" + ARTICLE_TYPE + "\">usw</content> ...</p></body>";
    
    WebResource resolverBuilder = mock(WebResource.class);
    when(resolverClient.resource(shortUrl)).thenReturn(resolverBuilder);
    
    ClientResponse redirectionResponse = mock(ClientResponse.class);
    when(redirectionResponse.getStatus()).thenReturn(SC_MOVED_TEMPORARILY);
    when(redirectionResponse.getLocation()).thenReturn(URI.create(resolvedIdentifier));
    when(resolverBuilder.head()).thenReturn(redirectionResponse);
    
    WebResource queryResource = mock(WebResource.class);
    WebResource.Builder queryBuilder = mock(WebResource.Builder.class);
    URI queryURI = UriBuilder.fromUri(DOC_STORE_QUERY)
                             .queryParam("identifierAuthority", BRAND_ID)
                             .queryParam("identifierValue", URI.create(resolvedIdentifier))
                             .build();
    
    when(documentStoreQueryClient.resource(queryURI)).thenReturn(queryResource);
    when(queryResource.header("Host", "document-store-api")).thenReturn(queryBuilder);
    
    ClientResponse queryResponse = mock(ClientResponse.class);
    when(queryResponse.getStatus()).thenReturn(SC_MOVED_PERMANENTLY);
    when(queryResponse.getLocation()).thenReturn(URI.create("http://www.ft.com/content/" + ftContentUUID));
    when(queryBuilder.head()).thenReturn(queryResponse);
    
    String actual = processor.process(bodyWithShortLink, null);
    assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expectedTransformed));
    
    InOrder inOrder = inOrder(resolverClient);
    inOrder.verify(resolverClient).setFollowRedirects(false);
    inOrder.verify(resolverClient).resource(shortUrl);
    
    inOrder = inOrder(documentStoreQueryClient);
    inOrder.verify(documentStoreQueryClient).setFollowRedirects(false);
    inOrder.verify(documentStoreQueryClient).resource(queryURI);
  }
  
  @Test
  public void thatShortenedLinksForNonFTContentAreNotTransformed() {
    URI shortUrl = URI.create("http://short.example.com/foobar");
    URI redirectionUrl = URI.create("http://www.example.org/");
    
    String body = "<body><p>Blah blah blah <a href=\"" + shortUrl
        + "\">usw</a> ...</p></body>";
    
    WebResource resolverBuilder1 = mock(WebResource.class);
    when(resolverClient.resource(shortUrl)).thenReturn(resolverBuilder1);
    
    ClientResponse redirectionResponse1 = mock(ClientResponse.class);
    when(redirectionResponse1.getStatus()).thenReturn(SC_MOVED_TEMPORARILY);
    when(redirectionResponse1.getLocation()).thenReturn(redirectionUrl);
    when(resolverBuilder1.head()).thenReturn(redirectionResponse1);
    
    WebResource resolverBuilder2 = mock(WebResource.class);
    when(resolverClient.resource(redirectionUrl)).thenReturn(resolverBuilder2);
    
    ClientResponse redirectionResponse2 = mock(ClientResponse.class);
    when(redirectionResponse2.getStatus()).thenReturn(SC_OK);
    when(resolverBuilder2.head()).thenReturn(redirectionResponse2);
    
    String actual = processor.process(body, null);
    assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(body));
    
    InOrder inOrder = inOrder(resolverClient);
    inOrder.verify(resolverClient).setFollowRedirects(false);
    inOrder.verify(resolverClient).resource(shortUrl);
    
    verify(documentStoreQueryClient, never()).resource(any(URI.class));
  }
  
  @Test
  public void thatOtherLinksAreNotTransformed() {
    String body = "<body><p>Blah blah blah <a href=\"http://www.example.com/foobar\">usw</a> ...</p></body>";
    
    String actual = processor.process(body, null);
    assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(body));
    verify(resolverClient, never()).resource(any(URI.class));
    verify(documentStoreQueryClient, never()).resource(any(URI.class));
  }
  
  @Test
  public void thatCircularShortenedLinksAreNotTransformed() {
    String shortUrl = "http://short.example.com/foobar";
    String body = "<body><p>Blah blah blah <a href=\"" + shortUrl
        + "\">usw</a> ...</p></body>";
    
    WebResource resolverBuilder = mock(WebResource.class);
    when(resolverClient.resource(URI.create(shortUrl))).thenReturn(resolverBuilder);
    
    ClientResponse redirectionResponse = mock(ClientResponse.class);
    when(redirectionResponse.getStatus()).thenReturn(SC_MOVED_TEMPORARILY);
    when(redirectionResponse.getLocation()).thenReturn(URI.create(shortUrl));
    when(resolverBuilder.head()).thenReturn(redirectionResponse);
    
    String actual = processor.process(body, null);
    assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(body));
    verify(documentStoreQueryClient, never()).resource(any(URI.class));
  }
  
  @Test
  public void thatShortenedLinksWithErrorResponsesAreNotTransformed() {
    String shortUrl = "http://short.example.com/foobar";
    String body = "<body><p>Blah blah blah <a href=\"" + shortUrl
        + "\">usw</a> ...</p></body>";
    
    WebResource resolverBuilder = mock(WebResource.class);
    when(resolverClient.resource(URI.create(shortUrl))).thenReturn(resolverBuilder);
    
    ClientResponse redirectionResponse = mock(ClientResponse.class);
    when(redirectionResponse.getStatus()).thenReturn(SC_NOT_FOUND);
    when(resolverBuilder.head()).thenReturn(redirectionResponse);
    
    String actual = processor.process(body, null);
    assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(body));
    verify(documentStoreQueryClient, never()).resource(any(URI.class));
  }
  
  @Test
  public void thatShortenedLinksResolvedButNotFoundAreNotTransformed() {
    String shortUrl = "http://short.example.com/foobar";
    String resolvedIdentifier = "http:/www.ft.com/resolved/foo/bar";
    String body = "<body><p>Blah blah blah <a href=\"" + shortUrl
        + "\">usw</a> ...</p></body>";
    
    WebResource resolverBuilder = mock(WebResource.class);
    when(resolverClient.resource(URI.create(shortUrl))).thenReturn(resolverBuilder);
    
    ClientResponse redirectionResponse = mock(ClientResponse.class);
    when(redirectionResponse.getStatus()).thenReturn(SC_MOVED_TEMPORARILY);
    when(redirectionResponse.getLocation()).thenReturn(URI.create(resolvedIdentifier));
    when(resolverBuilder.head()).thenReturn(redirectionResponse);
    
    WebResource queryResource = mock(WebResource.class);
    WebResource.Builder queryBuilder = mock(WebResource.Builder.class);
    URI queryURI = UriBuilder.fromUri(DOC_STORE_QUERY)
                             .queryParam("identifierAuthority", BRAND_ID)
                             .queryParam("identifierValue", URI.create(resolvedIdentifier))
                             .build();
    
    when(documentStoreQueryClient.resource(queryURI)).thenReturn(queryResource);
    when(queryResource.header("Host", "document-store-api")).thenReturn(queryBuilder);
    
    ClientResponse queryResponse = mock(ClientResponse.class);
    when(queryResponse.getStatus()).thenReturn(SC_NOT_FOUND);
    when(queryBuilder.head()).thenReturn(queryResponse);
    
    String actual = processor.process(body, null);
    assertThat(actual, IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(body));
  }
  
  @Test(expected = BodyProcessingException.class)
  public void thatBadlyFormedContentIsRejected() {
    processor.process("<foo>", null);
  }
}
