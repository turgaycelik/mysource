package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.collect.CloseableIterator;

public interface IssueIterator extends CloseableIterator<Issue>
{
    /**
     * @deprecated use @{#next()}
     */
    @Deprecated
    Issue nextIssue();

    void close();
}
