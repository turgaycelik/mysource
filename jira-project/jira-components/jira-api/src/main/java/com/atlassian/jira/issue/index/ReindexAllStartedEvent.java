package com.atlassian.jira.issue.index;

/**
* Event raised when a "reindex all" event is beginning. This is the event triggered when e.g. the
* admin clicks reindex or new data is imported.
* @since v5.0
*/
public class ReindexAllStartedEvent
{
    private final boolean useBackgroundIndexing;
    private final boolean updateReplicatedIndex;

    // added to keep the APICheck happy
    @SuppressWarnings("unused")
    public ReindexAllStartedEvent()
    {
        this.useBackgroundIndexing = false;
        this.updateReplicatedIndex = false;
    }

    public ReindexAllStartedEvent(final boolean useBackgroundIndexing, final boolean updateReplicatedIndex)
    {
        this.useBackgroundIndexing = useBackgroundIndexing;
        this.updateReplicatedIndex = updateReplicatedIndex;
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
