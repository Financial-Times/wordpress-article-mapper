package com.ft.wordpressarticletransformer.resources;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.api.jaxrs.errors.ServerError.ServerErrorBuilder;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.wordpressarticletransformer.exception.NativeStoreReaderUnavailableException;
import com.ft.wordpressarticletransformer.exception.NativeStoreReaderUnreachableException;
import com.ft.wordpressarticletransformer.exception.PostNotFoundException;
import com.ft.wordpressarticletransformer.exception.UnexpectedNativeStoreReaderException;
import com.ft.wordpressarticletransformer.model.WordPressContent;
import com.ft.wordpressarticletransformer.response.Post;
import com.ft.wordpressarticletransformer.response.WordPressPostType;
import com.ft.wordpressarticletransformer.response.WordPressResponse;
import com.ft.wordpressarticletransformer.service.WordpressContentSourceService;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformer;
import com.ft.wordpressarticletransformer.transformer.WordPressBlogPostContentTransformer;
import com.ft.wordpressarticletransformer.transformer.WordPressContentTransformer;
import com.ft.wordpressarticletransformer.transformer.WordPressLiveBlogContentTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;


@Path("/content")
public class WordPressArticleTransformerResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(WordPressArticleTransformerResource.class);

    private static final String CHARSET_UTF_8 = ";charset=utf-8";
    private static final String NATIVE_READER_ERROR_WITH_STATUS_CODE = "Native Reader returned %d";
    private static final String NATIVE_READER_NOT_REACHABLE = "Native Reader is unreachable. Possible network issue.";

    private final WordPressBlogPostContentTransformer blogTransformer;
    private final WordPressLiveBlogContentTransformer liveBlogTransformer;
    private final WordpressContentSourceService wordpressContentSourceService;

    public WordPressArticleTransformerResource(BodyProcessingFieldTransformer bodyProcessingFieldTransformer,
                                               BrandSystemResolver brandSystemResolver,
                                               WordpressContentSourceService wordpressContentSourceService) {
        this.wordpressContentSourceService = wordpressContentSourceService;
        this.blogTransformer = new WordPressBlogPostContentTransformer(brandSystemResolver, bodyProcessingFieldTransformer);
        this.liveBlogTransformer = new WordPressLiveBlogContentTransformer(brandSystemResolver);
    }

    @GET
    @Timed
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final WordPressContent getByPostId(@PathParam("uuid") String uuidString, @Context HttpHeaders httpHeaders) {
        UUID uuid = UUID.fromString(uuidString);
        String transactionId = TransactionIdUtils.getTransactionIdOrDie(httpHeaders, uuid, "Publish request");
        try {
            return getWordpressContent(uuid, transactionId);
        } catch (NativeStoreReaderUnavailableException e) {
            int status = Response.Status.SERVICE_UNAVAILABLE.getStatusCode();
            throw ServerError.status(status)
                    .error(String.format(NATIVE_READER_ERROR_WITH_STATUS_CODE, status)).exception(e);
        } catch (UnexpectedNativeStoreReaderException e) {
            throw ServerError.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).error(e.getMessage()).exception(e);
        } catch (NativeStoreReaderUnreachableException e) {
            throw ServerError.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).error(NATIVE_READER_NOT_REACHABLE).exception(e);
        }
    }

    public WordPressContent getWordpressContent(UUID uuid, String transactionId) {
        WordPressResponse wordpressResponse = wordpressContentSourceService.getValidWordpressResponse(uuid.toString(), transactionId);
        Post postDetails = wordpressResponse.getPost();

        if (postDetails == null) {
            LOGGER.error("No content was returned for {}", uuid);
            throw new PostNotFoundException(
                    uuid.toString(),
                    OffsetDateTime.of(
                        LocalDateTime.ofInstant(wordpressResponse.getLastModified().toInstant(), ZoneId.of(ZoneOffset.UTC.getId())),
                        ZoneOffset.UTC
                    )
            );
        }

        String apiUrl = wordpressResponse.getApiUrl();
        if (apiUrl == null) {
            throw new IllegalArgumentException("No apiUrl supplied");
        }

        URI requestUri = UriBuilder.fromUri(apiUrl).build();
        Date lastModified = wordpressResponse.getLastModified();
        return transformerFor(postDetails).transform(transactionId, requestUri, postDetails, uuid, lastModified);
    }

    private WordPressContentTransformer<?> transformerFor(Post post) {
        WordPressPostType wordPressPostType = null;
        try {
            wordPressPostType = WordPressPostType.fromString(post.getType());
        } catch (IllegalArgumentException e) {/* ignore and throw as below */}

        if (wordPressPostType == null) {
            throw new ServerErrorBuilder(SC_UNPROCESSABLE_ENTITY).error("unsupported blog post type").exception();
        }

        switch (wordPressPostType) {
            case POST:
                return blogTransformer;
            case MARKETS_LIVE:
            case LIVE_Q_AND_A:
            case LIVE_BLOG:
                return liveBlogTransformer;
            default:
                throw new ServerErrorBuilder(SC_UNPROCESSABLE_ENTITY).error("unsupported blog post type").exception();
        }

    }

}
