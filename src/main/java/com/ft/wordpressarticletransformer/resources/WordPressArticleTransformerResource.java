package com.ft.wordpressarticletransformer.resources;

import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ServerError.ServerErrorBuilder;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.WordPressContent;
import com.ft.wordpressarticletransformer.response.Post;
import com.ft.wordpressarticletransformer.response.WordPressPostType;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformer;
import com.ft.wordpressarticletransformer.transformer.WordPressBlogPostContentTransformer;
import com.ft.wordpressarticletransformer.transformer.WordPressContentTransformer;
import com.ft.wordpressarticletransformer.transformer.WordPressLiveBlogContentTransformer;
import com.sun.jersey.api.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/content")
public class WordPressArticleTransformerResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(WordPressArticleTransformerResource.class);

    private static final String CHARSET_UTF_8 = ";charset=utf-8";

	private final WordPressBlogPostContentTransformer blogTransformer;
	private final WordPressLiveBlogContentTransformer liveBlogTransformer;
    private WordPressResilientClient wordPressResilientClient;

	public WordPressArticleTransformerResource(BodyProcessingFieldTransformer bodyProcessingFieldTransformer,
                                WordPressResilientClient wordPressResilientClient, BrandSystemResolver brandSystemResolver) {

        this.wordPressResilientClient = wordPressResilientClient;
        this.blogTransformer = new WordPressBlogPostContentTransformer(brandSystemResolver, bodyProcessingFieldTransformer);
        this.liveBlogTransformer = new WordPressLiveBlogContentTransformer(brandSystemResolver);
    }
    
	@GET
	@Timed
	@Path("/{uuid}")
	@Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
	public final WordPressContent getByPostId(@PathParam("uuid") String uuidString, @QueryParam("url") URI requestUri, @Context HttpHeaders httpHeaders) {
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
		
        return transformerFor(postDetails).transform(transactionId, requestUri, postDetails, uuid);
	}

	private Post doRequest(URI requestUri, UUID uuid, String transactionId) {
		return wordPressResilientClient.getContent(requestUri, uuid, transactionId);
    }
	
	private WordPressContentTransformer<?> transformerFor(Post post) {
	    WordPressContentTransformer<?> transformer = null;
        try {
            switch (WordPressPostType.fromString(post.getType())) {
                case POST:
                    transformer = blogTransformer;
                    break;
                
                case MARKETS_LIVE:
                case LIVE_Q_AND_A:
                case LIVE_BLOG:
                    transformer = liveBlogTransformer;
                    break;
                
                default:
                    break;
            }
        }
        catch (IllegalArgumentException e) {/* ignore and throw as below */}
        
        if (transformer == null) {
            throw new ServerErrorBuilder(SC_UNPROCESSABLE_ENTITY).error("unsupported blog post type").exception();
        }
	    
        return transformer;
	}
}
