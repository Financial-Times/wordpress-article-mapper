package com.ft.wordpressarticlemapper.resources;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.wordpressarticlemapper.exception.PostNotFoundException;
import com.ft.wordpressarticlemapper.exception.WordPressContentException;
import com.ft.wordpressarticlemapper.messaging.MessageProducingContentMapper;
import com.ft.wordpressarticlemapper.model.WordPressContent;
import com.ft.wordpressarticlemapper.response.NativeWordPressContent;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.wordpressarticlemapper.response.WordPressPostType;
import com.ft.wordpressarticlemapper.transformer.WordPressBlogPostContentMapper;
import com.ft.wordpressarticlemapper.transformer.WordPressContentMapper;
import com.ft.wordpressarticlemapper.transformer.WordPressLiveBlogContentMapper;
import com.ft.wordpressarticlemapper.validation.NativeWordPressContentValidator;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;


@Path("/")
public class WordPressArticleMapperResource {

    private static final String CHARSET_UTF_8 = ";charset=utf-8";

    private final WordPressBlogPostContentMapper blogTransformer;
    private final WordPressLiveBlogContentMapper liveBlogTransformer;
    private final MessageProducingContentMapper contentMapper;
    private final NativeWordPressContentValidator contentValidator;

    public WordPressArticleMapperResource(WordPressBlogPostContentMapper blogTransformer,
                                          WordPressLiveBlogContentMapper liveBlogTransformer,
                                          MessageProducingContentMapper contentMapper,
                                          NativeWordPressContentValidator contentValidator) {
        this.blogTransformer = blogTransformer;
        this.liveBlogTransformer = liveBlogTransformer;
        this.contentMapper = contentMapper;
        this.contentValidator = contentValidator;
    }

    @POST
    @Timed
    @Path("/map")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final WordPressContent map(NativeWordPressContent nativeWordPressContent, @Context HttpHeaders httpHeaders) {
        String transactionId = TransactionIdUtils.getTransactionIdOrDie(httpHeaders);
        try {
            contentValidator.validate(nativeWordPressContent);
            Post postDetails = nativeWordPressContent.getPost();
            return transformerFor(postDetails).mapWordPressArticle(transactionId, postDetails, new Date());
        } catch (PostNotFoundException e) {
            throw new ClientError.ClientErrorBuilder(SC_NOT_FOUND).error("Delete event").exception();
        } catch (IllegalArgumentException | WordPressContentException e) {
            throw new ClientError.ClientErrorBuilder(SC_UNPROCESSABLE_ENTITY).error("Wordpress content is not valid").exception(e);
        }
    }

    @POST
    @Timed
    @Path("/ingest")
    public Response ingest(NativeWordPressContent nativeWordPressContent, @Context HttpHeaders httpHeaders) {
        String transactionId = TransactionIdUtils.getTransactionIdOrDie(httpHeaders);
        try {
            contentValidator.validate(nativeWordPressContent);
            Post post = nativeWordPressContent.getPost();
            contentMapper.mapForPublish(transactionId, post, new Date());
        } catch (PostNotFoundException e) {
            Post post = nativeWordPressContent.getPost();
            contentMapper.mapForDelete(post.getUuid(), new Date(), transactionId);
        } catch (IllegalArgumentException | WordPressContentException e) {
            throw new ClientError.ClientErrorBuilder(SC_UNPROCESSABLE_ENTITY).error("Wordpress content is not valid").exception(e);
        }
        return Response.noContent().build();
    }

    private WordPressContentMapper<?> transformerFor(Post post) {
        WordPressPostType wordPressPostType = null;
        try {
            wordPressPostType = WordPressPostType.fromString(post.getType());
        } catch (IllegalArgumentException e) {/* ignore and throw as below */}

        if (wordPressPostType == null) {
            throw new ServerError.ServerErrorBuilder(SC_UNPROCESSABLE_ENTITY).error("unsupported blog post type").exception();
        }

        switch (wordPressPostType) {
            case POST:
                return blogTransformer;
            case MARKETS_LIVE:
            case LIVE_Q_AND_A:
            case LIVE_BLOG:
                return liveBlogTransformer;
            default:
                throw new ServerError.ServerErrorBuilder(SC_UNPROCESSABLE_ENTITY).error("unsupported blog post type").exception();
        }

    }
}
