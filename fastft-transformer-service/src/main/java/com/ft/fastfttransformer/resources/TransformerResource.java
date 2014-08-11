package com.ft.fastfttransformer.resources;

import java.net.URI;
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
import com.ft.fastfttransformer.response.FastFTResponse;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.client.Client;
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
				.withSource("FT").withByline("By FastFT")//TODO - make byline optional in writer/find a good alternative byline
				.withUuid(uuid).build();

	}

	private String tidiedUpBody(String body) {
		// TODO - temporary fix?? use to tidy up problem characters
		return body.replaceAll("&", "&amp;");
	}

	private String transformBody(String originalBody) {
		return "<body>" + originalBody + "</body>";
	}

	private Map<String, Object> doRequest(Integer postId) {

		// FIXME: build this properly.
		// NB: the string below is the equivalent of this:
		// [{"arguments": {"outputfields": {"title": true,"content" : "text"},"id": <postID>},"action": "getPost" }]
		String eq = "%5B%7B%22arguments%22%3A%20%7B%22outputfields%22%3A%20%7B%22title%22%3A%20true%2C%22content%22%20%3A%20%22text%22%7D%2C%22id%22%3A%20"
				+ Integer.toString(postId)
				+ "%7D%2C%22action%22%3A%20%22getPost%22%20%7D%5D%0A";

		WebResource webResource = client.resource(getClamoBaseUrl(postId));

		ClientResponse response = webResource.queryParam("request", eq)
				.accept("application/json").get(ClientResponse.class);

		int responseStatusCode = response.getStatus();
		int responseStatusFamily = responseStatusCode / 100;

		if (responseStatusFamily == 2) {
			FastFTResponse[] output = response.getEntity(FastFTResponse[].class);

			if (okReturned(output)) {
				return output[0].getData().getAdditionalProperties();
			} else  if (errorReturned(output)) {
				if (CLAMO_RECORD_NOT_FOUND.equals(title(output))) {
					throw ClientError.status(404).exception();
				} else {
					// It says it's an error, but we do not understand this kind of error.
					throw ServerError.status(500).error(title(output)).exception();
				}
			} else {
				// We do not understand this response.
				throw ServerError.status(500).error(
						String.format("Invalid response received from Clamo, title [%s], output.length [%d]", title(output), output.length)
				).exception();
			}

		} else if (responseStatusFamily == 4) {
			// We do not expect this behaviour, we always expect a 200, and an 'error' status in the title.
			// If 404 is returned, then either the behaviour of Clamo has changed, or it isn't deployed correctly.
			LOGGER.error("Unexpected status returned by Clamo: [{}].", responseStatusCode);
			throw ServerError.status(503).exception();
		} else {
			throw ServerError.status(responseStatusCode).exception();
		}
	}

	private boolean okReturned(FastFTResponse[] output) {
		return output.length > 0 && output[0].getStatus().equals(CLAMO_OK);
	}

	private boolean errorReturned(FastFTResponse[] output) {
		return output.length > 0 && output[0].getStatus().equals(CLAMO_ERROR);
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
                .path("{uuid}")
                .scheme("http")
                .host(clamoConnection.getHostName())
                .port(clamoConnection.getPort())
                .build(id);
	}

}
