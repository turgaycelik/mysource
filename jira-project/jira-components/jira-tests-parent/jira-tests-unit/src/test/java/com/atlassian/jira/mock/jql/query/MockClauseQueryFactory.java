package com.atlassian.jira.mock.jql.query;

import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.query.clause.TerminalClause;

/**
 * A mock implemenation of {@link com.atlassian.jira.jql.query.ClauseQueryFactory} for testsing.
 *
 * @since v4.0
 */
public class MockClauseQueryFactory implements ClauseQueryFactory
{
    private static final AtomicInteger COUNT = new AtomicInteger();

    private int count;

    public MockClauseQueryFactory()
    {
        count = COUNT.getAndIncrement();
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof MockClauseQueryFactory))
        {
            return false;
        }

        final MockClauseQueryFactory that = (MockClauseQueryFactory) o;

        if (count != that.count)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return count;
    }

    @Override
    public String toString()
    {
        return String.format("Mock Query Factory %d.", count);
    }
}
