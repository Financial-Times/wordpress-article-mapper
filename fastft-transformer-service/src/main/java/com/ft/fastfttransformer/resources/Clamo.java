package com.ft.fastfttransformer.resources;

/**
 * Clamo
 *
 * @author Simon.Gibbs
 */
public class Clamo {

    public static final String CLAMO_QUERY_JSON_STRING = "[{\"arguments\":{\"outputfields\":{\"title\":true,\"content\":\"text\"},\"id\":<postId>},\"action\":\"getPost\"}]";

    public static String buildPostRequest(int postId) {
        return CLAMO_QUERY_JSON_STRING.replace("<postId>", Integer.toString(postId));
    }
}
