package com.atlassian.jira.issue.vote;

import java.sql.Timestamp;
import java.util.Date;

/**
 * This represents an entry in the vote history of an issue at a point in time.
 * Vote history entries are created whenever a vote is added or removed from an issue.
 *
 * @since v4.4
 */
public class VoteHistoryEntryImpl implements VoteHistoryEntry
{
    /** The issue Key. */
    final private Long issueId;
    /** Timestamp of this historical entry. */
    final private Timestamp timestamp;
    /** Number of Votes recorded for the issue at this moment in time. */
    final private long votes;

    public VoteHistoryEntryImpl(Long issueId, Timestamp timestamp, long votes)
    {
        this.issueId = issueId;
        this.timestamp = timestamp;
        this.votes = votes;
    }

    public Long getIssueId()
    {
        return issueId;
    }

    public Timestamp getTimestamp()
    {
        return timestamp;
    }

    public long getVotes()
    {
        return votes;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        VoteHistoryEntryImpl that = (VoteHistoryEntryImpl) o;

        if (votes != that.votes) { return false; }
        if (issueId != null ? !issueId.equals(that.issueId) : that.issueId != null) { return false; }
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = issueId != null ? issueId.hashCode() : 0;
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (int) (votes ^ (votes >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        return "VoteHistoryEntry{" +
                "issueId='" + issueId + '\'' +
                ", timestamp=" + timestamp +
                ", votes=" + votes +
                '}';
    }
}
