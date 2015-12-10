/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.util.index;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.Shutdown;
import com.atlassian.jira.util.collect.Sized;

import java.util.Collection;

/**
 * Manage an index lifecycle.
 *
 * @since v3.13
 */
@PublicApi
public interface IndexLifecycleManager extends Sized, Shutdown
{
    /**
     * Reindex everything.
     *
     * @param context used to report progress back to the user or to the logs. Must not be null.
     * @return Reindex time in ms.
     */
    long reIndexAll(Context context);

    /**
     * Reindex everything, but don't stop the world
     * Comments and change history will not be reindexed.
     *
     * @param context used to report progress back to the user or to the logs. Must not be null.
     * @return Reindex time in ms.
     * @since v5.2
     */
    long reIndexAllIssuesInBackground(final Context context);

    /**
     * Reindex everything, but don't stop the world
     *
     * @param context used to report progress back to the user or to the logs. Must not be null.
     * @param reIndexComments Also reindex all the issue comments.
     * @param reIndexChangeHistory Also reindex the issue change history.
     * @return Reindex time in ms.
     * @since v6.2
     */
    long reIndexAllIssuesInBackground(final Context context, boolean reIndexComments, boolean reIndexChangeHistory);

    /**
     * Optimize the underlying indexes. Make the subsequent searching more efficient.
     *
     * @return the amount of time in millis this method took (because you are too lazy to time me), 0 if indexing is not enabled or -1 if we cannot
     *         obtain the index writeLock.
     * @throws IndexException if the indexes are seriously in trouble
     */
    long optimize();

    /**
     * Shuts down the indexing manager and closes its resources (if any).
     */
    void shutdown();

    /**
     * Activates search indexes.
     * This will rebuild the indexes.
     *
     * @param context used to report progress back to the user or to the logs. Must not be null.
     * @return Reindex time in ms
     */
    long activate(Context context);

    /**
     * Activates search indexes.
     *
     * @param context used to report progress back to the user or to the logs. Must not be null.
     * @param reindex reindex after activation.
     * @return Reindex time in ms
     */
    long activate(Context context, boolean reindex);

    /**
     * De-activates indexing (as happens from the admin page) and removes index directories.
     */
    void deactivate();

    /**
     * @return whether this index is enabled or true if all sub indexes are enabled
     * @deprecated since v6.3.3 Use {@link #isIndexAvailable()}
     */
    boolean isIndexingEnabled();

    /**
     * Whether this index is available.
     * The index is not available if the index is being rebuilt or recovered.
     * In a clustered environment this reflects only the state on the local node.
     *
     * @return Whether this index is available.
     *
     * @since v6.3.3
     */
    boolean isIndexAvailable();

    /**
     * @return the result of a simple consistency check that compares the index state to
     *      the current number of issues.  A background re-index should not be attempted
     *      when this returns {@code false}.
     * @since 5.2
     */
    boolean isIndexConsistent();

    /**
     * @return a collection of Strings that map to all paths that contain Lucene indexes. Must not be null.
     */
    Collection<String> getAllIndexPaths();

    /**
     * @return how many Entities will be re-indexed by {@link #reIndexAll(Context)}
     */
    int size();
}
