package com.ft.wordpressarticlemapper.transformer.html;

import com.ft.wordpressarticlemapper.transformer.DefaultTransactionIdBodyProcessingContext;
import org.junit.Test;
import org.xmlmatchers.XmlMatchers;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.transform.XmlConverters.the;

/**
 * RemoveEmptyElementsBodyProcessorTest
 *
 * @author Simon
 */
public class RemoveEmptyElementsBodyProcessorTest {

	public static final String EXAMPLE_BODY = "<body><p><a href=\"http://uat.ftalphaville.ft.com/files/2014/10/Chart5.png\" target=\"_blank\"><img class=\"aligncenter size-full wp-image-2012992\" src=\"http://uat.ftalphaville.ft.com/files/2014/10/Chart5-e1413767777269.png\" alt=\"\" width=\"300\" height=\"660\" /></a></p></body>";
	public static final DefaultTransactionIdBodyProcessingContext TEST_CONTEXT = new DefaultTransactionIdBodyProcessingContext("test");


	@Test
	public void shouldNotRemoveElementsThatWrapNonTextContent() {
		RemoveEmptyElementsBodyProcessor processor = paragraphsAndLinksWithoutImages();

		String result = processor.process(EXAMPLE_BODY, TEST_CONTEXT);

		assertEquivalentXml(result, EXAMPLE_BODY);
	}

	@Test
	public void shouldRemoveElementsThatWrapWhitespaceOnly() {

		String example = "<body><p>   </p><p>Test <a href=\"ghh\"> </a></p></body>";
		String expectedResult = "<body><p>Test </p></body>";

		RemoveEmptyElementsBodyProcessor processor = paragraphsAndLinksWithoutImages();

		String result = processor.process(example, TEST_CONTEXT);

		assertEquivalentXml(result, expectedResult);
	}

	@Test
	public void shouldRemoveElementsThatWrapEmptyElements() {
		String example = "<body><p><a href=\"ghh\"> </a></p><p>Test</p></body>";
		String expectedResult = "<body><p>Test</p></body>";

		RemoveEmptyElementsBodyProcessor processor = paragraphsAndLinksWithoutImages();

		String result = processor.process(example, TEST_CONTEXT);

		assertEquivalentXml(result, expectedResult);
	}

	@Test
	public void shouldReturnEmptyStringIfThereIsNoContentLeft() {
		String example = "<body><p><a href=\"ghh\"> </a></p></body>";

		RemoveEmptyElementsBodyProcessor processor = paragraphsAndLinksWithoutImages();

		String result = processor.process(example, TEST_CONTEXT);

		assertThat(result, equalTo(""));
	}


	@Test
	public void shouldRemoveElementsThatWrapEmptyElementsInAnyOrder() {
		String example = "<body><p><a href=\"ghh\"> </a></p><p>Test</p></body>";
		String expectedResult = "<body><p>Test</p></body>";

		// reverse order of configured element names
		RemoveEmptyElementsBodyProcessor processor = new RemoveEmptyElementsBodyProcessor(asList("a","p"), asList("img"));

		String result = processor.process(example, TEST_CONTEXT);

		assertEquivalentXml(result, expectedResult);

	}

	private void assertEquivalentXml(String result, String expectedResult) {
		assertThat(the(result), XmlMatchers.equivalentTo(the(expectedResult)));
	}

	private RemoveEmptyElementsBodyProcessor paragraphsAndLinksWithoutImages() {
		return new RemoveEmptyElementsBodyProcessor(asList("p","a"),asList("img"));
	}

}
