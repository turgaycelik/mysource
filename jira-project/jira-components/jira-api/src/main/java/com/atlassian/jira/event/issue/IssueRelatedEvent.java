package com.atlassian.jira.event.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.event.JiraEvent;
import com.atlassian.jira.issue.Issue;

/**
 * This interface should be implemented by events that are related to the Issue.
 *
 * @since v5.2.6
 */
@PublicApi
public interface IssueRelatedEvent extends JiraEvent
{
    Issue getIssue();
}
