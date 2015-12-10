/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.vote;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * The vote manager is responsible for counting issue votes.
 */
@PublicApi
public interface VoteManager
{
    /**
     * Adds a new vote for the user and issue specified.
     *
     * @param user the User
     * @param issue the Issue
     * @return True if the vote succeeded. False if the user or issue supplied were null, or if the resolution is set or if voting is disabled.
     *
     * @deprecated Use {@link #addVote(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    boolean addVote(User user, Issue issue);
    /**
     * Adds a new vote for the user and issue specified.
     *
     * @param user the User
     * @param issue the Issue
     * @return True if the vote succeeded. False if the user or issue supplied were null, or if the resolution is set or if voting is disabled.
     */
    boolean addVote(ApplicationUser user, Issue issue);

    /**
     * Adds a new vote for the user and issue specified.
     *
     * @param user the User
     * @param issue the Issue
     * @return false, if the user or issue supplied were null, or if the resolution is set or if voting is disabled.  True if the vote succeeded.
     *
     * @deprecated Use {@link #addVote(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    boolean addVote(User user, GenericValue issue);

    /**
     * Removes a vote for the user and issue specified.
     *
     * @param user the User
     * @param issue the Issue
     * @return true if removing a vote succeeded.
     *
     * @deprecated Use {@link #removeVote(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    boolean removeVote(User user, Issue issue);

    /**
     * Removes a vote for the user and issue specified.
     *
     * @param user the User
     * @param issue the Issue
     * @return true if removing a vote succeeded.
     */
    boolean removeVote(ApplicationUser user, Issue issue);

    /**
     * Removes a vote for the user and issue specified.
     *
     * @param user the User
     * @param issue the Issue
     * @return false, if the user or issue supplied were null, or if the resolution is set or if voting is disabled.  True if removing a vote succeeded.
     *
     * @deprecated Use {@link #removeVote(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    boolean removeVote(User user, GenericValue issue);

    /**
     * Returns an ordered list of voters for a particular issue.
     *
     * @param issue the Issue
     * @param usersLocale the locale of the user making this call which is used to sort the results.
     * @return an ordered list of voters for a particular issue.
     * @since v4.3
     *
     * @deprecated Use {@link #getVotersFor(com.atlassian.jira.issue.Issue, java.util.Locale)} instead. Since v6.0.
     */
    List<User> getVoters(final Issue issue, final Locale usersLocale);

    /**
     * Returns an ordered list of voters for a particular issue.
     *
     * @param issue the Issue
     * @param usersLocale the locale of the user making this call which is used to sort the results.
     * @return an ordered list of voters for the given issue.
     * @since v6.0
     */
    List<ApplicationUser> getVotersFor(final Issue issue, final Locale usersLocale);

    /**
     * Return a collection of usernames of users that voted for the given issue.
     *
     * @param issue the Issue
     * @return a collection of usernames, never null
     * @since v4.3
     *
     * @deprecated Use {@link #getVoterUserkeys(com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    Collection<String> getVoterUsernames(Issue issue);

    /**
     * Return a collection of usernames of users that voted for given issue
     *
     * @param issue issue voted for
     * @return a collection of usernames, never null
     * @since v3.13
     *
     * @deprecated Use {@link #getVoterUsernames(com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    Collection<String> getVoterUsernames(GenericValue issue);

    /**
     * Return a collection of userkeys of users that voted for given issue
     *
     * @param issue issue voted for
     * @return a collection of userkeys, never null
     * @since v6.0
     */
    Collection<String> getVoterUserkeys(Issue issue);

    /**
     * Return the number of users that have voted for the given issue.
     *
     * @param issue issue voted for
     * @return the number of users that have voted for the given issue.
     * @since v6.0
     */
    int getVoteCount(Issue issue);

    /**
     * Get the list of vote history for an issue.
     * The history will be in time sequence.
     * @param issue the issue to view
     * @return List of Vote History Entries
     */
    List<VoteHistoryEntry> getVoteHistory(final Issue issue);

    /**
     * Retrieves the 'jira.option.voting' property
     *
     * @return True if voting is enabled
     */
    boolean isVotingEnabled();

    /**
     * Checks if the given User has voted on the given Issue.
     *
     * @param user the User
     * @param issue the Issue
     * @return True if the user has voted.
     *
     * @deprecated Use {@link #hasVoted(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    boolean hasVoted(User user, Issue issue);

    /**
     * Checks if the given User has voted on the given Issue.
     *
     * @param user the User
     * @param issue the Issue
     * @return True if the user has voted.
     */
    boolean hasVoted(ApplicationUser user, Issue issue);

    /**
     * Checks if the given User has voted on the given Issue.
     * the user supplied.
     *
     * @param user the User
     * @param issue the Issue
     * @return True if the user has voted.
     *
     * @deprecated Use {@link #hasVoted(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    boolean hasVoted(User user, GenericValue issue);

    /**
     * Removes all votes made by user.  Usually means user is being deleted.
     *
     * @param user user to remove vote associations for.
     * @since v3.13
     *
     * @deprecated Use {@link #removeVotesForUser(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     */
    void removeVotesForUser(final User user);

    /**
     * Removes all votes made by user.  Usually means user is being deleted.
     *
     * @param user user to remove vote associations for.
     * @since v6.0
     */
    void removeVotesForUser(final ApplicationUser user);
}
