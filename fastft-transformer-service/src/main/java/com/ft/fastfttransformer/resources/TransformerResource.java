package com.ft.fastfttransformer.resources;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.content.model.Content;
import com.ft.fastfttransformer.configuration.ClamoConnection;
import com.ft.fastfttransformer.response.Data;
import com.ft.fastfttransformer.response.FastFTResponse;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/content")
public class TransformerResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransformerResource.class);

    private static final String CHARSET_UTF_8 = ";charset=utf-8";

	private static final String CLAMO_OK = "ok";
	private static final String CLAMO_ERROR = "error";
	private static final String CLAMO_FIELD_TITLE = "title";

	private static final String CLAMO_RECORD_NOT_FOUND = "Record not found";
	private final Client client;
	private final ClamoConnection clamoConnection;

	public TransformerResource(Client client, ClamoConnection clamoConnection) {
		this.client = client;
		this.clamoConnection = clamoConnection;
	}

	@GET
	@Timed
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
	public final Content getByUuid(@PathParam("id") Integer postId) {

		Map<String, Object> result = doRequest(postId);

		if (result == null) {
			throw new NotFoundException();
		}

		String title = result.get(CLAMO_FIELD_TITLE).toString();
		String body = transformBody(result.get("content").toString());
		UUID uuid = UUID.fromString(result.get("uuidv3").toString());
		Date datePublished = new Date(1000 * Long.parseLong(result.get(
				"datepublished").toString()));

		LOGGER.info("Returning content for [{}] with uuid [{}].", postId, uuid);

		return Content.builder().withTitle(title)
				.withPublishedDate(datePublished)
				.withXmlBody(tidiedUpBody(body))
				.withSource("FT")
				.withUuid(uuid).build();

	}

	private String tidiedUpBody(String body) {
		// TODO - temporary fix until business rules are defined for Body processing
		return body.replaceAll("&", "&amp;");
	}

	private String transformBody(String originalBody) {
		return "<body>" + originalBody + "</body>";
	}

	private Map<String, Object> doRequest(Integer postId) {
        String eq;
        try {
            String queryStringValue = Clamo.buildPostRequest(postId);
            eq = URLEncoder.encode(queryStringValue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // should never happen, UTF-8 is part of the Java spec
            throw ServerError.status(503).error("JVM Capability missing: UTF-8 encoding").exception();
        }

		URI fastFtContentByIdUri = getClamoBaseUrl(postId);
		WebResource webResource = client.resource(fastFtContentByIdUri);

		ClientResponse response;
		try {
			response = webResource.queryParam("request", eq)
					.accept("application/json").get(ClientResponse.class);
		} catch (ClientHandlerException che) {
			Throwable cause = che.getCause();
			if(cause instanceof IOException) {
				throw ServerError.status(503).context(webResource).error(
						String.format("Cannot connect to Clamo for url: [%s]", fastFtContentByIdUri)).exception(cause);
			}
			throw che;
		}

		int responseStatusCode = response.getStatus();
		int responseStatusFamily = responseStatusCode / 100;

		if (responseStatusFamily == 2) {
			FastFTResponse[] output = response.getEntity(FastFTResponse[].class);

			// Status can be "ok" or "error".
			if (statusIsOk(output)) {
				Data data = output[0].getData();
				if (data == null) {
					LOGGER.error("Data node is missing from return JSON for ID [{}]", postId);
					return null;
				}
				if (data.getAdditionalProperties().size() == 0) {
					LOGGER.error("Data node is empty in return JSON for ID [{}]", postId);
					return null;
				}
				return data.getAdditionalProperties();
			} else  if (statusIsError(output)) {
				// Title specifies what is wrong exactly.
				if (titleIsRecordNotFound(output)) {
					throw ClientError.status(404).exception();
				} else {
					// It says it's an error, but from the title we do not understand this kind of error.
					throw ServerError.status(500).error(
							String.format("Unexpected title returned by Clamo: [%s] for ID [%d].", title(output), postId)).exception();
				}
			} else {
				// We do not understand this status.
				throw ServerError.status(500).error(
						String.format("Unexpected status (status field; not HTTP status) returned by Clamo, status [%s], title [%s], output.length [%d] for ID [%d].",
								status(output), title(output), output.length, postId)
				).exception();
			}

		} else if (responseStatusFamily == 4) {
			// We do not expect this behaviour, we always expect a 200, and an 'error' status with more info in the title.
			// If 404 is returned, then either the behaviour of Clamo has changed, or it isn't deployed correctly.
			throw ServerError.status(503).error(
					String.format("Unexpected HTTP status returned by Clamo: [%d] for ID [%d].", responseStatusCode, postId)).exception();
		} else {
			throw ServerError.status(responseStatusCode).exception();
		}
	}

	private boolean statusIsOk(FastFTResponse[] output) {
		return CLAMO_OK.equals(status(output));
	}

	private boolean statusIsError(FastFTResponse[] output) {
		return CLAMO_ERROR.equals(status(output));
	}

	private String status(FastFTResponse[] output) {
		if (output.length > 0) {
			return output[0].getStatus();
		} else {
			return null;
		}
	}

	private boolean titleIsRecordNotFound(FastFTResponse[] output) {
		return CLAMO_RECORD_NOT_FOUND.equals(title(output));
	}

	private String title(FastFTResponse[] output) {
		if (output.length > 0 && output[0].getAdditionalProperties().get(CLAMO_FIELD_TITLE) != null) {
			return output[0].getAdditionalProperties().get(CLAMO_FIELD_TITLE).toString();
		} else {
			return null;
		}
	}

	private URI getClamoBaseUrl(int id) {
		return UriBuilder.fromPath(clamoConnection.getPath())
                .scheme("http")
                .host(clamoConnection.getHostName())
                .port(clamoConnection.getPort())
                .build(id);
	}

}
