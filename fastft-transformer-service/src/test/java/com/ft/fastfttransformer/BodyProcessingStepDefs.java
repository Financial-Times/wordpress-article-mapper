package com.ft.fastfttransformer;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import com.ft.bodyprocessing.transformer.FieldTransformer;
import com.ft.bodyprocessing.xml.eventhandlers.SimpleTransformTagXmlEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import com.ft.fastfttransformer.transformer.BodyProcessingFieldTransformerFactory;
import com.ft.fastfttransformer.transformer.BodyTransformationXMLEventRegistry;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang.RandomStringUtils;
import org.codehaus.stax2.ri.evt.StartElementEventImpl;

public class BodyProcessingStepDefs {

    private String fastFTBodyText;
    private String transformedBodyText;
    private FieldTransformer bodyTransformer;

    private static final String TRANSACTION_ID = randomChars(10);
    private static final String TEXT = "Some text in between tags";
    private BodyTransformationXMLEventRegistry registry;

    private Map <String, String> rulesAndHandlers;

    private static String randomChars(int howMany) {
        return RandomStringUtils.randomAlphanumeric(howMany).toLowerCase();
    }

    @Before
    public void setup() {
        bodyTransformer = new BodyProcessingFieldTransformerFactory().newInstance();
        registry = new BodyTransformationXMLEventRegistry();
        rulesAndHandlers = new HashMap<String, String>();
        rulesAndHandlers.put( "STRIP ELEMENT AND CONTENTS" , "StripElementAndContentsXMLEventHandler");
        rulesAndHandlers.put( "STRIP ELEMENT AND LEAVE CONTENT", "StripXMLEventHandler");
        rulesAndHandlers.put( "RETAIN ELEMENT AND REMOVE ATTRIBUTES", "RetainWithoutAttributesXMLEventHandler");
        rulesAndHandlers.put( "TRANSFORM THE TAG", "SimpleTransformTagXmlEventHandler");
        rulesAndHandlers.put( "CONVERT HTML ENTITY TO UNICODE", "SimpleTransformTagXmlEventHandler");
        rulesAndHandlers.put( "STRIP ELEMENT AND CONTENTS BY DEFAULT", "StripXMLEventHandler");
    }

    @Given("^I have html (.+?)$")
    public void I_have_html(String html) throws Throwable {
        fastFTBodyText = html;
    }

    @Given("^I have start tag (.+) and end tag (.+)$")
    public void I_have_start_and_end_tags(String start, String end) throws Throwable {
        fastFTBodyText = start + TEXT + end;
    }


    @Given("^I have a rule to (.+) and an entity (.+)$")
    public void I_have_a_rule_and_an_entity(String rule, String entity) throws Throwable {
        fastFTBodyText = "<body>" + TEXT +  entity + "</body>";
    }

    @Then("^it is left unmodified$")
    public void it_is_left_unmodified() {
        assertThat(transformedBodyText,equalToIgnoringCase(fastFTBodyText));
    }

    @Given("^Tag name is (.+) is assigned to rule (.+)$")
    public void tag_name_is_assigned_to_rule(String name, String rule) throws Throwable {
        assertTagIsRegistered(name, rule);
    }

    @And("^the tag (.+) adheres to the (.+) rule$")
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


    @Then("^the start tag (.+) should have been removed$")
        public void the_start_tag_should_have_been_removed(String tagname) throws Throwable {
        assertThat("start tag wasn't removed", transformedBodyText, not(containsString("<" + tagname + ">")));
    }

    @Then("^the end tag (.+) should have be removed$")
    public void the_end_tag_should_have_been_removed(String tagname) throws Throwable {
        assertThat("end tag wasn't removed", transformedBodyText, not(containsString("</" + tagname + ">")));
    }

    @And("^the text inside should have been removed$")
    public void the_text_inside_should_have_been_removed() throws Throwable {
        assertThat("Text was removed", transformedBodyText, not(containsString(TEXT)));
    }

    @Then("^the text inside should not have been removed$")
    public void the_text_inside_should_not_have_been_removed() throws Throwable {
        assertThat("Text was removed", transformedBodyText, containsString(TEXT));
    }

    @And("^the attributes inside the (.+) tag should be removed$")
    public void the_attributes_inside_the_tag_should_be_removed(String start) throws Throwable {
        String [] splitTag = start.split("\\s");
        String tagWithoutAttributes = splitTag[0] + ">";
        assertThat("closed start tag without attributes not found", transformedBodyText, containsString(tagWithoutAttributes));
    }

    @Then("^the tag should be replaced with the tag (.+)$")
    public void the_before_tag_should_be_replaced_with_the_after_tag(String tagname) throws Throwable {
        String expected = "<" + tagname + ">" + TEXT + "</" + tagname + ">" ;
        assertThat(transformedBodyText, is(expected));
    }

    @Then("^the entity should be replaced by the unicode codepoint (.+)$")
    public void the_entity_should_be_replace_by_unicode_codepoint(String codepoint) throws Throwable {
        int codePointInt = Integer.decode(codepoint);
        char[] chars = Character.toChars(codePointInt);
        String expected = "<body>" + TEXT  + new String(chars) + "</body>";
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

    @Given("^the fastFt body contains (.+) the transformer will (.+)$")
    public void the_fastFT_body_contains(String tagname, String rule) throws Throwable {
        assertTagIsRegistered(tagname,rule);
    }


    @Given("^a replacement tag (.+) and the fastFt body contains (.+) the transformer will (.+)$")
    public void the_fastFT_body_contains_transforms_into(String replacement, String tagname, String rule) throws Throwable {
        assertTagIsRegisteredToTransform(rule, tagname, replacement);
    }


}
