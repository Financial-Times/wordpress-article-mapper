package com.ft.wordpressarticletransformer.service;

import com.ft.wordpressarticletransformer.exception.InvalidResponseException;
import com.ft.wordpressarticletransformer.exception.UnexpectedStatusFieldException;
import com.ft.wordpressarticletransformer.exception.UnpublishablePostException;
import com.ft.wordpressarticletransformer.response.Post;
import com.ft.wordpressarticletransformer.response.WordPressPostType;
import com.ft.wordpressarticletransformer.response.WordPressResponse;
import com.ft.wordpressarticletransformer.response.WordPressStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class WordpressResponseValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(WordpressContentSourceService.class);

    private static final String UNSUPPORTED_POST_TYPE =
            "Not a valid post, type [%s] is not in supported types %s, for content with uuid:[%s]";

    private static final Set<String> SUPPORTED_POST_TYPES = WordPressPostType.stringValues();

    public void validateWordpressResponse(WordPressResponse wordPressResponse, String uuid) {

        String status = wordPressResponse.getStatus();
        if (status == null) {
            throw new InvalidResponseException("Response not a valid WordPressResponse. Response status is null");
        }
        WordPressStatus wordPressStatus = WordPressStatus.valueOf(status);

        if (wordPressStatus == WordPressStatus.ok) {
            if (!isSupportedPostType(wordPressResponse)) {
                throw new UnpublishablePostException(uuid, String.format(UNSUPPORTED_POST_TYPE,
                        findTheType(wordPressResponse), SUPPORTED_POST_TYPES, uuid));
            }

        } else {
            throw new UnexpectedStatusFieldException(status, uuid);
        }

    }

    private String findTheType(WordPressResponse wordPressResponse) {
        if (wordPressResponse.getPost() == null) {
            return null;
        }
        return wordPressResponse.getPost().getType();
    }

    private boolean isSupportedPostType(WordPressResponse wordPressResponse) {
        Post post = wordPressResponse.getPost();
        if (post != null) {
            LOGGER.info("post={}, type={}", post.getId(), post.getType());
            return SUPPORTED_POST_TYPES.contains(post.getType());
        } else {
            LOGGER.info("Post was null");
            return false;
        }
    }
}
