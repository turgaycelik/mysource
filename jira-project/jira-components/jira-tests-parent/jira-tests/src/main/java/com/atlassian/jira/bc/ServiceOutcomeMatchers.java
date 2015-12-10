package com.atlassian.jira.bc;

import com.atlassian.jira.util.ErrorCollection;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Static factory for matchers that work with the {@link ServiceOutcome}.
 */
public class ServiceOutcomeMatchers
{
    private ServiceOutcomeMatchers() {}

    public static <T> ValueMatcher<T> equalTo(T value)
    {
        return new ValueMatcher<T>(Matchers.equalTo(value));
    }

    public static <T> ValueMatcher<T> nullValue(Class<T> value)
    {
        return new ValueMatcher<T>(Matchers.nullValue(value));
    }

    public static <T> ValueMatcher<T> equalTo(Matcher<? super T> matcher)
    {
        return new ValueMatcher<T>(matcher);
    }

    public static ErrorMatcher errorMatcher()
    {
        return new ErrorMatcher();
    }

    public static ErrorMatcher noError()
    {
        return new ErrorMatcher();
    }

    public static ErrorMatcher errorMatcher(String key, String value, ErrorCollection.Reason reason)
    {
        return new ErrorMatcher().addError(key, value).addReason(reason);
    }
}
