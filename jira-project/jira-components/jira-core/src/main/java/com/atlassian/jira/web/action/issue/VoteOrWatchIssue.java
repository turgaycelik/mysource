package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.issue.vote.VoteService;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;

/**
 * Action to add or remove votes and watches to a particular issue.
 *
 * @since v4.1
 */
public class VoteOrWatchIssue extends AbstractIssueSelectAction
{
    private final VoteService voteService;
    private final WatcherManager watcherManager;

    private String watch;
    private String vote;

    public VoteOrWatchIssue(final VoteService voteService, final WatcherManager watcherManager)
    {
        this.voteService = voteService;
        this.watcherManager = watcherManager;
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            if (getLoggedInUser() == null)
            {
                return PERMISSION_VIOLATION_RESULT;
            }

            if ("vote".equalsIgnoreCase(vote))
            {
                final VoteService.VoteValidationResult validationResult = voteService.validateAddVote(getLoggedInUser(), getLoggedInUser(), getIssueObject());
                if (validationResult.isValid())
                {
                    voteService.addVote(getLoggedInUser(), validationResult);
                }
                else
                {
                    addErrorCollection(validationResult.getErrorCollection());
                }
            }
            else if ("unvote".equalsIgnoreCase(vote))
            {
                final VoteService.VoteValidationResult validationResult = voteService.validateRemoveVote(getLoggedInUser(), getLoggedInUser(), getIssueObject());
                if (validationResult.isValid())
                {
                    voteService.removeVote(getLoggedInUser(), validationResult);
                }
                else
                {
                    addErrorCollection(validationResult.getErrorCollection());
                }
            }

            if ("watch".equalsIgnoreCase(watch))
            {
                watcherManager.startWatching(getLoggedInUser(), getIssue());
            }
            else if ("unwatch".equalsIgnoreCase(watch))
            {
                watcherManager.stopWatching(getLoggedInUser(), getIssue());
            }
            return returnComplete("/browse/" + getIssueObject().getKey());
        }
        catch (final IssueNotFoundException ex)
        {
            addErrorMessage(getText("admin.errors.issues.issue.does.not.exist"));
            return ISSUE_NOT_FOUND_RESULT;
        }
        catch (final IssuePermissionException ex)
        {
            addErrorMessage(getText("admin.errors.issues.no.browse.permission"));
            return PERMISSION_VIOLATION_RESULT;
        }
    }

    public String getVote()
    {
        return vote;
    }

    public String getWatch()
    {
        return watch;
    }

    public void setVote(final String vote)
    {
        this.vote = vote;
    }

    public void setWatch(final String watch)
    {
        this.watch = watch;
    }
}
