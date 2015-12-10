package com.atlassian.jira.index;

import java.io.IOException;

public class MockCloseableIndex implements CloseableIndex
{
    public Result perform(final Operation op)
    {
        try
        {
            op.perform(null);
        }
        catch (final IOException e)
        {
            ///CLOVER:OFF
            throw new RuntimeException(e);
            ///CLOVER:ON
        }
        return new MockResult();
    }

    public void close()
    {}
}
