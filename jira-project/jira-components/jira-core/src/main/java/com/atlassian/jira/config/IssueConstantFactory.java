package com.atlassian.jira.config;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import org.ofbiz.core.entity.GenericValue;

/**
 * Converts GV's of issues constants into their associated objects.
 *
 * @since v5.2
 */
public interface IssueConstantFactory
{
    Priority createPriority(GenericValue priorityGv);
    IssueType createIssueType(GenericValue issueTypeGv);
    Resolution createResolution(GenericValue resolutionGv);
    Status createStatus(GenericValue statusGv);
}
