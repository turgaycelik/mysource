package com.atlassian.jira.util;

import com.atlassian.annotations.PublicApi;

import javax.annotation.concurrent.Immutable;

/**
 * Evaluate an input and return true or false. Useful for filtering.
 */
@Immutable
@PublicApi
public interface Predicate<T>
{
    boolean evaluate(T input);
}
