package com.ft.fastfttransformer;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import javax.xml.namespace.QName;

import com.ft.bodyprocessing.transformer.FieldTransformer;
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

    private static String randomChars(int howMany) {
        return RandomStringUtils.randomAlphanumeric(howMany).toLowerCase();
    }

    @Before
    public void setup() {
        bodyTransformer = new BodyProcessingFieldTransformerFactory().newInstance();
    }

    /*
    @Given("^I have html (.+?) hosted at location (.+?)$")
    public void I_have_html(String html, String base) throws Throwable {
        fastFTBodyText = html;
        baseUri = URI.create(base);
    } */

    @Given("^I have html (.+?)$")
         public void I_have_html(String html) throws Throwable {
        fastFTBodyText = html;
    }



    @When("^I transform it$")
    public void I_transform_it() {
        transformedBodyText = bodyTransformer.transform(fastFTBodyText, TRANSACTION_ID);
    }

    @Then("^it is left unmodified$")
    public void it_is_left_unmodified() {
        assertThat(transformedBodyText,equalToIgnoringCase(fastFTBodyText));
    }

    @Given("^Tag name is (.+) is assigned to rule (.+)$")
    public void tag_name_is_assigned_to_rule(String name, String rule) throws Throwable {
        BodyTransformationXMLEventRegistry registry = new BodyTransformationXMLEventRegistry();
        StartElementEventImpl startElement = StartElementEventImpl.construct(null, new QName(name), null, null, null);
        XMLEventHandler eventHandler = registry.getEventHandler(startElement);
        assertThat("handler incorrect", eventHandler.getClass().getSimpleName(), equalTo(rule) );
    }

    @When("^I have a StripElementAndContent tag type and a tag called (.+)$")
    public void I_have_a_strip_element_and_content_tag_type(String tagname) throws Throwable {
        fastFTBodyText = "<"+ tagname + ">" + TEXT + "</"+ tagname + ">";
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

    @When("^I have a Strip tag type and a tag called (.+)$")
    public void I_have_a_Strip_tag_type_and_a_tag_called(String tagname) throws Throwable {
        fastFTBodyText = "<"+ tagname + ">" + TEXT + "</"+ tagname + ">";
        transformedBodyText = bodyTransformer.transform(fastFTBodyText, TRANSACTION_ID);
    }

    @Then("^the text inside should not have been removed$")
    public void the_text_inside_should_not_have_been_removed() throws Throwable {
        assertThat("Text was removed", transformedBodyText, containsString(TEXT));
    }
}
