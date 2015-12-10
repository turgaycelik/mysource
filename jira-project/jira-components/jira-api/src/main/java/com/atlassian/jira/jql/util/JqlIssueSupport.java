package com.atlassian.jira.jql.util;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.lang.Pair;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Some helper IssueLookup functions for JIRA.
 *
 * @since v4.0
 */
@Internal
public interface JqlIssueSupport
{
    /**
     * Get the issue given its id if the passed user can see it. A null will be returned if the issue key is
     * not within JIRA or if the user does not have permission to see the issue.
     *
     * @param id   the id of the issue to retreieve. A null key is assumed not to exist within JIRA.
     * @param user the user who must have permission to see the issue.
     * @return the issue identified by the passed id if it can be seen by the passed user. A null value will be returned
     * if the issue does not exist or the user cannot see the issue.
     */
    Issue getIssue(long id, ApplicationUser user);

    /**
     * Get the issue given its id if the passed user can see it. A null will be returned if the issue key is
     * not within JIRA or if the user does not have permission to see the issue.
     *
     * @param id   the id of the issue to retreieve. A null key is assumed not to exist within JIRA.
     * @param user the user who must have permission to see the issue.
     * @return the issue identified by the passed id if it can be seen by the passed user. A null value will be returned
     * if the issue does not exist or the user cannot see the issue.
     * @deprecated Use {@link JqlIssueSupport#getIssue(long, com.atlassian.jira.user.ApplicationUser)} instead. Since v6.1.
     */
    @Deprecated
    Issue getIssue(long id, User user);

    /**
     * Get the issue given its id. A null will be returned if the issue is not within JIRA.
     *
     * @param id the id of the issue to retrieve.
     * @return the issue identified by the passed id. A null value will be returned if the issue does not exist.
     */
    Issue getIssue(long id);

    /**
     * Get the issue with the passed key if the passed user can see it.
     *
     * @param issueKey they key of the issue to retrieve. A null key is assumed not to exist within JIRA.
     * @param user     the user who must have permission to see the issue.
     * @return the issue identified by the passed key if it can be seen by the passed user.
     * Null is returned if the issue key is not within JIRA or if the user does not have
     * permission to see the issue.
     * @see #getIssue(String) for a version with no permission check
     */
    Issue getIssue(String issueKey, ApplicationUser user);

    /**
     * Get the issue with the passed key.
     *
     * @param issueKey they key of the issue to retrieve. A null key is assumed not to exist within JIRA.
     * @return the issue identified by the passed key. Null is returned if the issue key is not within JIRA.
     * @see #getIssue(String, ApplicationUser) for a version with permission checks
     */
    Issue getIssue(String issueKey);

    /**
     * Get the issues with the passed key if the passed user can see it. An empty list will be returned if the issue
     * key is not within JIRA or if the user does not have permission to see any of the issues.
     * Note: This no longer tries to do a case insensitive lookup
     *
     * @param issueKey they key of the issue to retrieve. A null key is assumed not to exist within JIRA.
     * @param user     the user who must have permission to see the issue.
     * @return the issues identified by the passed key if they can be seen by the passed user.
     * An empty list will be returned if the issue key is not within JIRA or if the user does not have
     * permission to see any of the issues.
     * @see #getIssues(String) for a version with no permission check
     * @deprecated Use {@link #getIssue(String, com.atlassian.jira.user.ApplicationUser)} instead. Since v6.1.
     */
    @Deprecated
    List<Issue> getIssues(String issueKey, User user);

    /**
     * Get the issues with the passed key. An empty list will be returned if the issue key is not within JIRA.
     * Note: This no longer tries to do a case insensitive lookup.
     *
     * @param issueKey they key of the issue to retreieve. A null key is assumed not to exist within JIRA.
     * @return the issues identified by the passed key. An empty list will be returned if the issue key is not within
     * JIRA.
     * @see #getIssues(String, User) for a version with permission checks
     * @deprecated Use {@link #getIssue(String)} instead. Since v6.1.
     */
    @Deprecated
    List<Issue> getIssues(String issueKey);

    /**
     * Returns a set of project ID / issue type combinations that given issue keys cover.
     * @param issueKeys Set of issue keys
     * @return Project ID / issue type pairs
     */
    @Internal
    @Nonnull
    Set<Pair<Long, String>> getProjectIssueTypePairsByKeys(@Nonnull final Set<String> issueKeys);

    /**
     * Returns a set of project ID / issue type combinations that given issue IDs cover.
     * @param issueIds Set of issue IDs
     * @return Project ID / issue type pairs
     */
    @Internal
    @Nonnull
    Set<Pair<Long, String>> getProjectIssueTypePairsByIds(@Nonnull final Set<Long> issueIds);

    /**
     * Check existence of issues for the given set of keys
     * @param issueKeys Set of issue keys
     * @return Set of invalid keys or the ones that don't represent an issue
     */
    @Internal
    @Nonnull
    Set<String> getKeysOfMissingIssues(@Nonnull final Set<String> issueKeys);

    /**
     * Check existence of issues for the given set of IDs
     * @param issueIds Set of issue IDs
     * @return Set of IDs that don't represent an issue
     */
    @Internal
    @Nonnull
    Set<Long> getIdsOfMissingIssues(@Nonnull final Set<Long> issueIds);
}
