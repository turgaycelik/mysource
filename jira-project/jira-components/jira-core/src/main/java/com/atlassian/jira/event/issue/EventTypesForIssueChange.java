package com.atlassian.jira.event.issue;

import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.util.IssueUpdateBean;

/**
 * Responsible of knowing what events should be raised when an issue is changed.
 */
public interface EventTypesForIssueChange
{
    /**
     * Given an IssueUpdateBean representing the collection of changes over an issue, returns a list
     * of events that should be triggered for all of those changes.
     * @param issueUpdateBean An object encapsulating the changes on an issue.
     * @return A list containing the ids of all the {@link EventType} that should be triggered due to the changes on the issue.
     */
    @Nonnull
    List<Long> getEventTypeIdsForIssueUpdate(@Nonnull IssueUpdateBean issueUpdateBean);
}
