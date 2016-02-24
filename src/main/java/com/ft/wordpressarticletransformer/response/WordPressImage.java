package com.ft.wordpressarticletransformer.response;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WordPressImage {
  private String url;
  private int width;
  private int height;
  
  @JsonProperty("url")
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getUrl() {
    return url;
  }
  
  @JsonProperty("width")
  public void setWidth(int width) {
    this.width = width;
  }
  
  public int getWidth() {
    return width;
  }
  
  @JsonProperty("height")
  public void setHeight(int height) {
    this.height = height;
  }
  
  public int getHeight() {
    return height;
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
