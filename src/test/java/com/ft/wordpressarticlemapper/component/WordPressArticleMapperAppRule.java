package com.ft.wordpressarticlemapper.component;

import com.ft.message.consumer.MessageListener;
import com.ft.messagequeueproducer.MessageProducer;
import com.ft.wordpressarticlemapper.WordPressArticleMapperApplication;
import com.ft.wordpressarticlemapper.configuration.ConsumerConfiguration;
import com.ft.wordpressarticlemapper.configuration.ProducerConfiguration;
import com.ft.wordpressarticlemapper.configuration.WordPressArticleTransformerConfiguration;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sun.jersey.api.client.Client;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import static com.ft.wordpressarticlemapper.util.TestFileUtil.resourceFilePath;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_TEMPORARILY;
import static org.junit.Assert.fail;

public class WordPressArticleMapperAppRule implements TestRule {

    private final RuleChain ruleChain;
    private WireMockRule documentStoreWireMockRule;
    private String contentReadOutputTemplate;
    private static MessageProducer producer;
    private static MessageListener listener;
    private DropwizardAppRule<WordPressArticleTransformerConfiguration> appRule;

    public static class StubWordPressArticleMapperApplication extends WordPressArticleMapperApplication {

        @Override
        protected void startListener(Environment environment, MessageListener listener, ConsumerConfiguration config, Client consumerClient) {
            WordPressArticleMapperAppRule.listener = listener;
        }

        @Override
        protected MessageProducer configureMessageProducer(Environment environment, ProducerConfiguration config) {
            return producer;
        }
    }

    public WordPressArticleMapperAppRule(String configurationPath, int documentStorePort, MessageProducer producer) {
        WordPressArticleMapperAppRule.producer = producer;
        appRule = new DropwizardAppRule<>(StubWordPressArticleMapperApplication.class, configurationPath);

        documentStoreWireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
                .port(documentStorePort)
        );

        ruleChain = RuleChain
                .outerRule(documentStoreWireMockRule)
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
                    documentStoreWireMockRule.shutdown();
                }
            }
        };
    }

    public void mockContentReadResponse(String uuid, int status) {
        MappingBuilder request = get(urlPathEqualTo("/content/" + uuid));
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

    public static MessageListener getListener() {
        return listener;
    }

    public void reset() {
        documentStoreWireMockRule.resetToDefaultMappings();
        org.mockito.Mockito.reset(producer);
    }

    public int getWordPressArticleMapperLocalPort() {
        return appRule.getLocalPort();
    }
}
