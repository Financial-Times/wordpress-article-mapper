package com.ft.wordpressarticletransformer.service;

import com.ft.api.jaxrs.client.exceptions.ApiNetworkingException;
import com.ft.api.jaxrs.client.exceptions.RemoteApiException;
import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.jerseyhttpwrapper.config.EndpointConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;

public class NativeReaderClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeReaderClient.class);
    private static final String GET = "GET";

    private final Client jerseyClient;
    private final String apiHost;
    private final int apiPort;
    private final String apiPath;

    public NativeReaderClient(final Client jerseyClient, final EndpointConfiguration nativeReaderConfiguration) {
        this.jerseyClient = jerseyClient;
        this.apiHost = nativeReaderConfiguration.getHost();
        this.apiPort = nativeReaderConfiguration.getPort();
        this.apiPath = nativeReaderConfiguration.getPath();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getWordpressContent(final String uuid, final String transactionId) {
        final URI fileByUuidUri = requestUrlBuilder().build(uuid);
        LOGGER.debug("Making GET request to Native Reader on URI: {}", fileByUuidUri);
        ClientResponse clientResponse = null;
        try {
            clientResponse = httpCall(fileByUuidUri, transactionId);
            handleNonOkStatus(clientResponse, fileByUuidUri);

        return clientResponse.getEntity(Map.class);
        }
        finally {
            if (clientResponse != null) {
                try {
                    clientResponse.getEntityInputStream().close();
                } catch (IOException e) {
                    LOGGER.debug("Error occurred while trying to prevent connections from staying open. Could not close response stream.", e);
                }
            }
        }
    }

    /**
     * It looks like build(...) isn't safe for concurrent use
     * so this method can be used to create fresh instances for
     * use in a single thread.
     */
    private UriBuilder requestUrlBuilder() {
        return UriBuilder.fromPath(apiPath)
                .scheme("http")
                .host(apiHost)
                .port(apiPort);
    }

    protected ClientResponse httpCall(final URI fileByUuidUri, final String transactionId) {
        try {
            return jerseyClient
                    .resource(fileByUuidUri)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .header(TRANSACTION_ID_HEADER, transactionId)
                    .header("Host", "nativerw")
                    .get(ClientResponse.class);
        } catch (final ClientHandlerException clientHandlerException) {
            if (clientHandlerException.getCause() instanceof IOException) {
                throw new ApiNetworkingException(fileByUuidUri, GET, clientHandlerException);
            } else {
                throw clientHandlerException;
            }
        }
    }

    protected void handleNonOkStatus(final ClientResponse clientResponse, final URI fileByUuidUri) {
        if (Response.Status.OK.getStatusCode() == clientResponse.getStatus()) {
            return;
        }
        final int status = clientResponse.getStatus();
        ErrorEntity errorEntity = null;
        try {
            errorEntity = clientResponse.getEntity(ErrorEntity.class);
        } catch (ClientHandlerException ex) {
            LOGGER.warn("Failed to parse ErrorEntity when handling API transaction failure.", ex);
        }
        throw new RemoteApiException(fileByUuidUri, GET, status, errorEntity);
    }
}
