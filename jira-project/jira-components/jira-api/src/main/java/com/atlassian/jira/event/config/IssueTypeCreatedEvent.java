package com.atlassian.jira.event.config;

import com.atlassian.jira.issue.issuetype.IssueType;

/**
 * Event indicating an issue type has been created
 *
 * @since v5.1
 */
public class IssueTypeCreatedEvent extends AbstractIssueTypeEvent
{
    public IssueTypeCreatedEvent(IssueType issueType, String issueTypeStyle)
    {
        super(issueType, issueTypeStyle);
    }
}
