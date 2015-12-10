package com.atlassian.jira.matchers;

import com.atlassian.jira.util.ErrorCollection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matchers for {@link com.atlassian.jira.util.ErrorCollection}.
 *
 * @since v6.3
 */
public class ErrorCollectionMatchers
{
    private ErrorCollectionMatchers() {}

    public static Matcher<ErrorCollection> isEmpty()
    {
        return new TypeSafeMatcher<ErrorCollection>()
        {
            @Override
            protected boolean matchesSafely(final ErrorCollection errorCollection)
            {
                return errorCollection.getErrors().isEmpty() && errorCollection.getErrorMessages().isEmpty();
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Empty error collection");
            }
        };
    }

    public static Matcher<ErrorCollection> containsSystemError(final String errorMessage)
    {
        return new TypeSafeMatcher<ErrorCollection>()
        {
            @Override
            protected boolean matchesSafely(final ErrorCollection errorCollection)
            {
                return errorCollection.getErrorMessages().equals(ImmutableList.of(errorMessage)) &&
                        errorCollection.getErrors().isEmpty();
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Error collection with one error message: ").appendValue(errorMessage);
            }
        };
    }

    public static Matcher<ErrorCollection> containsFieldError(final String field, final String errorMessage)
    {
        return new TypeSafeMatcher<ErrorCollection>()
        {
            @Override
            protected boolean matchesSafely(final ErrorCollection errorCollection)
            {
                return errorCollection.getErrorMessages().isEmpty() &&
                        errorCollection.getErrors().equals(ImmutableMap.of(field, errorMessage));
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Error collection with one error for field: ")
                        .appendValue(field).appendText(" with message: ")
                        .appendValue(errorMessage);
            }
        };
    }
}
