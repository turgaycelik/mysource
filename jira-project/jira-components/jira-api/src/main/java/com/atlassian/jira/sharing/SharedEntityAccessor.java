package com.atlassian.jira.sharing;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.util.collect.EnclosedIterable;

import java.util.List;
import java.util.Set;

/**
 * Provides an abstraction for accessing SharedEntity objects. This class abstracts away the details of the SharedEntity
 * implementation to those components that need it.
 *
 * @since v3.13
 */
public interface SharedEntityAccessor<S extends SharedEntity>
{
    /**
     * Returns the type that this object can work with.
     *
     * @return the type that this object can work with.
     */
    TypeDescriptor<S> getType();

    /**
     * Adjusts the favourite counts for a given entity. This resulting value must always be greater or equal to one.
     *
     * @param entity          the entity to adjust
     * @param adjustmentValue the value to adjust by.
     */
    // @TODO make this an S when we work out how to fix the DefaultFavouritesManager to resolve the correct objects
    void adjustFavouriteCount(SharedEntity entity, int adjustmentValue);

    /**
     * This will call back to ask for a {@link SharedEntity} based on id.
     *
     * @param entityId the id of the {@link SharedEntity}
     * @return a {@link SharedEntity} or null if it cant be found
     */
    S getSharedEntity(Long entityId);

    /**
     * This is called to get {@link SharedEntity} by id If the user is allows to see it
     *
     * @param user     the user in play
     * @param entityId the id of the {@link SharedEntity}
     * @return a {@link SharedEntity} if it exists and the user can see it and null otherwise
     */
    S getSharedEntity(User user, Long entityId);

    /**
     * Returns true if the user has permission to use the {@link SharedEntity}
     *
     * @param user   the user in play
     * @param entity the {@link SharedEntity} to check
     * @return true if the user has permission to use it
     */
    boolean hasPermissionToUse(User user, S entity);

    /**
     * Get all {@link SharedEntity sharable entities} this accessor can see.
     *
     * @return a {@link com.atlassian.jira.util.collect.EnclosedIterable} of {@link com.atlassian.jira.sharing.SharedEntity}'s
     *
     * @deprecated This has been moved to the PortalPageManager and deprecated for SearchRequestManager. Since v5.2.
     */
    EnclosedIterable<S> getAll();

    /**
     * Get all {@link SharedEntity sharable entities} this accessor can see for use in indexing.
     *
     * @return a {@link com.atlassian.jira.util.collect.EnclosedIterable} of {@link com.atlassian.jira.sharing.SharedEntity}'s
     */
    EnclosedIterable<SharedEntity> getAllIndexableSharedEntities();

    /**
     * Used to get {@link SharedEntity sharable entities} from a search result.
     *
     * @param descriptor retrieval descriptor
     * @return a {@link com.atlassian.jira.util.collect.EnclosedIterable} of {@link com.atlassian.jira.sharing.SharedEntity}'s
     */
    EnclosedIterable<S> get(RetrievalDescriptor descriptor);

    /**
     * Used to get {@link SharedEntity sharable entities} from a search result.
     * The entities returned may be modified by the user parameter (e.g. permissions or clause sanitisation).
     *
     * @param searcher the user performing the search
     * @param descriptor retrieval descriptor
     * @return a {@link com.atlassian.jira.util.collect.EnclosedIterable} of {@link com.atlassian.jira.sharing.SharedEntity}'s
     */
    EnclosedIterable<S> get(final User searcher, RetrievalDescriptor descriptor);

    /**
     * Factory to retrieve a {@link SharedEntityAccessor} for a given {@link SharedEntity}
     */
    public interface Factory
    {
        /**
         * Retrieves a {@link SharedEntityAccessor} that can operate on the passed type. E.g. SearchRequestManager will be returned for a
         * SearchRequest.
         *
         * @param type the type of entity.
         * @return the corresponding accessor.
         */
        <S extends SharedEntity> SharedEntityAccessor<S> getSharedEntityAccessor(SharedEntity.TypeDescriptor<S> type);

        /**
         * Retrieves a {@link SharedEntityAccessor} that can operate on the passed type. E.g. SearchRequestManager will be returned for a
         * SearchRequest.
         *
         * @param type the type of entity to adjust
         * @return the corresponding accessor.
         */
        <S extends SharedEntity> SharedEntityAccessor<S> getSharedEntityAccessor(String type);
    }

    /**
     * Used when getting a Collection of {@link SharedEntity shared entities}. Describes the ids of the entities
     * to retrieve and whether the order of the result is important.
     */
    public interface RetrievalDescriptor
    {
        /**
         * The list of ids to retrieve. This list should only contain unique elements (ie. a {@link Set} view would have
         * the same size) and not contain any nulls.
         *
         * @return list of ids to retrieve
         */
        List<Long> getIds();

        /**
         * is the order of the id list significant. If so the result should preserve the same iteration order.
         *
         * @return true of the order of the id list should be preserved in the result.
         */
        boolean preserveOrder();
    }
}
