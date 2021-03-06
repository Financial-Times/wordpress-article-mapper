package com.ft.wordpressarticlemapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class Comments {
  private boolean enabled;

  public Comments(@JsonProperty("enabled") boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Comments that = (Comments) o;

    return Objects.equal(this.enabled, that.enabled);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(enabled);
  }
}
