package com.atlassian.jira.plugin.headernav;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class ContextProviderMatcher
{
    static Matcher<Object> emptyCollection()
    {
        return new TypeSafeMatcher<Object>()
        {
            @Override
            protected boolean matchesSafely(@Nullable final Object o)
            {
                if (o != null && Collection.class.isAssignableFrom(o.getClass()))
                {
                    final Collection<?> collection = (Collection<?>) o;
                    return collection.isEmpty();
                }
                return false;
            }

            @Override
            public void describeTo(@Nonnull final Description description)
            {
                description.appendText("an empty collection");
            }
        };
    }

    static Matcher<Object> emptyMap()
    {
        return new TypeSafeMatcher<Object>()
        {
            @Override
            protected boolean matchesSafely(@Nullable final Object o)
            {
                if (o != null && Map.class.isAssignableFrom(o.getClass()))
                {
                    final Map<?, ?> map = (Map<?, ?>) o;
                    return map.isEmpty();
                }
                return false;
            }

            @Override
            public void describeTo(@Nonnull final Description description)
            {
                description.appendText("an empty map");
            }
        };
    }

    static Matcher<Object> aListWithValues(@Nonnull final Object... values)
    {
        return new TypeSafeMatcher<Object>()
        {
            @Override
            protected boolean matchesSafely(@Nullable final Object o)
            {
                if (o != null && Collection.class.isAssignableFrom(o.getClass()))
                {
                    final Collection<?> iteratable = (Collection<?>) o;
                    return iteratable.size() == values.length && iteratable.containsAll(Arrays.asList(values));

                }
                return false;
            }

            @Override
            public void describeTo(@Nonnull final Description description)
            {
                description.appendText("a collection with values ").appendValue(values);
            }
        };
    }

    static Matcher<Object> aMapWithEntrySet(@Nonnull final Object expectedKey, @Nonnull final Object expectedValue)
    {
        return new TypeSafeMatcher<Object>()
        {
            @Override
            protected boolean matchesSafely(@Nullable final Object o)
            {
                if (o != null && Map.class.isAssignableFrom(o.getClass()))
                {
                    final Map<?, ?> map = (Map<?, ?>) o;
                    final Object value = map.get(expectedKey);
                    return map.size() == 1 && value != null && expectedValue.equals(value);
                }
                return false;
            }

            @Override
            public void describeTo(@Nonnull final Description description)
            {
                description.appendText("a map with key ").appendValue(expectedKey)
                        .appendText(" and value ").appendValue(expectedValue);
            }
        };
    }
}
