/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.listeners.search;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.AbstractIssueEventListener;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This listener updates the search index within JIRA.
 * <p>
 * Do not delete ;)
 */
public class IssueIndexListener extends AbstractIssueEventListener
{
    private static final Logger log = org.apache.log4j.Logger.getLogger(IssueIndexListener.class);
    public static final String NAME = "Issue Index Listener";

    @Override
    public void init(final Map params)
    {}

    @Override
    public String[] getAcceptedParams()
    {
        return new String[0];
    }

    /**
     * Should not be deleted - required for JIRA operation
     */
    @Override
    public boolean isInternal()
    {
        return true;
    }

    @Override
    public void issueCreated(final IssueEvent event)
    {
        reIndexModifiedIssue(event);
    }

    @Override
    public void issueUpdated(final IssueEvent event)
    {
        reIndexModifiedIssue(event);
    }

    @Override
    public void issueAssigned(final IssueEvent event)
    {
        reIndexModifiedIssue(event);
    }

    @Override
    public void issueResolved(final IssueEvent event)
    {
        reIndexModifiedIssue(event);
    }

    @Override
    public void issueClosed(final IssueEvent event)
    {
        reIndexModifiedIssue(event);
    }

    @Override
    public void issueCommented(final IssueEvent event)
    {
        // For a comment we need to update the issue because the last updated date is changed, but this does not effect the change history
        reIndex(event, false, false);
        if (event.getComment() != null)
        {
            reIndexComment(event.getComment());
        }
    }

    @Override
    public void issueWorkLogged(final IssueEvent event)
    {
        reIndexModifiedIssue(event);
    }

    @Override
    public void issueReopened(final IssueEvent event)
    {
        reIndexModifiedIssue(event);
    }

    @Override
    public void issueGenericEvent(final IssueEvent event)
    {
        reIndexModifiedIssue(event);
    }

    @Override
    public void issueCommentEdited(final IssueEvent event)
    {
        // For a comment we need to update the issue because the last updated date is changed, but this does not effect the change history
        reIndex(event, false, false);
        if (event.getComment() != null)
        {
            reIndexComment(event.getComment());
        }
    }

    @Override
    public void issueCommentDeleted(final IssueEvent event)
    {
        // For a comment we need to update the issue because the last updated date is changed, but this does not effect the change history
        // We also need to reindex all the comments because the event does not contain the deleted comment
        reIndex(event, true, false);
    }

    @Override
    public void issueWorklogUpdated(final IssueEvent event)
    {
        reIndexModifiedIssue(event);
    }

    @Override
    public void issueWorklogDeleted(final IssueEvent event)
    {
        reIndexModifiedIssue(event);
    }

    @Override
    public void issueDeleted(final IssueEvent event)
    {}

    @Override
    public void issueMoved(final IssueEvent event)
    {
        reIndex(event, true, true);
    }

    @Override
    public void customEvent(final IssueEvent event)
    {}

    private static void reIndex(final IssueEvent issueEvent, boolean reIndexComments, boolean reIndexChangeHistory)
    {
        final Set issuesToReindex = new HashSet();
        final Issue issue = issueEvent.getIssue();
        issuesToReindex.add(issue);

        //if there are any subtasks that were modified as part of an issue operation (e.g. editing a parent issue's
        //security level) ensure that they are also re-indexed.
        if (issueEvent.isSubtasksUpdated())
        {
            issuesToReindex.addAll(issue.getSubTaskObjects());
        }
        try
        {
            ComponentAccessor.getIssueIndexManager().reIndexIssueObjects(issuesToReindex, reIndexComments, reIndexChangeHistory);
        }
        catch (final Exception issueReindexException) //catch everything here to make sure it doesn't bring server down.
        {
            log.error("Error re-indexing changes for issue '" + issue.getKey() + "'", issueReindexException);
        }
    }

    private static void reIndexModifiedIssue(IssueEvent event)
    {
        reIndex(event, false, true);
        if (event.getComment() != null)
        {
            reIndexComment(event.getComment());
        }
    }

    private static void reIndexComment(final Comment comment)
    {
        try
        {
            ComponentAccessor.getIssueIndexManager().reIndexComments(Collections.singletonList(comment));
        }
        catch (final Exception commentReindexException) //catch everything here to make sure it doesn't bring server down.
        {
            log.error("Error re-indexing comment '" + comment.getId() + "'", commentReindexException);
        }
    }

    /**
     * As we wish to only have one IssueIndexListener at a time - enforce uniqueness
     */
    @Override
    public boolean isUnique()
    {
        return true;
    }

    @Override
    public String getDescription()
    {
        return getI18NBean().getText("admin.listener.issue.index.desc");
    }
}
