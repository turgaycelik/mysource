package com.atlassian.jira.util;

import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Arrays;
import java.util.Collection;

/**
 * A predicate that checks that all the given predicates evaulate to true for the specified input.
 * It is fail fast.
 *
 * @since v4.0
 */
public class EvaluateAllPredicate<T> implements Predicate<T>
{
    private final Collection<Predicate<T>> predicates;

    public EvaluateAllPredicate(Predicate<T> first, Predicate<T>... predicates)
    {
        this.predicates = CollectionBuilder.newBuilder(first).addAll(Arrays.<Predicate<T>>asList(predicates)).asList();
    }

    public boolean evaluate(final T input)
    {
        for (Predicate<T> predicate : predicates)
        {
            if (!predicate.evaluate(input))
            {
                return false;
            }
        }
        return true;
    }
}
