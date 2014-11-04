package com.ft.wordpressarticletransformer.resources;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.content.model.Brand;
import com.ft.content.model.Content;
import com.ft.wordpressarticletransformer.response.Post;
import com.ft.wordpressarticletransformer.response.WordPressResponse;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformer;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

@Path("/content")
public class WordPressArticleTransformerResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(WordPressArticleTransformerResource.class);

    private static final String CHARSET_UTF_8 = ";charset=utf-8";

	public static final String ORIGINATING_SYSTEM_WORDPRESS = "http://www.ft.com/ontology/origin/FT-LABS-WP-1-242";

    private static final String STATUS_ERROR = "error";
    private static final String ERROR_NOT_FOUND = "Not found.";

    private final BodyProcessingFieldTransformer bodyProcessingFieldTransformer;
    private final BrandResolver brandResolver;
	
	private WordPressResilientClient wordPressResilientClient;


	public WordPressArticleTransformerResource(BodyProcessingFieldTransformer bodyProcessingFieldTransformer,
                                WordPressResilientClient wordPressResilientClient, BrandResolver brandResolver) {

        this.bodyProcessingFieldTransformer = bodyProcessingFieldTransformer;
        this.wordPressResilientClient = wordPressResilientClient;
        this.brandResolver = brandResolver;
    }

	@GET
	@Timed
	@Path("/{uuid}")
	@Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
	public final Content getByPostId(@PathParam("uuid") String uuid, @QueryParam("url") URI requestUri, @Context HttpHeaders httpHeaders) {

	    if (requestUri == null || "".equals(requestUri.toString())) {
	        throw ClientError.status(400).error("No url supplied").exception();
	    }
	    
	    if (requestUri.getHost() == null) {
	        throw ClientError.status(400).error("Not a valid url").exception();
	    }
	    
	    String transactionId = TransactionIdUtils.getTransactionIdOrDie(httpHeaders, uuid, "Publish request");
	    
	    WordPressResponse wordPressResponse = doRequest(requestUri);

		if (wordPressResponse == null) {
			throw new NotFoundException();
		}

		String body = wrapBody(wordPressResponse.getPost().getContent());
		
        Post postDetails = wordPressResponse.getPost();
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); //2014-10-21 05:45:30
		DateTime datePublished = formatter.parseDateTime(postDetails.getDate());
		
		LOGGER.info("Returning content for uuid [{}].", uuid);
		

		Brand brand = brandResolver.getBrand(requestUri);
        SortedSet<Brand> brands = null;

        if(brand == null){


            // We have not been able to establish the brand for this request URL.
            // We are going to pass null to the content builder in the place of brands.


        } else {

            // We have resolved the correct brand for this piece of content to have.
            // We are going to wrap this one brand in a sorted set, and pass it to
            // the content builder.
            brands = new TreeSet<>();
                brands.add(brand);

        }

        return Content.builder().withTitle(postDetails.getTitle())
                .withPublishedDate(datePublished.toDate())
                .withXmlBody(tidiedUpBody(body, transactionId))
                .withByline(postDetails.getAuthor().getName())
                .withContentOrigin(ORIGINATING_SYSTEM_WORDPRESS, postDetails.getUrl())
                .withBrands(new TreeSet<>(Arrays.asList(brand)))
                .withUuid(UUID.fromString(uuid)).build();
	}

    private String tidiedUpBody(String body, String transactionId) {
        try {
		    return bodyProcessingFieldTransformer.transform(body, transactionId);
        } catch (BodyProcessingException bpe) {
            LOGGER.error("Failed to transform body",bpe);
            throw ServerError.status(500).error("article has invalid body").exception(bpe);
        }
	}

	private String wrapBody(String originalBody) {
		return "<body>" + originalBody + "</body>";
	}

	private WordPressResponse doRequest(URI requestUri) {
		
		ClientResponse response = wordPressResilientClient.getContent(requestUri);

		int responseStatusCode = response.getStatus();
		int responseStatusFamily = responseStatusCode / 100;

		if (responseStatusFamily == 2) {
		    WordPressResponse wordPressResponse = null;
		    try {
		        wordPressResponse = response.getEntity(WordPressResponse.class);
		    } catch (ClientHandlerException | UniformInterfaceException e) {
		        throw ClientError.status(400).error(
                        String.format("Response not a valid WordPressResponse - check your url [%s].", requestUri)).exception();
		    } 
		    if (wordPressResponse.getStatus() == null) {
		        throw ClientError.status(400).error(
                        String.format("Response not a valid WordPressResponse - check your url [%s].", requestUri)).exception();
		    }
		    if (STATUS_ERROR.equals(wordPressResponse.getStatus())) {
		        String error = wordPressResponse.getError();
		        if (ERROR_NOT_FOUND.equals(error)) {
	                throw ClientError.status(404).error("Not found").exception();
		        } else {
		            // It says it's an error, but we don't understand this kind of error
		            throw ServerError.status(500).error(
                            String.format("Unexpected error from WordPress: [%s] for url [%s].", error, requestUri)).exception();
		        }
		    }
		    
		    return wordPressResponse;
		} else if (responseStatusFamily == 4) {
		    throw ClientError.status(404).error("Not found").exception();
		} else {
			throw ServerError.status(responseStatusCode).exception();
		}
	}

    private WordPressResponse getJsonFields(ClientResponse response) {   
        String rawOutput = response.getEntity(String.class);
        
        String json = rawOutput.substring(rawOutput.indexOf("{"));
        
        final ObjectMapper objectMapper = new ObjectMapper();
        WordPressResponse wordPressResponse = null;
        
        try {
            wordPressResponse = objectMapper.readValue(json, WordPressResponse.class);
        } catch (IOException e) {
            LOGGER.error("Failed to parse response from WordPress", e);
            throw ServerError.status(500).error("Failed to parse response from WordPress").exception(e);
        }
        

        return wordPressResponse;
    }


}
