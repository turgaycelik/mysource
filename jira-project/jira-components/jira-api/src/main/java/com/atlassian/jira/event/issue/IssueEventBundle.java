package com.atlassian.jira.event.issue;

import java.util.Collection;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Represents a list of changes that have been applied to an issue at once.
 * <p/>
 * For example, the action of a user updating an issue changing its assignee and adding a comment can be represented by
 * an {@link IssueEventBundle} that encapsulates an object representing the assignee changed event and another
 * representing the comment added event.
 *
 * @since 6.3.10
 */
@ExperimentalApi
public interface IssueEventBundle
{
    Collection<JiraIssueEvent> getEvents();

    boolean doesSendEmailNotification();
}
