package com.atlassian.jira.util.concurrent;

import java.util.concurrent.ThreadFactory;

public class ThreadFactories
{
    public static ThreadFactory namedThreadFactory(final String name)
    {
        return new DefaultThreadFactory(name);
    }

    private ThreadFactories()
    {
        throw new AssertionError("cannot instantiate!");
    }
}
