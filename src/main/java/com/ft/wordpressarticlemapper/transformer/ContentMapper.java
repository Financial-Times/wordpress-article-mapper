package com.ft.wordpressarticlemapper.transformer;

import com.ft.wordpressarticlemapper.model.WordPressContent;
import com.ft.wordpressarticlemapper.response.Post;

import java.net.URI;
import java.util.Date;
import java.util.UUID;

public interface ContentMapper {

    public WordPressContent mapWordPressArticle(String transactionId, URI requestUri, Post post, Date lastModified);
}
