package com.ft.fastfttransformer.resources;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.StringEscapeUtils;

import com.codahale.metrics.annotation.Timed;
import com.ft.contentstoreapi.model.Content;
import com.ft.fastfttransformer.configuration.ClamoConnection;
import com.ft.fastfttransformer.response.FastFTResponse;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jndi.toolkit.url.Uri;

@Path("/content")
public class TransformerResource {

	private static final String CHARSET_UTF_8 = ";charset=utf-8";

	private final ClamoConnection clamoConnection;

	public TransformerResource(ClamoConnection clamoConnection) {
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

		String title = result.get("title").toString();
		String body = transformBody(result.get("content").toString());
		UUID uuid = UUID.fromString(result.get("uuidv3").toString());
		Date datePublished = new Date(1000 * Long.parseLong(result.get(
				"datepublished").toString()));

		return Content.builder().withHeadline(title)
				.withLastPublicationDate(datePublished)
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

		Client client = Client.create();

		// FIXME: build this properly.
		String eq = "%5B%7B%22arguments%22%3A%20%7B%22outputfields%22%3A%20%7B%22title%22%3A%20true%2C%22content%22%20%3A%20%22text%22%7D%2C%22id%22%3A%20"
				+ Integer.toString(postId)
				+ "%7D%2C%22action%22%3A%20%22getPost%22%20%7D%5D%0A";

		WebResource webResource = client.resource(getClamoBaseUrl(postId));

		ClientResponse response = webResource.queryParam("request", eq)
				.accept("application/json").get(ClientResponse.class);

		if (response.getStatus() != 200) {
			// FIXME: handle this better.
			throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatus());
		}

		FastFTResponse[] output = response.getEntity(FastFTResponse[].class);

		if (output.length != 1 || !output[0].getStatus().equals("ok")) {
			return null;
		}

		return output[0].getData().getAdditionalProperties();

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
