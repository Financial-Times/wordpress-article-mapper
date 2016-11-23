package com.ft.wordpressarticlemapper.component;

import com.ft.wordpressarticlemapper.WordPressArticleMapperApplication;
import com.ft.wordpressarticlemapper.configuration.WordPressArticleTransformerConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class AppSetupComponentTest {

    private static final String CONFIG_FILE = "wordpress-article-mapper-test.yaml";

    @ClassRule
    public static DropwizardAppRule<WordPressArticleTransformerConfiguration> appRule =
            new DropwizardAppRule<>(WordPressArticleMapperApplication.class, CONFIG_FILE);

    @SuppressWarnings("rawtypes")
    @Test
    public void thatBuildInfoResourceIsRegisteredAndWorking() {
        Client client = new Client();
        ClientResponse response = client.resource(format(
                "http://localhost:%d%s",
                appRule.getLocalPort(),
                "/build-info")).get(ClientResponse.class);

        assertThat(response.getStatus(), equalTo(SC_OK));
        Map entity = response.getEntity(Map.class);
        assertThat(entity.keySet(), equalTo(Collections.singleton("buildInfo")));
        @SuppressWarnings("unchecked")
        Map<String, String> buildInfo = (Map) entity.get("buildInfo");
        assertThat(buildInfo, hasEntry("artifact.id", "wordpress-article-mapper"));
    }
}
