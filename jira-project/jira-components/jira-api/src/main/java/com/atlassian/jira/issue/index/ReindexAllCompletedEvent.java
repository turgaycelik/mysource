package com.atlassian.jira.issue.index;

/**
* Raised when "reindex all" has completed.
* @since v5.0
*/
public class ReindexAllCompletedEvent
{
    /**
     * How long it took for the reindex to occur.
     */
    private final long time;
    private final boolean useBackgroundIndexing;
    private final boolean updateReplicatedIndex;

    public ReindexAllCompletedEvent(long time)
    {
        this.time = time;
        this.useBackgroundIndexing = false;
        this.updateReplicatedIndex = false;
    }

    public ReindexAllCompletedEvent(final long totalTime, final boolean useBackgroundIndexing, final boolean updateReplicatedIndex)
    {
        this.time = totalTime;
        this.useBackgroundIndexing = useBackgroundIndexing;
        this.updateReplicatedIndex = updateReplicatedIndex;

    }

    public long getTotalTime()
    {
        return time;
    }

    public boolean isUsingBackgroundIndexing()
    {
        return useBackgroundIndexing;
    }

    public boolean shouldUpdateReplicatedIndex()
    {
        return updateReplicatedIndex;
    }
}
