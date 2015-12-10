package com.atlassian.jira.issue.util;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.collect.EnclosedIterable;

@PublicApi
public interface IssuesIterable extends EnclosedIterable<Issue>
{
    /**
     * Return a user friendly message that identifies which issues this iterable holds.
     * If there is a problem, this method is used to log what issues are affected.
     * <p/>
     * For example, if there is a problem locking the Lucene index this method is used to log
     * which issues could not be reindexed.
     * <p/>
     * Hence, it is important to provide a useful implementation for this method.
     */
    String toString();
}