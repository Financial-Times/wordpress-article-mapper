package com.ft.wordpressarticletransformer;

import java.util.HashMap;
import java.util.Map;

public class WordPressArticleTransformerComponentTestConfigHelper {
    public static final String CONFIG_FILE = "config-component-tests.yml";

    public static void setUp() {
        setUp(null);
    }
    
    public static void setUp(Map<String,Object> additionalConfig) {
        Map<String,Object> hieraData = new HashMap<>();
        hieraData.put("credentialsFile", "junit-credentials.properties");
        hieraData.put("httpPort", "14180");
        hieraData.put("adminPort", "14181");
        hieraData.put("hostname_1", "localhost");
        hieraData.put("port_1", "15670");
        hieraData.put("path_1", "/api/get_recent_posts/");
        hieraData.put("hostname_2", "localhost");
        hieraData.put("port_2", "15670");
        hieraData.put("path_2", "/api/get_recent_posts/");
        hieraData.put("jerseyClientTimeout", "200ms");
        if (additionalConfig != null) {
            hieraData.putAll(additionalConfig);
        }
        
        try {
            MustacheTemplatingHelper.generateConfigFile("config.yaml.mustache",
                    hieraData, CONFIG_FILE);
        }
        catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
