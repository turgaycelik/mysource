package com.atlassian.jira.event.config;

import com.atlassian.jira.issue.issuetype.IssueType;

/**
 * Abstract event that captures the data relevant to issue type events
 *
 * @since v5.1
 */
public class AbstractIssueTypeEvent
{
    private String id;
    private String issueTypeStyle;

    public AbstractIssueTypeEvent(IssueType issueType, String issueTypeStyle)
    {
        if (null != issueType)
        {
            this.id = issueType.getId();
            this.issueTypeStyle = issueTypeStyle;
        }
    }

    public String getId()
    {
        return id;
    }

    public String getIssueTypeStyle()
    {
        return issueTypeStyle;
    }
}
