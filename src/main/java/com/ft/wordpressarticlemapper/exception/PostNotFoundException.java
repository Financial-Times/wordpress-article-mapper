package com.ft.wordpressarticlemapper.exception;

import java.time.OffsetDateTime;

public class PostNotFoundException extends WordPressContentException {

  private final String uuid;

  private OffsetDateTime lastModified;

  public PostNotFoundException(String uuid) {
    this(uuid, null);
  }

  public PostNotFoundException(String uuid, OffsetDateTime lastModified) {
    super(String.format("Error. Content with uuid: [%s] not found", uuid));
    this.uuid = uuid;
    this.lastModified = lastModified;
  }

  public String getUuid() {
    return uuid;
  }

  public OffsetDateTime getLastModified() {
    return lastModified;
  }
}
