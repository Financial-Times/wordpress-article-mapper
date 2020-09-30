package com.ft.wordpressarticlemapper;

import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.api.jaxrs.errors.entities.ErrorEntityFactory;
import java.util.UUID;

public class WordPressArticleMapperErrorEntityFactory implements ErrorEntityFactory {

  public WordPressArticleMapperErrorEntityFactory() {}

  @Override
  public ErrorEntity entity(String message, Object context) {

    if (context instanceof UUID) {
      UUID uuid = (UUID) context;
      return new IdentifiableErrorEntity(uuid, message);
    }

    // fall back to default format
    return new ErrorEntity(message);
  }
}
