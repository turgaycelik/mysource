package com.atlassian.jira.jql.query;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Functions;
import com.atlassian.query.operator.Operator;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class RangeQueryFactory<T>
{
    static RangeQueryFactory<String> stringRangeQueryFactory()
    {
        return new RangeQueryFactory<String>(Functions.<String> identity());
    }

    private final Function<T, String> valueFactory;

    public RangeQueryFactory(final Function<T, String> valueFactory)
    {
        this.valueFactory = notNull("valueFactory", valueFactory);
    }

    Query get(final Operator operator, final String fieldName, final T value)
    {
        switch (operator)
        {
            case LESS_THAN:
                return handleLessThan(fieldName, value);
            case LESS_THAN_EQUALS:
                return handleLessThanEquals(fieldName, value);
            case GREATER_THAN:
                return handleGreaterThan(fieldName, value);
            case GREATER_THAN_EQUALS:
                return handleGreaterThanEquals(fieldName, value);
            default:
                // should not have gotten here
                throw new IllegalStateException("Unhandled Operator: " + operator);
        }
    }

    Query handleLessThan(final String fieldName, final T value)
    {
        return new TermRangeQuery(fieldName, null, valueFactory.get(value), true, false);
    }

    Query handleLessThanEquals(final String fieldName, final T value)
    {
        return new TermRangeQuery(fieldName, null, valueFactory.get(value), true, true);
    }

    Query handleGreaterThan(final String fieldName, final T value)
    {
        return new TermRangeQuery(fieldName, valueFactory.get(value), null, false, true);
    }

    Query handleGreaterThanEquals(final String fieldName, final T value)
    {
        return new TermRangeQuery(fieldName, valueFactory.get(value), null, true, true);
    }
}
