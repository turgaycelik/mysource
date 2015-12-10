package com.atlassian.jira.matchers;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.jql.operand.QueryLiteral;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

/**
 * Hamcrest matchers for {@code QueryLiteral} values and collections.
 *
 * @since v7.0
 */
@SuppressWarnings({
        // Matcher utility classes often have to do awkward things like these...
        "AnonymousInnerClassWithTooManyMethods",
        "MethodWithMultipleReturnPoints",
        "CastToConcreteClass",
        "OverlyComplexMethod",
        "ChainOfInstanceofChecks" })
public class QueryLiteralMatchers
{
    private QueryLiteralMatchers()
    {
        // static-only
    }

    public static Matcher<Iterable<QueryLiteral>> emptyIterable()
    {
        return Matchers.emptyIterable();
    }

    public static Matcher<QueryLiteral> literal()
    {
        return new TypeSafeMatcher<QueryLiteral>()
        {
            @Override
            protected boolean matchesSafely(final QueryLiteral item)
            {
                return item.isEmpty();
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("QueryLiteral[EMPTY]");
            }
        };
    }

    public static Matcher<QueryLiteral> literal(final String expectedStringValue)
    {
        if (expectedStringValue == null)
        {
            return literal();
        }
        return new TypeSafeMatcher<QueryLiteral>()
        {
            @Override
            protected boolean matchesSafely(final QueryLiteral item)
            {
                return expectedStringValue.equals(item.getStringValue());
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("QueryLiteral[\"").appendValue(expectedStringValue).appendText("\"]");
            }
        };
    }

    public static Matcher<QueryLiteral> literal(final Long expectedLongValue)
    {
        if (expectedLongValue == null)
        {
            return literal();
        }
        return new TypeSafeMatcher<QueryLiteral>()
        {
            @Override
            protected boolean matchesSafely(final QueryLiteral item)
            {
                return expectedLongValue.equals(item.getLongValue());
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("QueryLiteral[").appendValue(expectedLongValue).appendText("]");
            }
        };
    }

    public static Matcher<? super QueryLiteral> literal(final Object expectedValue)
    {
        if (expectedValue == null)
        {
            return literal();
        }
        if (expectedValue instanceof String)
        {
            return literal((String)expectedValue);
        }
        if (expectedValue instanceof Long)
        {
            return literal((Long)expectedValue);
        }
        if (expectedValue instanceof QueryLiteral)
        {
            return equalTo((QueryLiteral)expectedValue);
        }
        throw new IllegalArgumentException("Cannot match instance of " + expectedValue.getClass().getName() +
                " as a QueryLiteral");
    }

    public static Matcher<Iterable<QueryLiteral>> literals(Object... values)
    {
        final List<Matcher<? super QueryLiteral>> list = new ArrayList<Matcher<? super QueryLiteral>>(values.length);
        for (Object value : values)
        {
            list.add(literal(value));
        }
        return contains(list);
    }

    public static Matcher<Iterable<QueryLiteral>> literals(List<?> values)
    {
        final List<Matcher<? super QueryLiteral>> list = new ArrayList<Matcher<? super QueryLiteral>>(values.size());
        for (Object value : values)
        {
            list.add(literal(value));
        }
        return contains(list);
    }
}
