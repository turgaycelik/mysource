package com.atlassian.jira.issue.vote;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.exception.DataAccessException;

import java.util.List;

/**
 * Persistent storage mechanism for {@link com.atlassian.jira.avatar.AvatarImpl}.
 *
 * @since v4.0
 */
public interface VoteHistoryStore
{

    /**
     * Retrieve the vote history for an issue
     *
     * @param issueId the issue Id, must not be null.
     * @return the vote history for an issue
     * @throws com.atlassian.jira.exception.DataAccessException if there is a back-end storage problem.
     */
    public List<VoteHistoryEntry> getHistory(Long issueId) throws DataAccessException;

    /**
     * Remove all the vote history for an issue.
     *
     * @param issueId the issue id, must not be null.
     * @throws com.atlassian.jira.exception.DataAccessException if there is a back-end storage problem.
     */
    public void delete(String issueId) throws DataAccessException;

    /**
     * Creates an issue history entry
     *
     * @param entry a vote history entry for an issue.
     * @throws com.atlassian.jira.exception.DataAccessException if there is a back-end storage problem.
     */
    public void add(VoteHistoryEntry entry) throws DataAccessException;

}
