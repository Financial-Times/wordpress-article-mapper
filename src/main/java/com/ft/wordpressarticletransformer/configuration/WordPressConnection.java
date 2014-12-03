package com.ft.wordpressarticletransformer.configuration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import io.dropwizard.validation.PortRange;

public class WordPressConnection {
	
	private final String hostName;
	private final String path;
	private final int port;
	
	public WordPressConnection(@JsonProperty("hostName") String hostName,
							   @JsonProperty("path") String path,
							   @JsonProperty("port") int port) {
		super();
		this.hostName = hostName;
		this.path = path;
		this.port = port;
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

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("hostName", hostName)
                .add("path", path)
                .add("port", port);
    }
    
    @Override
    public String toString() {
        return toStringHelper().toString();
    }

}
