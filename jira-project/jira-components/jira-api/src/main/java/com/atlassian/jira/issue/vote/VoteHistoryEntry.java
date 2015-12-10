package com.atlassian.jira.issue.vote;

import java.sql.Timestamp;

/**
 * This represents an entry in the vote history of an issue at a point in time.
 * Vote history entries are created whenever a vote is added or removed from an issue.
 *
 * @since v4.4
 */
public interface VoteHistoryEntry
{
    /** Get the issue Id. */
    public Long getIssueId();

    /** Get the Timestamp of this historical entry. */
    public Timestamp getTimestamp();

    /** Get the number of Votes recoreded for the issue at this moment in time. */
    public long getVotes();
}
