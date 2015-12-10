package com.atlassian.jira.index;

public class MockIndex implements CloseableIndex
{
    public void close()
    {
        throw new UnsupportedOperationException();
    }

    public Result perform(final Operation operation)
    {
        throw new UnsupportedOperationException();
    }
}
