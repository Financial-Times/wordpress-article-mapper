package com.ft.wordpressarticlemapper.resources;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.wordpressarticlemapper.exception.WordPressContentException;
import com.ft.wordpressarticlemapper.model.WordPressContent;
import com.ft.wordpressarticlemapper.response.NativeWordPressContent;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.wordpressarticlemapper.response.WordPressPostType;
import com.ft.wordpressarticlemapper.transformer.BodyProcessingFieldTransformer;
import com.ft.wordpressarticlemapper.transformer.WordPressBlogPostContentMapper;
import com.ft.wordpressarticlemapper.transformer.WordPressContentMapper;
import com.ft.wordpressarticlemapper.transformer.WordPressLiveBlogContentMapper;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;


@Path("/")
public class WordPressArticleMapperResource {

    private static final String CHARSET_UTF_8 = ";charset=utf-8";
    private static final String STATUS_ERROR = "error";
    private static final String ERROR_NOT_FOUND = "Not found";

    private final WordPressBlogPostContentMapper blogTransformer;
    private final WordPressLiveBlogContentMapper liveBlogTransformer;

    public WordPressArticleMapperResource(BodyProcessingFieldTransformer bodyProcessingFieldTransformer,
                                          BrandSystemResolver brandSystemResolver,
                                          IdentifierBuilder identifierBuilder) {
        this.blogTransformer = new WordPressBlogPostContentMapper(brandSystemResolver, bodyProcessingFieldTransformer, identifierBuilder);
        this.liveBlogTransformer = new WordPressLiveBlogContentMapper(brandSystemResolver, identifierBuilder);
    }

    @POST
    @Timed
    @Path("/map")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final WordPressContent map(NativeWordPressContent nativeWordPressContent, @Context HttpHeaders httpHeaders) {
        String transactionId = TransactionIdUtils.getTransactionIdOrDie(httpHeaders);
        try {
            Post post = nativeWordPressContent.getPost();
            if (post == null) {
                throw new IllegalArgumentException("No post supplied");
            }
            if (isDeleteEvent(nativeWordPressContent)){
                throw new ClientError.ClientErrorBuilder(SC_NOT_FOUND).error("Delete event").exception();
            }
            return getWordPressContent(nativeWordPressContent, transactionId);
        } catch (IllegalArgumentException | WordPressContentException e) {
            throw new ClientError.ClientErrorBuilder(SC_UNPROCESSABLE_ENTITY).error("Wordpress content is not valid").exception(e);
        }
    }

    private boolean isDeleteEvent(NativeWordPressContent nativeWordPressContent) {
        String status = nativeWordPressContent.getStatus();
        String error = nativeWordPressContent.getError();
        return STATUS_ERROR.equals(status) && ERROR_NOT_FOUND.equals(error);
    }

    private WordPressContent getWordPressContent(NativeWordPressContent nativeWordPressContent, String transactionId) {
        validateContent(nativeWordPressContent);
        Post postDetails = nativeWordPressContent.getPost();
        return transformerFor(postDetails).mapWordPressArticle(transactionId, postDetails, nativeWordPressContent.getLastModified());
    }

    private void validateContent(NativeWordPressContent nativeWordPressContent) {
        Post post = nativeWordPressContent.getPost();

        if (nativeWordPressContent.getApiUrl() == null) {
            throw new IllegalArgumentException("No apiUrl supplied");
        }

        String postUrl = post.getUrl();
        if (postUrl == null) {
            throw new IllegalArgumentException("No post Url supplied");
        }
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
