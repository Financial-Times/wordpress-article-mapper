package com.ft.wordpressarticlemapper.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.wordpressarticlemapper.exception.InvalidStatusException;
import com.ft.wordpressarticlemapper.exception.PostNotFoundException;
import com.ft.wordpressarticlemapper.exception.UnexpectedStatusFieldException;
import com.ft.wordpressarticlemapper.exception.UnpublishablePostException;
import com.ft.wordpressarticlemapper.response.NativeWordPressContent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class NativeWordPressContentValidatorTest {

    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private NativeWordPressContentValidator contentValidator;

    @Before
    public void setUp() {
        contentValidator = new NativeWordPressContentValidator();
    }

    @Test
    public void thatNoExceptionIsThrownForValidContent() throws Exception {
        NativeWordPressContent content = JACKSON_MAPPER.reader(NativeWordPressContent.class)
                .readValue(loadFile("messaging/native-wordpress-content.json"));
        try {
            contentValidator.validate(content);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void thatExceptionIsThrownWhenPostIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No content supplied");
        NativeWordPressContent content = new NativeWordPressContent();
        contentValidator.validate(content);
    }

    @Test
    public void thatExceptionIsThrownWhenApiUrlIsNull() throws Exception {
        NativeWordPressContent content = JACKSON_MAPPER.reader(NativeWordPressContent.class)
                .readValue(loadFile("wordPress/__files/WILL_RETURN_200-no-apiurl-on-response.json"));
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No apiUrl supplied");
        contentValidator.validate(content);
    }

    @Test
    public void thatExceptionIsThrownWhenStatusIsNull() throws Exception {
        NativeWordPressContent content = JACKSON_MAPPER.reader(NativeWordPressContent.class)
                .readValue(loadFile("wordPress/__files/WILL_RETURN_STATUS_NULL-body-from-wordpress.json"));
        thrown.expect(InvalidStatusException.class);
        contentValidator.validate(content);
    }

    @Test
    public void thatExceptionIsThrownWhenStatusIsUnknown() throws Exception {
        NativeWordPressContent content = JACKSON_MAPPER.reader(NativeWordPressContent.class)
                .readValue(loadFile("wordPress/__files/WILL_RETURN_STATUS_UNKNOWN-body-from-wordpress.json"));
        thrown.expect(UnexpectedStatusFieldException.class);
        contentValidator.validate(content);
    }

    @Test
    public void thatExceptionIsThrownWhenUnsupportedType() throws Exception {
        NativeWordPressContent content = JACKSON_MAPPER.reader(NativeWordPressContent.class)
                .readValue(loadFile("wordPress/__files/WILL_RETURN_200-incorrect-blog-type.json"));
        thrown.expect(UnpublishablePostException.class);
        contentValidator.validate(content);
    }

    @Test
    public void thatExceptionIsThrownWhenDeleteMessage() throws Exception {
        NativeWordPressContent content = JACKSON_MAPPER.reader(NativeWordPressContent.class)
                .readValue(loadFile("wordPress/__files/WILL_RETURN_404-delete-event.json"));
        thrown.expect(PostNotFoundException.class);
        contentValidator.validate(content);
    }

    @Test
    public void thatExceptionIsThrownWhenInvalidUUID() throws Exception {
        NativeWordPressContent content = JACKSON_MAPPER.reader(NativeWordPressContent.class)
                .readValue(loadFile("wordPress/__files/wordpress-content-invalid-uuid.json"));
        thrown.expect(IllegalArgumentException.class);
        contentValidator.validate(content);
    }

    private String loadFile(final String filename) throws Exception {
        URL resource = getClass().getClassLoader().getResource(filename);
        if (resource != null) {
            final URI uri = resource.toURI();
            return new String(Files.readAllBytes(Paths.get(uri)), "UTF-8");
        }
        return null;
    }

}
