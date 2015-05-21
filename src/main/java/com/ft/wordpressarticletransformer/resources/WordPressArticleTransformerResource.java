package com.ft.wordpressarticletransformer.resources;

import java.net.URI;
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

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.content.model.Brand;
import com.ft.content.model.Content;
import com.ft.wordpressarticletransformer.response.Post;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformer;
import com.sun.jersey.api.NotFoundException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/content")
public class WordPressArticleTransformerResource {

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
	public final Content getByPostId(@PathParam("uuid") String uuid, @QueryParam("url") URI requestUri, @Context HttpHeaders httpHeaders) {

	    if (requestUri == null || "".equals(requestUri.toString())) {
	        throw ClientError.status(400).error("No url supplied").exception();
	    }
	    
	    if (requestUri.getHost() == null) {
            throw ClientError.status(400).error("Not a valid url").exception();
        }
        UUID validUuid = null;
        try {
            validUuid = UUID.fromString(uuid);
        }
        catch(Exception e){
            throw ClientError.status(400).error("Not a valid uuid").exception();
        }
	    
	    String transactionId = TransactionIdUtils.getTransactionIdOrDie(httpHeaders, validUuid.toString(), "Publish request");

        Post postDetails = doRequest(requestUri, validUuid, transactionId);

		if (postDetails == null) {
			throw new NotFoundException();
		}

		String body = wrapBody(postDetails.getContent());
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); //2014-10-21 05:45:30
		DateTime datePublished = formatter.parseDateTime(postDetails.getDate());
		
		LOGGER.info("Returning content for uuid [{}].", validUuid.toString());

		Brand brand = brandSystemResolver.getBrand(requestUri);

        if(brand == null){
			LOGGER.error("Failed to resolve brand for uri [{}].", requestUri);
			throw ServerError.status(500).error(String.format("Failed to resolve brand for uri [%s].", requestUri)).exception();
        }

        String originatingSystemId = brandSystemResolver.getOriginatingSystemId(requestUri);
        if(originatingSystemId == null){
            LOGGER.error("Failed to resolve brand for uri [{}].", requestUri);
            throw ServerError.status(500).error(String.format("Failed to resolve originatingSystemId for uri [%s].", requestUri)).exception();
        }

        SortedSet<Brand> resolvedBrandWrappedInASet = new TreeSet<>();
        resolvedBrandWrappedInASet.add(brand);

        return Content.builder().withTitle(postDetails.getTitle())
                .withPublishedDate(datePublished.toDate())
                .withXmlBody(tidiedUpBody(body, transactionId))
                .withByline(postDetails.getAuthor().getName())
                .withContentOrigin(originatingSystemId, postDetails.getUrl())
                .withBrands(resolvedBrandWrappedInASet)
                .withUuid(validUuid).build();
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

	private Post doRequest(URI requestUri, UUID uuid, String transactionId) {
		
		Post post;

        try {
            post = wordPressResilientClient.getContent(requestUri, uuid, transactionId);

            if (post == null) {
                LOGGER.error("No content was returned");
                return null;
            }
            return post;
        } catch (InvalidResponseException e) {
            throw ClientError.status(400).error(e.getMessage()).exception(e);
        } catch (UnsupportedPostTypeException e) {
            throw ClientError.status(404).error(e.getMessage()).exception(e);
        } catch (PostNotFoundException e) {
            throw ClientError.status(404).context(uuid).error(e.getMessage()).exception(e);
        } catch (UnexpectedErrorCodeException | UnexpectedStatusFieldException | UnexpectedStatusCodeException e) {
            throw ServerError.status(500).error(e.getMessage()).exception(e);
        } catch (RequestFailedException | CannotConnectToWordPressException e) {
            throw ServerError.status(503).error(e.getMessage()).exception(e);
        }

    }

}
