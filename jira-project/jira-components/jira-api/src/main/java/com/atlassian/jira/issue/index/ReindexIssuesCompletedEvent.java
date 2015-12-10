package com.atlassian.jira.issue.index;

/**
* Event raised when indexing a set of issues has completed.
* @since v5.0
*/
public class ReindexIssuesCompletedEvent
{
    /**
     * The amount of time it took to reindex the issues.
     */
    final public long time;

    public ReindexIssuesCompletedEvent(long time)
    {
        this.time = time;
    }
}
