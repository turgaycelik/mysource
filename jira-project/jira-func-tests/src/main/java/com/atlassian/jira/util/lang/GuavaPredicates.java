package com.atlassian.jira.util.lang;

import com.google.common.base.Predicate;
import org.hamcrest.Matcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Utils for Google Guava predicates
 *
 * @since v5.0
 */
public class GuavaPredicates
{

    public static <T> Predicate<T> forMatcher(final @Nonnull Matcher<T> matcher)
    {
        return new Predicate<T>()
        {
            @Override
            public boolean apply(@Nullable T input)
            {
                return matcher.matches(input);
            }
        };
    }
}
