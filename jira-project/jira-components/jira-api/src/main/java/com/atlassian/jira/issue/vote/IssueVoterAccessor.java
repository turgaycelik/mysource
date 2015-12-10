package com.atlassian.jira.issue.vote;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import javax.annotation.Nonnull;

import java.util.Locale;

/**
 * Get all voters for an issue.
 *
 * @since v4.1
 */
public interface IssueVoterAccessor
{
    boolean isVotingEnabled();

    /**
     * Convenience function that simply returns the User names.
     *
     * @param issue the issue to get the voters for
     * @return an Iterable of the user names, empty if no voters
     *
     * @deprecated Use {@link #getVoterUserkeys(com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    @Nonnull
    Iterable<String> getVoterNames(final @Nonnull Issue issue);

    /**
     * Returns the userkeys for voters on the given issue.
     *
     * @param issue the issue to get the voters for
     * @return an Iterable of the user keys, empty if no voters
     */
    @Nonnull
    Iterable<String> getVoterUserkeys(final @Nonnull Issue issue);

    /**
     * Convenience function that simply returns the User objects.
     *
     * @param displayLocale for sorting.
     * @param issue the issue to get the voters for
     * @return an Iterable of the users, empty if no voters
     */
    @Nonnull
    Iterable<User> getVoters(final @Nonnull Locale displayLocale, final @Nonnull Issue issue);
}
