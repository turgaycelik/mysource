package com.atlassian.jira.util.collect;

import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.Comparator;

/**
 * Comparator that first resolves an input to an output type, and then delegates to a comparator of the output type.
 *
 * @since v3.13
 */
public class ResolvingComparator<I, O> implements Comparator<I>
{
    private final Comparator<O> comparator;
    private final Resolver<I, O> resolver;

    public ResolvingComparator(final Resolver<I, O> resolver, final Comparator<O> comparator)
    {
        Assertions.notNull("resolver", resolver);
        Assertions.notNull("comparator", comparator);
        this.resolver = resolver;
        this.comparator = comparator;
    }

    public int compare(final I o1, final I o2)
    {
        return comparator.compare(resolver.get(o1), resolver.get(o2));
    }
}
