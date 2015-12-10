package com.atlassian.jira.index;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

///CLOVER:OFF
public class MockResult implements Index.Result
{
    public void await()
    {}

    public boolean await(final long timeout, final TimeUnit unit)
    {
        await();
        return true;
    }

    public boolean isDone()
    {
        return false;
    }
}
///CLOVER:ON

