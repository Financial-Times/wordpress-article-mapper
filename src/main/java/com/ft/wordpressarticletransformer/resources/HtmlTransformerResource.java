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

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;


@Path("/content-transformer")
public class HtmlTransformerResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlTransformerResource.class);

    private BodyProcessingFieldTransformer bodyProcessingFieldTransformer;

    public HtmlTransformerResource(BodyProcessingFieldTransformer bodyProcessingFieldTransformer,
                                   BrandSystemResolver brandSystemResolver) {
        this.bodyProcessingFieldTransformer = bodyProcessingFieldTransformer;
    }

    @POST
    @Timed
    @Consumes({MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
    @Produces({MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
    public final String transformHtml(String body, @HeaderParam("transaction_id") String txId) {
        String result = bodyProcessingFieldTransformer.transform(body, txId);
        return result;
    }

}
