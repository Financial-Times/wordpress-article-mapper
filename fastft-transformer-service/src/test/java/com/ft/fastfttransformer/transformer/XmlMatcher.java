package com.ft.fastfttransformer.transformer;


import java.io.IOException;

import org.custommonkey.xmlunit.Diff;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.xml.sax.SAXException;

public class XmlMatcher extends TypeSafeDiagnosingMatcher<String> {

    final String xml;

    public XmlMatcher(String xml) {
        this.xml = xml;
    }

    @Override
    protected boolean matchesSafely(String item, Description mismatchDescription) {
        try {
            Diff diff = new Diff(xml, item);
            boolean matches = diff.identical();
            if (!matches) {
                mismatchDescription.appendText(diff.toString());
            }
            return matches;
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void describeTo(Description description) {
        description
                .appendText("xml matching ")
                .appendValue(xml);
    }

    public static Matcher<String> identicalXmlTo(String xml) {
        return new XmlMatcher(xml);
    }
}
