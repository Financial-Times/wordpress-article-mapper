package com.ft.wordpressarticletransformer.resources;

import com.codahale.metrics.annotation.Timed;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


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
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_HTML})
    public final String transformHtml(String body, @HeaderParam("transaction_id") String txId) {
        String result = bodyProcessingFieldTransformer.transform(body, txId);
        return result;
    }

}
