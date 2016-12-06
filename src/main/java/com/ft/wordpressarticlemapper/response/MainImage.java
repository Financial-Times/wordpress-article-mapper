package com.ft.wordpressarticlemapper.response;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MainImage {
  private String id;
  private String title;
  private String description;
  private String url;
  private String mediaType;
  
  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }
  
  public String getId() {
    return id;
  }
  
  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getTitle() {
    return title;
  }
  
  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }
  
  @JsonProperty("url")
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getUrl() {
    return url;
  }
  
  @JsonProperty("mime_type")
  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }
  
  public String getMediaType() {
    return mediaType;
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
