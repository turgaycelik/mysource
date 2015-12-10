/**
 * Copyright 2008 Atlassian Pty Ltd
 */

package com.atlassian.jira.sharing.index;

import com.atlassian.jira.index.Index;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.search.SharedEntitySearcher;

import java.util.Collection;
import java.util.Set;

/**
 * Responsible for actually indexing a {@link SharedEntity}.
 *
 * @since v3.13
 */
public interface SharedEntityIndexer
{
    /**
     * Add or update a document in the index.
     *
     * @param entity the entity to add to the index
     */
    Index.Result index(final SharedEntity entity);

    /**
     * Delete a document from the index.
     *
     * @param entity the entity to remove from the index
     */
    Index.Result deIndex(final SharedEntity entity);

    /**
     * Add or update a document in the index.
     *
     * @param entity the entity to add to the index
     * @param updateReplicatedIndex whether to update the replicated index or not
     * @since v6.1
     */Index.Result index(final SharedEntity entity, boolean updateReplicatedIndex);
    /**
     * Add or update multiple documents in the index
     *
     * @param sharedEntities  a Set of entities to add to the index
     * @param updateReplicatedIndex whether to update the replicated index or not
     * @since v6.1
     */
    Index.Result index(Set<SharedEntity> sharedEntities, boolean updateReplicatedIndex);

    /**
     * Delete multiple documents in the index
     *
     * @param sharedEntities  a Set of entities to remove from the index
     * @param updateReplicatedIndex whether to update the replicated index or not
     * @since v6.1
     */
    Index.Result deIndex(Set<SharedEntity> sharedEntities, boolean updateReplicatedIndex);

    /**
     * Get a SharedEntitySearcher for the specified {@link SharedEntity.TypeDescriptor}
     *
     * @param type the index to use when searching
     * @return searcher for searching
     */
    <S extends SharedEntity> SharedEntitySearcher<S> getSearcher(SharedEntity.TypeDescriptor<S> type);

    /**
     * Optimize a particular type's index.
     *
     * @param type describes the particular index
     * @return the number of milliseconds taken to optimize
     */
    long optimize(final SharedEntity.TypeDescriptor<?> type);

    /**
     * Shutdown a particular type's index.
     *
     * @param type describes the particular index
     */
    void shutdown(final SharedEntity.TypeDescriptor<?> type);

    /**
     * Clear a particular type's index. Return the path.
     *
     * @param type describes the particular index
     */
    String clear(final SharedEntity.TypeDescriptor<?> type);

    /**
     * @return all the paths where the indexes are
     */
    Collection<String> getAllIndexPaths();

    /**
     * Recreate the index. Clear it if it currently exists, create a new one if it doesn't.
     */
    void recreate(TypeDescriptor<?> type);
}