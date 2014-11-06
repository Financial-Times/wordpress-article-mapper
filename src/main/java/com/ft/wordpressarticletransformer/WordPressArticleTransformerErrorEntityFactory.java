package com.ft.wordpressarticletransformer;

import java.util.UUID;

import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.api.jaxrs.errors.entities.ErrorEntityFactory;

public class WordPressArticleTransformerErrorEntityFactory implements ErrorEntityFactory {


    public WordPressArticleTransformerErrorEntityFactory() {
    }

    @Override
    public ErrorEntity entity(String message, Object context) {

        if(context instanceof UUID) {
            UUID uuid = (UUID) context;
            return new IdentifiableErrorEntity(uuid, message);
        }

        // fall back to default format
        return new ErrorEntity(message);
    }
}
