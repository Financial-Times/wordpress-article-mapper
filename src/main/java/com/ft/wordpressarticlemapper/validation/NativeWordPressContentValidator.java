package com.ft.wordpressarticlemapper.validation;

import com.ft.wordpressarticlemapper.exception.InvalidResponseException;
import com.ft.wordpressarticlemapper.exception.PostNotFoundException;
import com.ft.wordpressarticlemapper.exception.UnexpectedErrorCodeException;
import com.ft.wordpressarticlemapper.exception.UnexpectedStatusFieldException;
import com.ft.wordpressarticlemapper.exception.UnpublishablePostException;
import com.ft.wordpressarticlemapper.exception.WordPressContentException;
import com.ft.wordpressarticlemapper.response.NativeWordPressContent;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.wordpressarticlemapper.response.WordPressPostType;
import com.ft.wordpressarticlemapper.response.WordPressStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;

public class NativeWordPressContentValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeWordPressContentValidator.class);

    private static final String UNSUPPORTED_POST_TYPE =
            "Not a valid post, type [%s] is not in supported types %s, for content with uuid:[%s]";

    private static final Set<String> SUPPORTED_POST_TYPES = WordPressPostType.stringValues();
    private static final String ERROR_NOT_FOUND = "Not found."; // DOES include a dot

    public void validateWordPressContent(NativeWordPressContent nativeWordPressContent) {

        if (nativeWordPressContent.getPost() == null) {
            throw new IllegalArgumentException("No content supplied");
        }

        if (nativeWordPressContent.getApiUrl() == null) {
            throw new IllegalArgumentException("No apiUrl supplied");
        }

        String status = nativeWordPressContent.getStatus();
        if (status == null) {
            throw new InvalidResponseException("Response not a valid NativeWordPressContent. Response status is null");
        }
        String uuid =  nativeWordPressContent.getPost().getUuid();
        WordPressStatus wordPressStatus;
        try {
            wordPressStatus = WordPressStatus.valueOf(status);
        } catch (IllegalArgumentException ignored) {
            throw new UnexpectedStatusFieldException(status, uuid);
        }
        switch (wordPressStatus) {
            case ok:
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
            return new PostNotFoundException(
                    uuid,
                    OffsetDateTime.of(
                            LocalDateTime.ofInstant(nativeWordPressContent.getLastModified().toInstant(), ZoneId.of(ZoneOffset.UTC.getId())),
                            ZoneOffset.UTC
                    )
            );
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
