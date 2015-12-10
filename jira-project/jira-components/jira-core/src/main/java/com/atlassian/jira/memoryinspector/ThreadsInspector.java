package com.atlassian.jira.memoryinspector;

/**
 * @since v6.3
 */
public interface ThreadsInspector
{
    InspectionReport inspectThreads(final Iterable<Thread> threads);
}
