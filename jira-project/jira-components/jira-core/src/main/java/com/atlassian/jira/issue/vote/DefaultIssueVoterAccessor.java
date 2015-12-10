package com.atlassian.jira.issue.vote;

import java.util.Locale;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultIssueVoterAccessor implements IssueVoterAccessor
{
    private final VoteManager voteManager;

    public DefaultIssueVoterAccessor(final VoteManager voteManager)
    {
        this.voteManager = notNull("voteManager", voteManager);
    }

    @Override
    public Iterable<User> getVoters(@Nonnull Locale displayLocale, @Nonnull Issue issue)
    {
        return voteManager.getVoters(issue, displayLocale);
    }

    @Override
    public boolean isVotingEnabled()
    {
        return voteManager.isVotingEnabled();
    }

    @Override
    public Iterable<String> getVoterNames(final @Nonnull Issue issue)
    {
        return voteManager.getVoterUsernames(issue.getGenericValue());
    }

    @Override
    public Iterable<String> getVoterUserkeys(@Nonnull Issue issue)
    {
        return voteManager.getVoterUserkeys(issue);
    }
}
