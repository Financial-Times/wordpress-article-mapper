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
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
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
            throw ServerError.status(503).error(String.format(NATIVE_READER_ERROR_WITH_STATUS_CODE, 503)).exception(e);
        } catch (UnexpectedNativeStoreReaderException e) {
            throw ServerError.status(500).error(e.getMessage()).exception(e);
        } catch (NativeStoreReaderUnreachableException e) {
            throw ServerError.status(500).error(NATIVE_READER_NOT_REACHABLE).exception(e);
        }
    }

    public WordPressContent getWordpressContent(UUID uuid, String transactionId) {
        WordPressResponse wordpressResponse = wordpressContentSourceService.getValidWordpressResponse(uuid.toString(), transactionId);
        Post postDetails = wordpressResponse.getPost();

        if (postDetails == null) {
            LOGGER.error("No content was returned for {}", uuid);
            throw new PostNotFoundException(uuid.toString());
        }

        String apiUrl = wordpressResponse.getApiUrl();
        if (apiUrl == null) {
            throw new IllegalArgumentException("No apiUrl supplied");
        }

        return transformerFor(postDetails).transform(transactionId, UriBuilder.fromUri(apiUrl).build(), postDetails, uuid);
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
        } catch (IllegalArgumentException e) {/* ignore and throw as below */}

        if (transformer == null) {
            throw new ServerErrorBuilder(SC_UNPROCESSABLE_ENTITY).error("unsupported blog post type").exception();
        }

        return transformer;
    }

}
