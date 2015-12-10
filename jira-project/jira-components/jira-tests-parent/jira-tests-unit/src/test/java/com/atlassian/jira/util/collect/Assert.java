package com.atlassian.jira.util.collect;

class Assert extends junit.framework.Assert
{
    static void assertUnsupportedOperation(final Runnable runnable)
    {
        try
        {
            runnable.run();
            fail("UnsupportedOperationException expected");
        }
        catch (final UnsupportedOperationException expected)
        {}
    }

    private Assert()
    {}
}
