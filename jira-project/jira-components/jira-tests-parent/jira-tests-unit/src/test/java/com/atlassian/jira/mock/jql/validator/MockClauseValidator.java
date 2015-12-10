package com.atlassian.jira.mock.jql.validator;

import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;

/**
 * Simple mock implementation of {@link com.atlassian.jira.jql.validator.ClauseValidator} for testing.
 *
 * @since v4.0
 */
public class MockClauseValidator implements ClauseValidator
{
    private static final AtomicInteger COUNT = new AtomicInteger();

    private int count;

    public MockClauseValidator()
    {
        count = COUNT.getAndIncrement();
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
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
        if (!(o instanceof MockClauseValidator))
        {
            return false;
        }

        final MockClauseValidator that = (MockClauseValidator) o;

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
        return String.format("Mock Query Validator %d.", count);
    }
}
