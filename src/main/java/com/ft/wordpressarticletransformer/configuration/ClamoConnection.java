package com.ft.wordpressarticletransformer.configuration;

import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import io.dropwizard.validation.PortRange;

public class ClamoConnection {
	
	private final String hostName;
	private final String path;
	private final int port;
	private final JerseyClientConfiguration jerseyClientConfiguration;
	private final int numberOfConnectionAttempts;
	
	public ClamoConnection(@JsonProperty("hostName") String hostName,
			@JsonProperty("path") String path,
			@JsonProperty("port") int port,
			@JsonProperty("jerseyClient") JerseyClientConfiguration jerseyClientConfiguration, 
			@JsonProperty("numberOfConnectionAttempts") int numberOfConnectionAttempts) {
		super();
		this.hostName = hostName;
		this.path = path;
		this.port = port;
		this.jerseyClientConfiguration = jerseyClientConfiguration;
		this.numberOfConnectionAttempts = numberOfConnectionAttempts;
	}

	@NotNull
	public String getHostName() {
		return hostName;
	}

	@NotNull
	public String getPath() {
		return path;
	}

	@NotNull @PortRange
	public int getPort() {
		return port;
	}

	@NotNull
	public JerseyClientConfiguration getJerseyClientConfiguration() {
		return jerseyClientConfiguration;
	}
	
	@NotNull
	public int getNumberOfConnectionAttempts() {
	    return numberOfConnectionAttempts;
	}

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("hostName", hostName)
                .add("path", path)
                .add("port", port)
                .add("jerseyClient", jerseyClientConfiguration)
                .add("numberOfConnectionAttempts", numberOfConnectionAttempts);
    }
    
    @Override
    public String toString() {
        return toStringHelper().toString();
    }

}
