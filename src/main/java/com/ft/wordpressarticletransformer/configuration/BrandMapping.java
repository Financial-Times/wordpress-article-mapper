package com.ft.wordpressarticletransformer.configuration;

import java.net.URI;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BrandMapping {
  private final Pattern pattern;
  private final URI brand;
  
  public BrandMapping(@JsonProperty("pattern") Pattern pattern, @JsonProperty("brand") URI brand) {
    this.pattern = pattern;
    this.brand = brand;
  }
  
  public Pattern getPattern() {
    return pattern;
  }
  
  public URI getBrand() {
    return brand;
  }
}
