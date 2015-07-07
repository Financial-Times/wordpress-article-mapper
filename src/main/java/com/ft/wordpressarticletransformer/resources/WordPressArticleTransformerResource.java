package com.ft.wordpressarticletransformer.resources;

import java.net.URI;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.content.model.Brand;
import com.ft.content.model.Comments;
import com.ft.content.model.Content;
import com.ft.content.model.Identifier;
import com.ft.wordpressarticletransformer.response.Author;
import com.ft.wordpressarticletransformer.response.Post;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformer;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;
import com.sun.jersey.api.NotFoundException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/content")
public class WordPressArticleTransformerResource {
	
	private static final String COMMENT_OPEN_STATUS = "open";

	private static final Logger LOGGER = LoggerFactory.getLogger(WordPressArticleTransformerResource.class);

    private static final String CHARSET_UTF_8 = ";charset=utf-8";
    private final BodyProcessingFieldTransformer bodyProcessingFieldTransformer;
    private final BrandSystemResolver brandSystemResolver;
	
	private WordPressResilientClient wordPressResilientClient;



	public WordPressArticleTransformerResource(BodyProcessingFieldTransformer bodyProcessingFieldTransformer,
                                WordPressResilientClient wordPressResilientClient, BrandSystemResolver brandSystemResolver) {

        this.bodyProcessingFieldTransformer = bodyProcessingFieldTransformer;
        this.wordPressResilientClient = wordPressResilientClient;
        this.brandSystemResolver = brandSystemResolver;
    }
    
	@GET
	@Timed
	@Path("/{uuid}")
	@Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
	public final Content getByPostId(@PathParam("uuid") String uuidString, @QueryParam("url") URI requestUri, @Context HttpHeaders httpHeaders) {
	    if (requestUri == null) {
	        throw new IllegalArgumentException("No url supplied");
	    }
	    
	    if (requestUri.getHost() == null) {
            throw new IllegalArgumentException("Not a valid url");
        }
	    
	    // throws an IllegalArgumentException with a reasonable message if not valid
	    UUID uuid = UUID.fromString(uuidString);
	    
	    String transactionId = TransactionIdUtils.getTransactionIdOrDie(httpHeaders, uuid.toString(), "Publish request");

        Post postDetails = doRequest(requestUri, uuid, transactionId);

		if (postDetails == null) {
		    LOGGER.error("No content was returned for {}", requestUri);
			throw new NotFoundException();
		}
		
		String body = postDetails.getContent();
		if (Strings.isNullOrEmpty(body)) {
		    throw new UnpublishablePostException(requestUri, uuid, "Not a valid WordPress article for publication");
		}
		body = wrapBody(body);
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); //2014-10-21 05:45:30
		String publishedDateStr = null;
        if(postDetails.getModifiedGmt() != null){
            publishedDateStr = postDetails.getModifiedGmt();
        }
        else if (postDetails.getDateGmt() != null) {
            publishedDateStr = postDetails.getDateGmt();
        }
        else {
            LOGGER.error("Modified and Date GMT fields not found for : " + requestUri);
            publishedDateStr = postDetails.getModified();
        }
        DateTime datePublished = formatter.parseDateTime(publishedDateStr);
		
		LOGGER.info("Returning content for uuid [{}].", uuid);

		Brand brand = brandSystemResolver.getBrand(requestUri);

        if(brand == null){
            String msg = String.format("Failed to resolve brand for uri [%s].", requestUri);
			LOGGER.error(msg);
			throw new BrandResolutionException(msg);
        }

        String originatingSystemId = brandSystemResolver.getOriginatingSystemId(requestUri);
        if(originatingSystemId == null){
            String msg = String.format("Failed to resolve originatingSystemId for uri [%s].", requestUri);
            LOGGER.error(msg);
            throw new BrandResolutionException(msg);
        }

        SortedSet<Brand> resolvedBrandWrappedInASet = new TreeSet<>();
        resolvedBrandWrappedInASet.add(brand);

        return Content.builder().withTitle(postDetails.getTitle())
                .withPublishedDate(datePublished.toDate())
                .withXmlBody(tidiedUpBody(body, transactionId))
                .withByline(createBylineFromAuthors(postDetails, requestUri))
                .withIdentifiers(ImmutableSortedSet.of(new Identifier(originatingSystemId, postDetails.getUrl())))
                .withBrands(resolvedBrandWrappedInASet)
                .withComments(createComments(postDetails.getCommentStatus()))
                .withUuid(uuid).build();
	}

    private Comments createComments(String commentStatus) {
        return Comments.builder().withEnabled(areCommentsOpen(commentStatus)).build();
    }

    private boolean areCommentsOpen(String commentStatus) {
        return COMMENT_OPEN_STATUS.equals(commentStatus);
    }
	
    private String createBylineFromAuthors(Post postDetails, URI requestUri) {
        List<Author> authorsList = postDetails.getAuthors();
        
        if (authorsList != null) {
            return authorsList.stream().map(i -> i.getName()).collect(Collectors.joining(", "));
        } else if (postDetails.getAuthor()!= null) {
            return postDetails.getAuthor().getName();
        }
        LOGGER.error("Failed to construct byline");
        throw new WordPressApiException("article has no authors", requestUri);
    }

    private String tidiedUpBody(String body, String transactionId) {
        return bodyProcessingFieldTransformer.transform(body, transactionId);
	}

	private String wrapBody(String originalBody) {
		return "<body>" + originalBody + "</body>";
	}

	private Post doRequest(URI requestUri, UUID uuid, String transactionId) {
		return wordPressResilientClient.getContent(requestUri, uuid, transactionId);
    }
}
