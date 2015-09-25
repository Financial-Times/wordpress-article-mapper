package com.ft.wordpressarticletransformer.resources;

import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Date;

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
import com.ft.content.model.Brand;
import com.ft.content.model.Comments;
import com.ft.content.model.Content;
import com.ft.content.model.Identifier;
import com.ft.wordpressarticletransformer.response.Author;
import com.ft.wordpressarticletransformer.response.Post;
import com.ft.wordpressarticletransformer.response.WordPressPostType;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformer;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;
import com.sun.jersey.api.NotFoundException;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/content")
public class WordPressArticleTransformerResource {
	
	private static final String COMMENT_OPEN_STATUS = "open";

	private static final Logger LOGGER = LoggerFactory.getLogger(WordPressArticleTransformerResource.class);

    private static final String CHARSET_UTF_8 = ";charset=utf-8";

    private static final DateTimeFormatter PUBLISH_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX");

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
	public final Object getByPostId(@PathParam("uuid") String uuidString, @QueryParam("url") URI requestUri, @Context HttpHeaders httpHeaders) {
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
		    throw new UnpublishablePostException(requestUri, uuid, "Not a valid WordPress article for publication - body of post is empty");
		}
		body = wrapBody(body);
		
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
        Date datePublished = Date.from(OffsetDateTime.parse(publishedDateStr + "Z", PUBLISH_DATE_FMT).toInstant());
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
        
        Object content = null;
        
        try {
        switch (WordPressPostType.fromString(postDetails.getType())) {
            case POST:
                Content.Builder builder = Content.builder()
                                                 .withUuid(uuid)
                                                 .withTitle(unescapeHtml4(postDetails.getTitle()))
                                                 .withPublishedDate(datePublished)
                                                 .withByline(unescapeHtml4(createBylineFromAuthors(postDetails, requestUri)))
                                                 .withBrands(resolvedBrandWrappedInASet)
                                                 .withXmlBody(tidiedUpBody(body, transactionId))
                                                 .withIdentifiers(ImmutableSortedSet.of(new Identifier(originatingSystemId, postDetails.getUrl())))
                                                 .withComments(createComments(postDetails.getCommentStatus()));
                
                content = builder.build();
                break;
                
            case MARKETS_LIVE:
            case LIVE_Q_AND_A:
            case LIVE_BLOG:
                Map<String,Object> liveBlog = new LinkedHashMap<>();
                liveBlog.put("uuid", uuid);
                liveBlog.put("title", unescapeHtml4(postDetails.getTitle()));
                liveBlog.put("publishedDate", datePublished);
                liveBlog.put("byline", unescapeHtml4(createBylineFromAuthors(postDetails, requestUri)));
                liveBlog.put("brands", resolvedBrandWrappedInASet);
                liveBlog.put("realtime", true);
                // TODO add webUrl?
                content = liveBlog;
                break;
                
            default:
                break;
        }
        }
        catch (IllegalArgumentException e) {/* ignore and throw as below */}
        
        if (content == null) {
            throw new ServerErrorBuilder(SC_UNPROCESSABLE_ENTITY).error("unsupported blog post type").exception();
        }
        
        return content;
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
