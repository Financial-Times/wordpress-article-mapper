package com.ft.wordpressarticlemapper.resources;

import com.ft.wordpressarticlemapper.WordPressArticleMapperApplication;
import com.ft.wordpressarticlemapper.configuration.WordPressArticleTransformerConfiguration;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import io.dropwizard.testing.junit.DropwizardAppRule;

import static com.ft.wordpressarticlemapper.util.TestFileUtil.resourceFilePath;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_TEMPORARILY;
import static org.junit.Assert.fail;

public class WordPressArticleTransformerAppRule
        implements TestRule {

    public static int findAvailableWireMockPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("unable to find an available port", e);
        }
    }

    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NO_HTML_NUMBER_ENTITY = "5c652c7e-c81e-4be7-8669-adeb5a5621da";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NO_HTML_NAME_ENTITY = "5c652c7e-c81e-4be7-8669-adeb5a5621dc";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS = "5c652c7e-c81e-4be7-8669-adeb5a5621dd";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_ERROR_NOT_FOUND = "5c652c7e-c81e-4be7-8669-adeb5a5621de";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_ERROR_UNKNOWN = "5c652c7e-c81e-4be7-8669-adeb5a5621d6";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_STATUS_UNKNOWN = "5c652c7e-c81e-4be7-8669-adeb5a5621d7";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_404 = "5c652c7e-c81e-4be7-8669-adeb5a5621df";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_INVALID_CONTENT_TYPE = "5c652c7e-c81e-4be7-8669-adeb5a5621d0";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NOT_TYPE_POST = "5c652c7e-c81e-4be7-8669-adeb5a5621d1";
    protected static final String UUID_MAP_TO_NO_REQUEST_TO_WORD_PRESS_EXPECTED = "ABC-1234";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_NON_WORD_PRESS_RESPONSE = "5c652c7e-c81e-4be7-8669-adeb5a5621d2";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_500 = "5c652c7e-c81e-4be7-8669-adeb5a5621d3";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_CANNOT_CONNECT = "5c652c7e-c81e-4be7-8669-adeb5a5621d4";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_502 = "5c652c7e-c81e-4be7-8669-adeb5a5621d5";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORDPRESS_NO_APIURL_ON_RESPONSE = "5c652c7e-c81e-4be7-8669-adeb5a5621db";
    protected static final String UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_ONLY_TABLES = "9425e94b-273e-459c-94cc-640c0b992d85";

    private final DropwizardAppRule<WordPressArticleTransformerConfiguration> appRule;

    private final RuleChain ruleChain;

    private WireMockRule nativeStoreWireMockRule;
    private WireMockRule documentStoreWireMockRule;

    private String contentReadOutputTemplate;

    public WordPressArticleTransformerAppRule(String configurationPath, int nativeRWPort, int documentStorePort) {
        appRule = new DropwizardAppRule<>(WordPressArticleMapperApplication.class, configurationPath);

        nativeStoreWireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
                .withRootDirectory("src/test/resources/wordPress")
                .port(nativeRWPort)
        );

        documentStoreWireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
                .port(documentStorePort)
        );

        ruleChain = RuleChain
                .outerRule(nativeStoreWireMockRule)
                .around(documentStoreWireMockRule)
                .around(appRule);

        try {
            contentReadOutputTemplate = Files.toString(new File(resourceFilePath("content-read-output-template.json")), Charsets.UTF_8);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        final Statement stmt = ruleChain.apply(base, description);

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    stmt.evaluate();
                } finally {
                    nativeStoreWireMockRule.shutdown();
                    documentStoreWireMockRule.shutdown();
                }
            }
        };
    }

    public int getWordPressArticleTransformerLocalPort() {
        return appRule.getLocalPort();
    }

    public void mockContentReadResponse(String uuid, int status) {
        MappingBuilder request = get(urlPathEqualTo("/content-read/" + uuid));
        String contentReadOutput = String.format(contentReadOutputTemplate, uuid, uuid, uuid);
        ResponseDefinitionBuilder response = aResponse().withStatus(status).withBody(contentReadOutput);

        documentStoreWireMockRule.stubFor(request.willReturn(response));
    }

    public void mockDocumentStoreQueryResponse(String authority, String identifierValue, int status, String location) {
        MappingBuilder request = head(urlPathEqualTo("/content-query"))
                .withQueryParam("identifierAuthority", equalTo(authority))
                .withQueryParam("identifierValue", equalTo(identifierValue));

        ResponseDefinitionBuilder response = aResponse().withStatus(status);
        if ((status == SC_MOVED_PERMANENTLY) || (status == SC_MOVED_TEMPORARILY)) {
            response = response.withHeader("Location", location);
        }

        documentStoreWireMockRule.stubFor(request.willReturn(response));
    }

}
