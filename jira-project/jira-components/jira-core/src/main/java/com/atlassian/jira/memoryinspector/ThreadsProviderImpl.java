package com.atlassian.jira.memoryinspector;

/**
 * @since v6.3
 */
public class ThreadsProviderImpl implements ThreadsProvider
{
    public Iterable<Thread> getAllThreads()
    {
        return Thread.getAllStackTraces().keySet();
    }
}
