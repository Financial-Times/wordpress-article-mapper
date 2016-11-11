package com.ft.wordpressarticlemapper.steps;


import com.ft.bodyprocessing.richcontent.ConvertParameters;
import com.ft.bodyprocessing.richcontent.VideoMatcher;
import com.ft.bodyprocessing.richcontent.VideoSiteConfiguration;
import com.ft.bodyprocessing.transformer.FieldTransformer;
import com.ft.bodyprocessing.xml.eventhandlers.SimpleTransformTagXmlEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import com.ft.wordpressarticlemapper.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticlemapper.resources.BlogApiEndpointMetadata;
import com.ft.wordpressarticlemapper.transformer.BodyProcessingFieldTransformerFactory;
import com.ft.wordpressarticlemapper.transformer.StructuredWordPressSourcedBodyXMLEventHandlerRegistry;
import com.ft.wordpressarticlemapper.util.ClientMockBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.client.Client;

import org.apache.commons.lang.RandomStringUtils;
import org.codehaus.stax2.ri.evt.EntityReferenceEventImpl;
import org.codehaus.stax2.ri.evt.StartElementEventImpl;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.mock;

public class BodyProcessingStepDefs {

    private String wordpressBodyText;
    private String transformedBodyText;
    private FieldTransformer bodyTransformer;
    private VideoMatcher videoMatcher;

    private static final String TRANSACTION_ID = randomChars(10);
    private static final String TEXT = "Some text in between tags";
    private StructuredWordPressSourcedBodyXMLEventHandlerRegistry registry;

    private Map<String, String> rulesAndHandlers;

    private static String randomChars(int howMany) {
        return RandomStringUtils.randomAlphanumeric(howMany).toLowerCase();
    }

    private static final List<String> T = Collections.singletonList("t");
    private static final List<String> NONE = Collections.emptyList();

    private static final String CONVERT_FROM_PARAMETER = "start";
    private static final String CONVERTED_TO_PARAMETER = "t";
    private static final String CONVERSION_TEMPLATE = "%ss";
    private static final ConvertParameters CONVERT_PARAMETERS = new ConvertParameters(CONVERT_FROM_PARAMETER, CONVERTED_TO_PARAMETER, CONVERSION_TEMPLATE);
    private static final List<ConvertParameters> CONVERT_PARAMETERS_LIST = ImmutableList.of(CONVERT_PARAMETERS);
    private static final URI DOCUMENT_STORE_URI = URI.create("http://localhost:8080/");
    private static final URI DOCUMENT_STORE_QUERY_URI = DOCUMENT_STORE_URI.resolve("/content-query");
    private static final URI CONTENT_READ_URI = URI.create("http://localhost:8080/content-read");
    private static final String CONTENT_READ_HOST_HEADER = "content-read";
    private static final String DOC_STORE_HOST_HEADER = "document-store-api";
    private static final String CONTENT_UUID = "8adad508-077b-3795-8569-18e532cabf96";

    public static List<VideoSiteConfiguration> DEFAULTS = Arrays.asList(
            new VideoSiteConfiguration("https?://www.youtube.com/watch\\?v=(?<id>[A-Za-z0-9_-]+)", "https://www.youtube.com/watch?v=%s", true, T, null, true),
            new VideoSiteConfiguration("https?://www.youtube.com/embed/(?<id>[A-Za-z0-9_-]+)", "https://www.youtube.com/watch?v=%s", false, NONE, CONVERT_PARAMETERS_LIST, true),
            new VideoSiteConfiguration("https?://youtu.be/(?<id>[A-Za-z0-9_-]+)", "https://www.youtube.com/watch?v=%s", false, T, null, true),
            new VideoSiteConfiguration("https?://www.vimeo.com/(?<id>[0-9]+)", null, false, NONE, null, true),
            new VideoSiteConfiguration("//player.vimeo.com/video/(?<id>[0-9]+)", "https://www.vimeo.com/%s", true, NONE, null, true),
            new VideoSiteConfiguration("https?://video.ft.com/(?<id>[0-9]+)/", null, false, NONE, null, true)
    );

