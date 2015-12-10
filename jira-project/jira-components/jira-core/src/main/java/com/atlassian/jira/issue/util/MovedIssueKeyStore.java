package com.atlassian.jira.issue.util;

import java.util.Set;

/**
 * @since v6.1
 */
public interface MovedIssueKeyStore
{
    void recordMovedIssueKey(final String oldIssueKey, final Long oldIssueId);

    Long getMovedIssueId(final String key);

    Set<String> getMovedIssueKeys(final Set<String> key);

    void deleteMovedIssueKeyHistory(final Long issueId);
}
