package com.ft.wordpressarticletransformer.steps;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import com.ft.bodyprocessing.transformer.FieldTransformer;
import com.ft.bodyprocessing.xml.eventhandlers.SimpleTransformTagXmlEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformerFactory;
import com.ft.wordpressarticletransformer.transformer.StructuredWordPressSourcedBodyXMLEventHandlerRegistry;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang.RandomStringUtils;
import org.codehaus.stax2.ri.evt.EntityReferenceEventImpl;
import org.codehaus.stax2.ri.evt.StartElementEventImpl;

public class BodyProcessingStepDefs {

    private String fastFTBodyText;
    private String transformedBodyText;
    private FieldTransformer bodyTransformer;

    private static final String TRANSACTION_ID = randomChars(10);
    private static final String TEXT = "Some text in between tags";
    private StructuredWordPressSourcedBodyXMLEventHandlerRegistry registry;

    private Map <String, String> rulesAndHandlers;

    private static String randomChars(int howMany) {
        return RandomStringUtils.randomAlphanumeric(howMany).toLowerCase();
    }

    @Before
    public void setup() {
        bodyTransformer = new BodyProcessingFieldTransformerFactory().newInstance();
        registry = new StructuredWordPressSourcedBodyXMLEventHandlerRegistry();
        rulesAndHandlers = new HashMap<String, String>();
        rulesAndHandlers.put( "STRIP ELEMENT AND CONTENTS" , "StripElementAndContentsXMLEventHandler");
        rulesAndHandlers.put( "STRIP ELEMENT AND LEAVE CONTENT", "StripXMLEventHandler");
        rulesAndHandlers.put( "RETAIN ELEMENT AND REMOVE ATTRIBUTES", "RetainWithoutAttributesXMLEventHandler");
        rulesAndHandlers.put( "TRANSFORM THE TAG", "SimpleTransformTagXmlEventHandler");
        rulesAndHandlers.put( "CONVERT HTML ENTITY TO UNICODE", "PlainTextHtmlEntityReferenceEventHandler");
        rulesAndHandlers.put( "STRIP ELEMENT AND LEAVE CONTENT BY DEFAULT", "StripXMLEventHandler");
    }

    @Given("^I have html (.+?)$")
    public void I_have_html(String html) throws Throwable {
        fastFTBodyText = html;
    }
    
    @Given("^there are empty paragraphs in the body$")
    public void there_are_empty_paragraphs() throws Throwable {
    	// no op!
    }


    @Given("^I have a rule to (.+) and an entity (.+)$")
    public void I_have_a_rule_and_an_entity(String rule, String entity) throws Throwable {
        String handler = rulesAndHandlers.get(rule);
        String entitybasic = entity.substring(1, entity.length()-1);
        EntityReferenceEventImpl event = new EntityReferenceEventImpl(null, entitybasic);
        XMLEventHandler eventHandler = registry.getEventHandler(event);
        assertThat("The handler is incorrect", eventHandler.getClass().getSimpleName(), equalTo(handler));
    }

    @Then("^it is left unmodified$")
    public void it_is_left_unmodified() {
        assertThat(transformedBodyText,equalToIgnoringCase(fastFTBodyText));
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
        transformedBodyText = bodyTransformer.transform(fastFTBodyText, TRANSACTION_ID);
    }


    @Then("^it is transformed, (.+) becomes (.+)$")
    public void the_before_becomes_after(String before, String after) throws Throwable {
        transformedBodyText = bodyTransformer.transform(before, TRANSACTION_ID);
        assertThat("before and after do not match", transformedBodyText, equalTo(after));
    }


    @Then("^it is transformed the entity (.+) should be replaced by the unicode codepoint (.+)$")
    public void the_entity_should_be_replace_by_unicode_codepoint(String entity, String codepoint) throws Throwable {
        int codePointInt = Integer.decode(codepoint);
        char[] chars = Character.toChars(codePointInt);
        String expected = "<p>" + TEXT  + new String(chars) + "</p>";
        fastFTBodyText = "<p>" + TEXT  +  entity + "</p>";
        transformedBodyText = bodyTransformer.transform(fastFTBodyText, TRANSACTION_ID);
        assertThat(transformedBodyText, is(expected));
    }

    private void assertTagIsRegisteredToTransform(String rule, String before, String after){
        SimpleTransformTagXmlEventHandler eventHandler = null;
        try{
            eventHandler = (SimpleTransformTagXmlEventHandler)assertTagIsRegistered(before, rule);
        }
        catch (ClassCastException cce){
            assertThat("The transformer is not SimpleTransformTagXmlEventHandler", false);
        }
        assertThat("The replacement tag is not registered properly", eventHandler.getNewElement(), equalTo(after));

    }

    private XMLEventHandler assertTagIsRegistered( String name, String rule ){
        String handler = rulesAndHandlers.get(rule);
        StartElementEventImpl startElement = StartElementEventImpl.construct(null, new QName(name), null, null, null);
        XMLEventHandler eventHandler = registry.getEventHandler(startElement);
        assertThat("handler incorrect", eventHandler.getClass().getSimpleName(), equalTo(handler) );
        return eventHandler;
    }

    @Given("^the \\w+ body contains (.+) the transformer will (.+)$")
    public void the_system_body_contains(String tagname, String rule) throws Throwable {
        assertTagIsRegistered(tagname,rule);
    }


    @Given("^a replacement tag (.+) and the fastFt body contains (.+) the transformer will (.+)$")
    public void the_fastFT_body_contains_transforms_into(String replacement, String tagname, String rule) throws Throwable {
        assertTagIsRegisteredToTransform(rule, tagname, replacement);
    }

	@Then("^I get the html (.+?)$")
	public void then_I_get_the_html(String html) throws Throwable {
		assertThat(transformedBodyText,equalToIgnoringWhiteSpace(html));
	}

}
