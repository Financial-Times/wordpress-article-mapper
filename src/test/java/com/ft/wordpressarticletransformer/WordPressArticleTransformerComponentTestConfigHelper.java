package com.ft.wordpressarticletransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        List<Map<String,Object>> sites = new ArrayList<>();
        Map<String,Object> site = new HashMap<>();
        site.put("hostname", "localhost");
        site.put("port", "15670");
        site.put("path", "/api/get_recent_posts/");
        sites.add(site);
        hieraData.put("healthCheckSites", sites);
        
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
