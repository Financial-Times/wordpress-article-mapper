package com.ft.wordpressarticletransformer.transformer.html;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;

import javax.xml.stream.events.Attribute;
import java.util.Map;

import static java.lang.String.format;

public class AttributeNameValuePredicate implements Predicate<Attribute> {

	Map<String,String> attributeValues;
    
    public AttributeNameValuePredicate(final Map<String, String> attributeValues) {
        notNull(attributeValues, "name");
        for (String key: attributeValues.keySet()) {
        	notNull(key, "name");
        	notNull(attributeValues.get(key), "value");
        }
        this.attributeValues = ImmutableMap.copyOf(attributeValues);
    }

    private void notNull(Object value, String paramName) {
        if (value == null) {
            throw new IllegalArgumentException(format("constructor param '%s' must not be null", paramName));
        }
    }

    @Override
    public boolean apply(Attribute attribute) {
        if (attribute == null) return false;
        String nameToMatch = attribute.getName().getLocalPart();
        String valueToMatch = attribute.getValue();
        for (String name: attributeValues.keySet()) {
        	String value = attributeValues.get(name);
        	if (name.equalsIgnoreCase(nameToMatch) && value.equalsIgnoreCase(valueToMatch)) {
        		return true;
        	}
        }
        return false;
    }

    public static Predicate<Attribute> attributeWithNameAndValue(Map<String,String> attributeValues) {
        return new AttributeNameValuePredicate(attributeValues);
    }
}
