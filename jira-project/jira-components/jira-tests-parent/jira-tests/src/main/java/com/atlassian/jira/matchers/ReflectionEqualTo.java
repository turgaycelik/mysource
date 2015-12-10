package com.atlassian.jira.matchers;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * Matcher that uses {@link org.apache.commons.lang.builder.EqualsBuilder#reflectionEquals(Object, Object)} instead of a
 * class's equals(Object) implementation.
 *
 * @since v4.4
 */
public class ReflectionEqualTo<T> extends TypeSafeMatcher<T>
{
    /**
     * Matcher that compares objects using reflection.
     *
     * @param expected the expected object
     * @return a Matcher
     */
    public static <T> Matcher<T> reflectionEqualTo(T expected)
    {
        return new ReflectionEqualTo<T>(expected);
    }

    /**
     * The expected object.
     */
    private final T expected;

    /**
     * Creates a new ReflectionEqualTo matcher with the expected value.
     *
     * @param expected the expected value
     */
    public ReflectionEqualTo(T expected)
    {
        this.expected = expected;
    }

    @Override
    public boolean matchesSafely(T actual)
    {
        return EqualsBuilder.reflectionEquals(expected, actual);
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText(String.format(ToStringBuilder.reflectionToString(expected, SHORT_PREFIX_STYLE)));
    }
}
