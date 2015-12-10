package com.atlassian.jira.issue.index;

import com.atlassian.jira.event.issue.AbstractIssueEventListener;
import com.atlassian.jira.event.issue.IssueEvent;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This Class listens for Issue updates and deletes that happen during a background reindex so that they can be
 * re-validated to ensure the index is not corrupted by any race between the background reindex and the issue updater.
 *
 * We don't need to worry about new created issues as we can't generate a race against
 * an issue that didn't have a previous state.
 * @since v6.2
 */
public class BackgroundIndexListener extends AbstractIssueEventListener
{
    private final Set<Long> updatedIssues = new HashSet<Long>();
    private final Set<Long> deletedIssues = new HashSet<Long>();

    @Override
    public void issueUpdated(final IssueEvent event)
    {
        recordUpdated(event);
    }

    @Override
    public void issueAssigned(final IssueEvent event)
    {
        recordUpdated(event);
    }

    @Override
    public void issueResolved(final IssueEvent event)
    {
        recordUpdated(event);
    }

    @Override
    public void issueClosed(final IssueEvent event)
    {
        recordUpdated(event);
    }

    @Override
    public void issueCommented(final IssueEvent event)
    {
        recordUpdated(event);
    }

    @Override
    public void issueCommentEdited(final IssueEvent event)
    {
        recordUpdated(event);
    }

    @Override
    public void issueCommentDeleted(final IssueEvent event)
    {
        recordUpdated(event);
    }

    @Override
    public void issueWorklogUpdated(final IssueEvent event)
    {
        recordUpdated(event);
    }

    @Override
    public void issueWorklogDeleted(final IssueEvent event)
    {
        recordUpdated(event);
    }

    @Override
    public void issueReopened(final IssueEvent event)
    {
        recordUpdated(event);
    }

    @Override
    public void issueDeleted(final IssueEvent event)
    {
        recordDeleted(event);
    }

    @Override
    public void issueWorkLogged(final IssueEvent event)
    {
        recordUpdated(event);
    }

    @Override
    public void issueMoved(final IssueEvent event)
    {
        recordUpdated(event);
    }

    public Set<Long> getUpdatedIssues()
    {
        return Collections.unmodifiableSet(updatedIssues);
    }

    public Set<Long> getDeletedIssues()
    {
        return Collections.unmodifiableSet(deletedIssues);
    }

    public int getTotalModifications()
    {
        return updatedIssues.size() + deletedIssues.size();
    }

    private boolean recordUpdated(final IssueEvent event)
    {
        return updatedIssues.add(event.getIssue().getId());
    }

    private boolean recordDeleted(final IssueEvent event)
    {
        return deletedIssues.add(event.getIssue().getId());
    }
}
