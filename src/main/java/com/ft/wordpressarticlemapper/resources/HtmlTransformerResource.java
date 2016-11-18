package com.ft.wordpressarticlemapper.resources;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.wordpressarticlemapper.transformer.BodyProcessingFieldTransformer;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.UUID;


@Path("/transform-html-fragment")
public class HtmlTransformerResource {

    private BodyProcessingFieldTransformer bodyProcessingFieldTransformer;

    public HtmlTransformerResource(BodyProcessingFieldTransformer bodyProcessingFieldTransformer) {
        this.bodyProcessingFieldTransformer = bodyProcessingFieldTransformer;
    }

    @POST
    @Timed
    @Consumes({MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_HTML})
    public final String transformHtml(String body, @Context HttpHeaders httpHeaders) {
        String transactionId = TransactionIdUtils.getTransactionIdOrDie(httpHeaders, UUID.randomUUID(), "Transform request");
        return bodyProcessingFieldTransformer.transform(body, transactionId);
    }

}
