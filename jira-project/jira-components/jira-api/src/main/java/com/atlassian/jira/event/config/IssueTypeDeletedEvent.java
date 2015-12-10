package com.atlassian.jira.event.config;

import com.atlassian.jira.issue.issuetype.IssueType;

/**
 * Event indicating an issue type has been deleted
 *
 * @note As at 2012-01-27, it is not possible to change whether an issue type is a sub-task or not.
 *
 * @since v5.1
 */
public class IssueTypeDeletedEvent extends AbstractIssueTypeEvent
{
    public IssueTypeDeletedEvent(IssueType issueType, String issueStyleType)
    {
        super(issueType, issueStyleType);
    }
}
