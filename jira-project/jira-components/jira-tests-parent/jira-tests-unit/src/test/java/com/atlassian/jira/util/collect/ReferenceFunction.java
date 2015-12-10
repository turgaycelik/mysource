package com.atlassian.jira.util.collect;

import com.atlassian.jira.util.Function;

/**
 * Simple pass through function implementation that returns the input.
 */
public class ReferenceFunction<T> implements Function<T, T>
{
    public T get(final T input)
    {
        return input;
    };
}
