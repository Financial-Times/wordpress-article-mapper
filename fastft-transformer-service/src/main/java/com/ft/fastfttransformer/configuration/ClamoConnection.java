package com.ft.fastfttransformer.configuration;

import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class ClamoConnection {
	
	private final String hostName;
	private final String path;
	private final int port;
	private final JerseyClientConfiguration jerseyClientConfiguration;
	
	public ClamoConnection(@JsonProperty("hostName") String hostName,
			@JsonProperty("path") String path,
			@JsonProperty("port") int port,
			@JsonProperty("jerseyClient") JerseyClientConfiguration jerseyClientConfiguration) {
		super();
		this.hostName = hostName;
		this.path = path;
		this.port = port;
		this.jerseyClientConfiguration = jerseyClientConfiguration;
	}

	@NotNull
	public String getHostName() {
		return hostName;
	}

	@NotNull
	public String getPath() {
		return path;
	}

	@NotNull
	public int getPort() {
		return port;
	}

	@NotNull
	public JerseyClientConfiguration getJerseyClientConfiguration() {
		return jerseyClientConfiguration;
	}

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("hostName", hostName)
                .add("path", path)
                .add("port", port)
                .add("jerseyClient", jerseyClientConfiguration);
    }
    
    @Override
    public String toString() {
        return toStringHelper().toString();
    }

}
