package com.atlassian.jira.memoryinspector;

/**
  * @since v6.3
 */
public interface ThreadsProvider
{
    Iterable<Thread> getAllThreads();
}
