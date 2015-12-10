package com.atlassian.jira.index;

public interface MultiThreadedIndexingConfiguration
{
    /**
     * Minimum size of a batch that will cause a the operation to become multi-threaded.
     * 
     * @return the minimum number of issues in the batch that triggers multi-threading.
     */
    int minimumBatchSize();

    /**
     * How many threads used.
     * 
     * @return the number of threads used when 
     */
    int noOfThreads();

    /**
     * The maximum number of elements allowed on the queue before blocking on queue submission.
     * 
     * @return the size of the queue
     */
    int maximumQueueSize();
}
