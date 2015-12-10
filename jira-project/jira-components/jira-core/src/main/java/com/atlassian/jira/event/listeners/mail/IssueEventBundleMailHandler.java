package com.atlassian.jira.event.listeners.mail;

import javax.annotation.Nonnull;

import com.atlassian.jira.event.issue.IssueEventBundle;

/**
 * Interface for classes that are able to handle {@link IssueEventBundle} events, sending email notifications to the appropriate users.
 */
public interface IssueEventBundleMailHandler
{
    /**
     * Called when an {@link IssueEventBundle} is dispatched.
     * @param issueEventBundle An object bundling several changes over an issue.
     * @see {@link IssueEventBundle}
     */
    void handle(@Nonnull final IssueEventBundle issueEventBundle);
}
