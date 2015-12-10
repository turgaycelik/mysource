package com.atlassian.jira.bc;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * @since v6.2
 */
public class ValueMatcher<T> extends TypeSafeMatcher<ServiceOutcome<T>>
{
    private final Matcher<? super T> matcher;

    public ValueMatcher(final Matcher<? super T> matcher)
    {
        this.matcher = matcher;
    }

    @Override
    protected boolean matchesSafely(final ServiceOutcome<T> item)
    {
        return matcher.matches(item.getReturnedValue());
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendDescriptionOf(matcher);
    }

    @Override
    protected void describeMismatchSafely(final ServiceOutcome<T> item, final Description mismatchDescription)
    {
        matcher.describeMismatch(item.getReturnedValue(), mismatchDescription);
    }
}
