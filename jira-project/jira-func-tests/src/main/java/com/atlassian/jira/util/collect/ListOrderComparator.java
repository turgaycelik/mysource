package com.atlassian.jira.util.collect;

import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.Comparator;
import java.util.List;

/**
 * Compare objects based on their order in a supplied list. Fails if the compared objects are not in the source list.
 * 
 * @since v3.13
 */
public class ListOrderComparator<T> implements Comparator<T>
{
    private final Resolver<T, Integer> resolver;

    public ListOrderComparator(final List<T> list)
    {
        Assertions.notNull("list", list);
        resolver = new Resolver<T, Integer>()
        {
            public Integer get(final T descriptor)
            {
                final int result = list.indexOf(descriptor);
                Assertions.not("unknown element: index < 0", result < 0);
                return result;
            }
        };
    }

    public int compare(final T o1, final T o2)
    {
        return resolver.get(o1).compareTo(resolver.get(o2));
    }
}