    private Client resolverClient = mock(Client.class);
    private Client documentStoreQueryClient = mock(Client.class);
    private Client contentReadClient = mock(Client.class);

    @Before
    public void setup() {
        String fastFtHost = "www.ft.com/fastft";
        String fastFtId = "FT-LABS-WP-1-335";
        String fastFtAuth = "http://api.ft.com/system/" + fastFtId;
        Set<String> fastFtBrands = ImmutableSet.of("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b54",
                "http://api.ft.com/things/dbb0bdae-1f0c-11e4-b0cb-b2227cce2b54");

        List<BlogApiEndpointMetadata> metadataList = ImmutableList.of(new BlogApiEndpointMetadata(fastFtHost, fastFtBrands, fastFtId));
        BlogApiEndpointMetadataManager blogApiEndpointMetadataManager = new BlogApiEndpointMetadataManager(metadataList);

        videoMatcher = new VideoMatcher(DEFAULTS);
        bodyTransformer = new BodyProcessingFieldTransformerFactory(videoMatcher,
                Collections.singleton(Pattern.compile("https?:\\/\\/on\\.ft\\.com/.*")),
                blogApiEndpointMetadataManager,
                resolverClient,
                1, 2,
                documentStoreQueryClient,
                DOCUMENT_STORE_URI,
                DOC_STORE_HOST_HEADER,
                contentReadClient,
                CONTENT_READ_URI,
                CONTENT_READ_HOST_HEADER)
                .newInstance();

        ClientMockBuilder clientMockBuilder = new ClientMockBuilder();

        URI identifierValue = URI.create("http://www.ft.com/fastft/2015/12/09/south-african-rand-dives-after-finance-ministers-exit/");
        clientMockBuilder.mockResolverRedirect(resolverClient, URI.create("http://on.ft.com/1NVIQzo"), identifierValue);
        clientMockBuilder.mockDocumentStoreQuery(
                documentStoreQueryClient,
                DOCUMENT_STORE_QUERY_URI,
                URI.create(fastFtAuth),
                identifierValue,
                URI.create("http://www.ft.com/content/" + CONTENT_UUID),
                SC_MOVED_PERMANENTLY
        );
        clientMockBuilder.mockContentRead(contentReadClient, CONTENT_READ_URI, CONTENT_UUID, CONTENT_READ_HOST_HEADER, SC_OK);

        registry = new StructuredWordPressSourcedBodyXMLEventHandlerRegistry(videoMatcher);
        rulesAndHandlers = new HashMap<>();
        rulesAndHandlers.put("STRIP ELEMENT AND CONTENTS", "StripElementAndContentsXMLEventHandler");
        rulesAndHandlers.put("STRIP ELEMENT AND LEAVE CONTENT", "StripXMLEventHandler");
        rulesAndHandlers.put("RETAIN ELEMENT AND REMOVE ATTRIBUTES", "RetainWithoutAttributesXMLEventHandler");
        rulesAndHandlers.put("TRANSFORM THE TAG", "SimpleTransformTagXmlEventHandler");
        rulesAndHandlers.put("CONVERT HTML ENTITY TO UNICODE", "PlainTextHtmlEntityReferenceEventHandler");
        rulesAndHandlers.put("STRIP ELEMENT AND LEAVE CONTENT BY DEFAULT", "StripXMLEventHandler");
    }


    @Given("^I have body (.+?)$")
    public void I_have_body(String html) throws Throwable {
        wordpressBodyText = "<body>" + html + "</body>";
    }

    @Given("^there are empty paragraphs in the body$")
    public void there_are_empty_paragraphs() throws Throwable {
        // no op!
    }


    @Given("^I have a rule to (.+) and an entity (.+)$")
    public void I_have_a_rule_and_an_entity(String rule, String entity) throws Throwable {
        String handler = rulesAndHandlers.get(rule);
        String entitybasic = entity.substring(1, entity.length() - 1);
        EntityReferenceEventImpl event = new EntityReferenceEventImpl(null, entitybasic);
        XMLEventHandler eventHandler = registry.getEventHandler(event);
        assertThat("The handler is incorrect", eventHandler.getClass().getSimpleName(), equalTo(handler));
    }

