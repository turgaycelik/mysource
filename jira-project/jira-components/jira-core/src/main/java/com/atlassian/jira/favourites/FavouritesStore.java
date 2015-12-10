package com.atlassian.jira.favourites;

import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Collection;
import java.util.List;

/**
 * Store used for CRUD of Favourites.
 *
 * @since v3.13
 */
public interface FavouritesStore
{
    /**
     * Create Favourites association between User and entity
     *
     * @param user User to associate entity with
     * @param entity the entity to associate with
     * @return true if association was successfully made, else false if it wasn't made or already existed.
     */
    boolean addFavourite(final ApplicationUser user, final SharedEntity entity);

    /**
     * Remove Favourites association between User and entity
     *
     * @param user to disassociate entity with
     * @param entity the entity to disassociate with
     * @return true if association was successfully removed, else false
     */
    boolean removeFavourite(final ApplicationUser user, final SharedEntity entity);

    /**
     * Checks to see if entity is a favourite of the user passed in.
     *
     * @param user to check entity with
     * @param entity the entity to check with
     * @return true if user has favourite association with entity
     */
    boolean isFavourite(final ApplicationUser user, final SharedEntity entity);

    /**
     * Get the ids of a user's favourite Entities for a given entity type
     *
     * @param user The user for the associated entities
     * @param entityType The type of entities to get. E.g. SearchRequest.ENTITY_TYPE
     * @return A Collection on Longs that represent the entities
     */
    Collection<Long> getFavouriteIds(final ApplicationUser user, final SharedEntity.TypeDescriptor<?> entityType);

    /**
     * Get the ids of a user's favourite Entities for a given entity type
     *
     * @param userKey The key of the user for the associated entities
     * @param entityType The type of entities to get. E.g. SearchRequest.ENTITY_TYPE
     * @return A Collection on Longs that represent the entities
     */
    Collection<Long> getFavouriteIds(String userKey, SharedEntity.TypeDescriptor<?> entityType);

    /**
     * Remove the favourite associations for the given User and the given type
     *
     * @param user The user with whom to disassociate entities with
     * @param entityType The type of entity to disassociate user with.
     */
    void removeFavouritesForUser(final ApplicationUser user, SharedEntity.TypeDescriptor<?> entityType);

    /**
     * Remove all favourite associations for a given entity.
     *
     * @param entity The entity to remove all associations with
     */
    void removeFavouritesForEntity(final SharedEntity entity);

    /**
     * Called to update the sequence of a list of favourite {@link com.atlassian.jira.sharing.SharedEntity shared entities}
     * @param user the user in play
     * @param favouriteEntities the list specifying the order of the {@link com.atlassian.jira.sharing.SharedEntity}s
     */
    void updateSequence(final ApplicationUser user, final List<? extends SharedEntity> favouriteEntities);
}
