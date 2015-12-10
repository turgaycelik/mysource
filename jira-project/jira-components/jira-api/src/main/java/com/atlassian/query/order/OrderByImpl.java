package com.atlassian.query.order;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.util.collect.CollectionUtil;
import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Joiner;

/**
 * Default implementation of {@link com.atlassian.query.order.OrderBy}.
 *
 * @since 4.0
 */
@Immutable
@Internal
public class OrderByImpl implements OrderBy
{
    public static final OrderByImpl NO_ORDER = new OrderByImpl(Collections.<SearchSort>emptyList());

    private final List<SearchSort> searchSorts;

    public OrderByImpl(final SearchSort ... searchSorts)
    {
        this(Arrays.asList(notNull("searchSorts", searchSorts)));
    }

    public OrderByImpl(final Collection<SearchSort> searchSorts)
    {
        this.searchSorts = CollectionUtil.copyAsImmutableList(containsNoNulls("searchSorts", searchSorts));
    }

    public List<SearchSort> getSearchSorts()
    {
        return searchSorts;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (!searchSorts.isEmpty())
        {
            sb.append("order by ");
        }
        Joiner.on(",").appendTo(sb,searchSorts);
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final OrderByImpl orderBy = (OrderByImpl) o;

        if (!searchSorts.equals(orderBy.searchSorts))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return searchSorts.hashCode();
    }
}
