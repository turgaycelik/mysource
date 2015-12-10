package com.atlassian.jira.util;

/**
 * Evaluate an input and return true or false. Useful for filtering.
 *
 * @deprecated use Guava predicates and functions
 */
@Deprecated
public interface Predicate<T>
{
    boolean evaluate(T input);
}
