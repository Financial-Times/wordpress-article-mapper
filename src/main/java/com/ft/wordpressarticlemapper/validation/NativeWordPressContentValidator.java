package com.ft.wordpressarticlemapper.validation;

import com.ft.wordpressarticlemapper.exception.InvalidStatusException;
import com.ft.wordpressarticlemapper.exception.PostNotFoundException;
import com.ft.wordpressarticlemapper.exception.UnexpectedErrorCodeException;
import com.ft.wordpressarticlemapper.exception.UnexpectedStatusFieldException;
import com.ft.wordpressarticlemapper.exception.UnpublishablePostException;
import com.ft.wordpressarticlemapper.exception.WordPressContentException;
import com.ft.wordpressarticlemapper.response.NativeWordPressContent;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.wordpressarticlemapper.response.WordPressPostType;
import com.ft.wordpressarticlemapper.response.WordPressStatus;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class NativeWordPressContentValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeWordPressContentValidator.class);

    private static final String UNSUPPORTED_POST_TYPE =
            "Not a valid post, type [%s] is not in supported types %s, for content with uuid:[%s]";

    private static final Set<String> SUPPORTED_POST_TYPES = WordPressPostType.stringValues();
    private static final String ERROR_NOT_FOUND = "Not found."; // DOES include a dot

    public void validate(NativeWordPressContent nativeWordPressContent) {
        if (nativeWordPressContent.getPost() == null) {
            throw new IllegalArgumentException("No content supplied");
        }

        final String status = nativeWordPressContent.getStatus();
        if (status == null) {
            throw new InvalidStatusException("Native WordPress content is not valid. Status is null");
        }

        final String uuid = nativeWordPressContent.getPost().getUuid();
        if (uuid != null && !UUID.fromString(uuid).toString().equals(uuid)) {
            throw new IllegalArgumentException("Invalid UUID supplied");
        }

        final WordPressStatus wordPressStatus;
        try {
            wordPressStatus = WordPressStatus.valueOf(status);
        } catch (IllegalArgumentException ignored) {
            throw new UnexpectedStatusFieldException(status, uuid);
        }
        switch (wordPressStatus) {
            case ok:
                if (nativeWordPressContent.getApiUrl() == null) {
                    throw new IllegalArgumentException("No apiUrl supplied");
                }

                if (!isSupportedPostType(nativeWordPressContent)) {
                    throw new UnpublishablePostException(uuid, String.format(UNSUPPORTED_POST_TYPE,
                            findTheType(nativeWordPressContent), SUPPORTED_POST_TYPES, uuid));
                }

                break;
            case error:
                throw processWordPressErrorResponse(uuid, nativeWordPressContent);

        }

    }

    private String findTheType(NativeWordPressContent nativeWordPressContent) {
        if (nativeWordPressContent.getPost() == null) {
            return null;
        }
        return nativeWordPressContent.getPost().getType();
    }

    private boolean isSupportedPostType(NativeWordPressContent nativeWordPressContent) {
        Post post = nativeWordPressContent.getPost();
        if (post != null) {
            LOGGER.info("post={}, type={}", post.getId(), post.getType());
            return SUPPORTED_POST_TYPES.contains(post.getType());
        } else {
            LOGGER.info("Post was null");
            return false;
        }
    }

    private WordPressContentException processWordPressErrorResponse(String uuid, NativeWordPressContent nativeWordPressContent) {
        String error = nativeWordPressContent.getError();
        if (ERROR_NOT_FOUND.equals(error)) {
            // wordpress delete request
            return new PostNotFoundException(uuid);
        }

        Post post = nativeWordPressContent.getPost();
        if ((post != null) && !isSupportedPostType(nativeWordPressContent)) {
            return new UnpublishablePostException(uuid,
                    String.format(UNSUPPORTED_POST_TYPE, post.getType(), SUPPORTED_POST_TYPES, uuid));
        }

        // It says it's an error, but we don't understand this kind of error
        return new UnexpectedErrorCodeException(error, uuid);
    }
}
