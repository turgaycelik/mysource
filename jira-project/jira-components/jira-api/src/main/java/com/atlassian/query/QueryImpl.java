package com.atlassian.query;

import com.atlassian.annotations.Internal;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;

/**
 * Defines a structured graph of objects that can be used to represent query.
 *
 * @since v4.0
 */
@Internal
public class QueryImpl implements Query
{
    private final Clause whereClause;
    private final OrderBy orderByClause;
    private final String queryString;

    public QueryImpl()
    {
        this (null, null, null);
    }

    public QueryImpl(Clause whereClause)
    {
        this (whereClause, new OrderByImpl(), null);
    }

    public QueryImpl(Clause whereClause, String originalQuery)
    {
        this (whereClause, new OrderByImpl(), originalQuery);
    }

    public QueryImpl(Clause whereClause, OrderBy orderByClause, String originalQuery)
    {
        this.whereClause = whereClause;
        this.queryString = originalQuery;
        this.orderByClause = orderByClause;
    }

    public Clause getWhereClause()
    {
        return whereClause;
    }

    public OrderBy getOrderByClause()
    {
        return orderByClause;
    }

    public String getQueryString()
    {
        return queryString;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        if (whereClause != null)
        {
            builder.append(whereClause.toString());
        }

        if (orderByClause != null && !orderByClause.getSearchSorts().isEmpty())
        {
            if (builder.length() > 0)
            {
                builder.append(" ");
            }
            builder.append(orderByClause.toString());
        }

        return builder.toString();
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

        final QueryImpl that = (QueryImpl) o;

        if (orderByClause != null ? !orderByClause.equals(that.orderByClause) : that.orderByClause != null)
        {
            return false;
        }
        if (queryString != null ? !queryString.equals(that.queryString) : that.queryString != null)
        {
            return false;
        }
        if (whereClause != null ? !whereClause.equals(that.whereClause) : that.whereClause != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = whereClause != null ? whereClause.hashCode() : 0;
        result = 31 * result + (orderByClause != null ? orderByClause.hashCode() : 0);
        result = 31 * result + (queryString != null ? queryString.hashCode() : 0);
        return result;
    }
}
