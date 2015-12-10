package com.atlassian.jira.bc.issue.vote;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.vote.VoteHistoryEntry;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Collection;
import java.util.List;

/**
 * Vote Service used to add and remove votes from a particular issue. This service's methods
 * will make sure that when voting, all of JIRA's business rules are enforced. This means
 * that permissions and data validation will be checked.
 *
 * @since v4.1
 */
@PublicApi
public interface VoteService
{
    /**
     * Validates if the user currently logged in may add a vote by the user supplied for the issue provided.
     * The remoteUser and user arguments may be the same if the current user is voting him/herself.  The
     * {@link com.atlassian.jira.bc.issue.vote.VoteService.VoteValidationResult} produced by this method should be
     * used in conjunction with the {@link #addVote(User, VoteValidationResult)}
     * method.
     * The following conditions will fail validation:
     * <ul>
     * <li>remoteUser is null (i.e. not logged in)</li>
     * <li>the user is the reporter of the issue</li>
     * <li>the issue has already been resolved</li>
     * <li>voting has been disabled system wide</li>
     * <li>the user doesn't have permission to browse the issue</li>
     * </ul>
     *
     * @param remoteUser The currently logged in user
     * @param user The user for whom a vote is being added
     * @param issue The issue being voted on
     * @return A validation result containing all errors, as well as the user and issue being voted on
     */
    VoteValidationResult validateAddVote(final User remoteUser, final User user, final Issue issue);

    /**
     * Adds a new vote using the specified validation result.
     * The vote will not be added if the user already voted for the issue.
     *
     * @param remoteUser The currently logged in user
     * @param validationResult Validation result for adding a new vote
     * @return The updated number of votes for the issue being voted for
     */
    int addVote(final User remoteUser, final VoteValidationResult validationResult);

    /**
     * Validates if the user currently logged in may remove a vote for the user supplied for the issue provided.
     * The remoteUser and user arguments may be the same if the current user is voting him/herself.  The
     * {@link com.atlassian.jira.bc.issue.vote.VoteService.VoteValidationResult} produced by this method should be
     * used in conjunction with the {@link #removeVote(User, VoteValidationResult)}
     * method.
     * The following conditions will fail validation:
     * <ul>
     * <li>remoteUser is null (i.e. not logged in)</li>
     * <li>the user is the reporter of the issue</li>
     * <li>the issue has already been resolved</li>
     * <li>voting has been disabled system wide</li>
     * <li>the user doesn't have permission to browse the issue</li>
     * </ul>
     *
     * @param remoteUser The currently logged in user
     * @param user The user for whom a vote is being removed
     * @param issue The issue being voted on
     * @return A validation result containing all errors, as well as the user and issue being voted on
     */
    VoteValidationResult validateRemoveVote(final User remoteUser, final User user, final Issue issue);

    /**
     * Removes a new vote using the specified validation result.
     * The vote will not be removed if the user didn't voted for the issue.
     *
     * @param remoteUser The currently logged in user
     * @param validationResult Validation result for removing a new vote
     * @return The updated number of votes for the issue being voted for
     */
    int removeVote(final User remoteUser, final VoteValidationResult validationResult);

    /**
     * Get the list of all users who have voted on an issue.
     * @param issue the issue to view
     * @param remoteUser the user who wants to know
     * @return a service outcome. if valid you can obtain the users that have voted on the.
     */
    ServiceOutcome<Collection<User>> viewVoters(final Issue issue, final User remoteUser);

    /**
     * Get the list of vote history for an issue.
     * The history will be in time sequence.
     * @param issue the issue to view
     * @param remoteUser the user who wants to know
     * @return a service outcome. if valid you can obtain the users that have voted on the.
     */
    ServiceOutcome<List<VoteHistoryEntry>> getVoterHistory(final Issue issue, final User remoteUser);

    /**
     * Retrieves the 'jira.option.voting' property
     *
     * @return True if voting is enabled
     */
    boolean isVotingEnabled();

    /**
     * Find if the specified user has voted on an issue
     * @param issue the issue to check
     * @param user the user to check
     * @return true if the user has voted on the issue
     */
    boolean hasVoted(final Issue issue, final User user);

    /**
     * A simple validation result that holds the user trying to vote, and the issue being voted on.
     */
    @PublicApi
    public static class VoteValidationResult extends ServiceResultImpl
    {
        private final User voter;
        private final Issue issue;

        public VoteValidationResult(final ErrorCollection errors, final User voter, final Issue issue)
        {
            super(errors);
            this.voter = voter;
            this.issue = issue;
        }

        public Issue getIssue()
        {
            return issue;
        }

        public User getVoter()
        {
            return voter;
        }
    }
}
