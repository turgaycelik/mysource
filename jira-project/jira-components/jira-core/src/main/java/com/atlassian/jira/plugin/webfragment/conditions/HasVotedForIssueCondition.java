package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

/**
 * Condition to check whether the current user has voted for the current issue
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.1
 */
public class HasVotedForIssueCondition extends AbstractIssueCondition
{
    private final VoteManager voteManager;

    public HasVotedForIssueCondition(VoteManager voteManager)
    {
        this.voteManager = voteManager;
    }

    public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
    {
        return user != null && voteManager.hasVoted(user, issue);
    }
}