    @Then("^it is left unmodified$")
    public void it_is_left_unmodified() {
        assertThat(transformedBodyText, equalToIgnoringCase(wordpressBodyText));
    }

    @And("^the tag (.+) adheres to the (.+)$")
    public void tag_name_adheres_to_rule(String name, String rule) throws Throwable {
        assertTagIsRegistered(name, rule);
    }

    @And("^the before tag (.+) and the after tag (.+) adheres to the (.+) rule$")
    public void before_and_after_tag_name_adheres_to_rule(String name, String aftername, String rule) throws Throwable {
        assertTagIsRegisteredToTransform(rule, name, aftername);
    }


    @When("^I transform it$")
    public void I_transform_it() throws Throwable {
        transformedBodyText = bodyTransformer.transform(wordpressBodyText, TRANSACTION_ID);
    }


    @Then("^it is transformed, (.+) becomes (.+)$")
    public void the_before_becomes_after(String before, String after) throws Throwable {
        transformedBodyText = bodyTransformer.transform(wrapped(before), TRANSACTION_ID);
        assertThat("transformed text", transformedBodyText, equalTo(wrapped(after)));
    }

    private String wrapped(String bodyMarkUp) {
        return String.format("<body>%s</body>", bodyMarkUp);
    }


    @Then("^it is transformed the entity (.+) should be replaced by the unicode codepoint (.+)$")
    public void the_entity_should_be_replace_by_unicode_codepoint(String entity, String codepoint) throws Throwable {
        int codePointInt = Integer.decode(codepoint);
        char[] chars = Character.toChars(codePointInt);
        String expected = "<p>" + TEXT + new String(chars) + "</p>";
        wordpressBodyText = "<p>" + TEXT + entity + "</p>";
        transformedBodyText = bodyTransformer.transform(wordpressBodyText, TRANSACTION_ID);
        assertThat(transformedBodyText, is(wrapped(expected)));
    }

    private void assertTagIsRegisteredToTransform(String rule, String before, String after) {
        SimpleTransformTagXmlEventHandler eventHandler = null;
        try {
            eventHandler = (SimpleTransformTagXmlEventHandler) assertTagIsRegistered(before, rule);
        } catch (ClassCastException cce) {
            assertThat("The transformer is not SimpleTransformTagXmlEventHandler", false);
        }
        assertThat("The replacement tag is not registered properly", eventHandler.getNewElement(), equalTo(after));

    }

    private XMLEventHandler assertTagIsRegistered(String name, String rule) {
        String handler = rulesAndHandlers.get(rule);
        StartElementEventImpl startElement = StartElementEventImpl.construct(null, new QName(name), null, null, null);
        XMLEventHandler eventHandler = registry.getEventHandler(startElement);
        assertThat("handler incorrect", eventHandler.getClass().getSimpleName(), equalTo(handler));
        return eventHandler;
    }

    @Given("^the \\w+ body contains (.+) the transformer will (.+)$")
    public void the_system_body_contains(String tagname, String rule) throws Throwable {
        assertTagIsRegistered(tagname, rule);
    }


    @Given("^a replacement tag (.+) and the fastFt body contains (.+) the transformer will (.+)$")
    public void the_fastFT_body_contains_transforms_into(String replacement, String tagname, String rule) throws Throwable {
        assertTagIsRegisteredToTransform(rule, tagname, replacement);
    }

    @Then("^I get the body (.+?)$")
    public void then_I_get_the_body(String html) throws Throwable {
        assertThat(transformedBodyText, equalToIgnoringWhiteSpace("<body>" + html + "</body>"));
    }

    @Given("^I have text in Wordpress XML like (.*)$")
    public void I_have_body_text_in_Methode_XML_like_before(String text) throws Throwable {
        wordpressBodyText = text;
    }

    @When("^I transform it into our Content Store format$")
    public void i_transform_it_into_our_content_store_format() throws Throwable {
        transformedBodyText = bodyTransformer.transform(wordpressBodyText, TRANSACTION_ID);
    }

    @Then("^the body should be like (.*)$")
    public void the_body_should_be_like_after(String after) throws Throwable {
        assertThat("the body was not transformed as expected", transformedBodyText, is(after));
    }

}
