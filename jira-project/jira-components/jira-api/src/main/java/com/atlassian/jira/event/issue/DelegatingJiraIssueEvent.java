package com.atlassian.jira.event.issue;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Represents objects that can offer a view of themselves as an {@link com.atlassian.jira.event.issue.IssueEvent}. This
 * interface should be implemented by classes that represent events related to issues but do not inherit from {@link
 * com.atlassian.jira.event.issue.IssueEvent}.
 *
 * @since 6.3.10
 */
@ExperimentalApi
public interface DelegatingJiraIssueEvent extends JiraIssueEvent
{
    /**
     * Returns the {@link com.atlassian.jira.event.issue.IssueEvent} equivalent to the object on which this method is
     * called.
     *
     * @return An IssueEvent. It can't be null.
     */
    @Nonnull
    IssueEvent asIssueEvent();
}
