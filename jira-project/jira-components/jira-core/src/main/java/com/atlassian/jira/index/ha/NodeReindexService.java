package com.atlassian.jira.index.ha;

/**
 * Reindex service that runs to check if other nodes have made index changes
 *
 * @since v6.1
 */
public interface NodeReindexService
{
    void cancel();

    void start();

    /**
     * Pause the service.
     * This will wait for any current execution of the service polling to finish.
     */
    void pause();

    void restart();

    /**
     * Returns true if the index on the invoking node can be rebuilt from the information contained in the
     * ReplicatedIndexOperationStore.
     * If the ReplicatedIndexOperationStore is empty then it is assumed the index can be rebuilt.
     *
     * @return true if the index can be rebuilt
     */
    boolean canIndexBeRebuilt();

    /**
     * Reset the index count to the last entry in the NodeIndexOperation table for each node.
     * This is done to mark the current state before getting a new index copy or performing a full reindex.
     *
     * It would normally be done immediately following a pause() of the service.
     */
    void resetIndexCount();

    /**
     * This will replay any local operations that may have been lost when a index is copied from another node in the cluster
     */
    void replayLocalOperations();
}
