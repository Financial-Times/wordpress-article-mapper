package com.ft.wordpressarticlemapper.response;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MainImage {
  private String id;
  private String title;
  private String description;
  private String url;
  private String mediaType;
  private Map<String,WordPressImage> images = new HashMap<>();
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();
  
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
  
  @JsonProperty("images")
  public void setImages(Map<String,WordPressImage> images) {
    this.images = images;
  }
  
  public Map<String,WordPressImage> getImages() {
    return images;
  }
  
  @JsonIgnore
  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
      return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
      this.additionalProperties.put(name, value);
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
