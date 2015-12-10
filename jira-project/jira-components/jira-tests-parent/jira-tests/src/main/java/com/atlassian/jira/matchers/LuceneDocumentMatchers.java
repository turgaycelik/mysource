package com.atlassian.jira.matchers;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * @since v6.1
 */
public class LuceneDocumentMatchers
{

    public static class DocumentFieldMatcher extends FeatureMatcher<Document, Fieldable>
    {
        private final String fieldName;

        public DocumentFieldMatcher(final String fieldName, final Matcher<? super Fieldable> fieldMatcher)
        {
            super(fieldMatcher, "a document with a field \""+fieldName+"\"", fieldName);
            this.fieldName = fieldName;
        }

        @Override
        protected Fieldable featureValueOf(final Document actual)
        {
            return actual.getFieldable(fieldName);
        }
    }

    public static class FieldableHasStringValue extends FeatureMatcher<Fieldable, String>
    {

        public FieldableHasStringValue(final Matcher<? super String> subMatcher)
        {
            super(subMatcher, "field with a value", "string value");
        }

        @Override
        protected String featureValueOf(final Fieldable actual)
        {
            return actual.stringValue();
        }
    }

    public static FieldableHasStringValue fieldableHasStringValue(final Matcher<? super String> stringValueMatcher){
        return new FieldableHasStringValue(stringValueMatcher);
    }

    public static DocumentFieldMatcher hasStringFieldThat(final String fieldName, final Matcher<Fieldable> expectedValue)
    {
        return new DocumentFieldMatcher(fieldName, expectedValue);
    }

    public static DocumentFieldMatcher hasStringField(final String fieldName, final String expectedValue)
    {
        Matcher<? super String> string = Matchers.equalTo(expectedValue);
        return hasStringFieldThat(fieldName, fieldableHasStringValue(string));
    }

}
