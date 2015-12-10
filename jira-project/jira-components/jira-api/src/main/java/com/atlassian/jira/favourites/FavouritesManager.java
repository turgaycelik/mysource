package com.atlassian.jira.favourites;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Collection;

/**
 * Manager for basic Favourites functionality. Used for adding, removing and checking favourites of generic entities. It also adjusts favourite counts
 * for entities
 *
 * @since v3.13
 */
@PublicApi
public interface FavouritesManager<S extends SharedEntity>
{
    /**
     * Add the given entity as a favourite of the user passed in add to favourites count if necessary.
     *
     * @param user The user adding the favourite
     * @param entity The entity to favourite
     * @throws com.atlassian.jira.exception.PermissionException when trying to add a filter as favourite when you have no permissions
     */
    void addFavourite(final ApplicationUser user, final S entity) throws PermissionException;

    /**
     * @deprecated Use {@link #addFavourite(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.sharing.SharedEntity)} instead. Since v6.0.
     *
     * Add the given entity as a favourite of the user passed in add to favourites count if necessary.
     *
     * @param user The user adding the favourite
     * @param entity The entity to favourite
     * @throws com.atlassian.jira.exception.PermissionException when trying to add a filter as favourite when you have no permissions
     */
    void addFavourite(final User user, final S entity) throws PermissionException;

    /**
     * Add the given entity as a favourite of the user passed in, in the specified position, add to favourites count if necessary.
     * The entity currently in the specified position and all those after will be moved down one position.
     *
     * @param user The user adding the favourite
     * @param entity The entity to favourite
     * @param position the position in which this entity should be added in the favourites order.
     * @throws com.atlassian.jira.exception.PermissionException when trying to add a filter as favourite when you have no permissions
     */
    void addFavouriteInPosition(final ApplicationUser user, final S entity, final long position) throws PermissionException;


    /**
     * @deprecated Use {@link #addFavouriteInPosition(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.sharing.SharedEntity, long)} instead. Since v6.0.
     *
     * Add the given entity as a favourite of the user passed in, in the specified position, add to favourites count if necessary.
     * The entity currently in the specified position and all those after will be moved down one position.
     *
     * @param user The user adding the favourite
     * @param entity The entity to favourite
     * @param position the position in which this entity should be added in the favourites order.
     * @throws com.atlassian.jira.exception.PermissionException when trying to add a filter as favourite when you have no permissions
     */
    void addFavouriteInPosition(final User user, final S entity, final long position) throws PermissionException;

    /**
     * Remove the given entity as a favourite of the user passed in. Remove even if user doesn't have permission to see it and adjust count of
     * favourites if necessary.
     *
     * @param user The user removing the favourite
     * @param entity The entity to favourite
     */
    void removeFavourite(final ApplicationUser user, final S entity);

    /**
     * @deprecated Use {@link #removeFavourite(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.sharing.SharedEntity)} instead. Since v6.0.
     *
     * Remove the given entity as a favourite of the user passed in. Remove even if user doesn't have permission to see it and adjust count of
     * favourites if necessary.
     *
     * @param user The user removing the favourite
     * @param entity The entity to favourite
     */
    void removeFavourite(final User user, final S entity);

    /**
     * Check to see if the given entity is a favourite of the user passed in.
     *
     * @param user The user checking the favourite
     * @param entity The entity to favourite
     * @return true if the entity is favourite and can be seen by user, otherwise false
     * @throws com.atlassian.jira.exception.PermissionException when checking a filter with no permission
     */
    boolean isFavourite(final ApplicationUser user, final S entity) throws PermissionException;

    /**
     * @deprecated Use {@link #isFavourite(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.sharing.SharedEntity)} instead. Since v6.0.
     *
     * Check to see if the given entity is a favourite of the user passed in.
     *
     * @param user The user checking the favourite
     * @param entity The entity to favourite
     * @return true if the entity is favourite and can be seen by user, otherwise false
     * @throws com.atlassian.jira.exception.PermissionException when checking a filter with no permission
     */
    boolean isFavourite(final User user, final S entity) throws PermissionException;

    /**
     * Get the ids of a user's favourite Entities for a given entity type
     *
     * @param user The user for the associated entities. Can not be null.
     * @param entityType The type of entities to get. E.g. SearchRequest.ENTITY_TYPE. Can not be null.
     * @return A Collection on Longs that represent the entities, sorted into sequence order
     * @throws IllegalArgumentException for null user or entity type
     */
    Collection<Long> getFavouriteIds(final ApplicationUser user, final SharedEntity.TypeDescriptor<S> entityType);

    /**
     * @deprecated Use {@link #getFavouriteIds(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.sharing.SharedEntity.TypeDescriptor)} instead. Since v6.0.
     * Get the ids of a user's favourite Entities for a given entity type
     *
     * @param user The user for the associated entities. Can not be null.
     * @param entityType The type of entities to get. E.g. SearchRequest.ENTITY_TYPE. Can not be null.
     * @return A Collection on Longs that represent the entities, sorted into sequence order
     * @throws IllegalArgumentException for null user or entity type
     */
    Collection<Long> getFavouriteIds(final User user, final SharedEntity.TypeDescriptor<S> entityType);

    /**
     * Remove the favourite associations for the given User and the given type
     *
     * @param user The {@link com.atlassian.jira.user.ApplicationUser} with whom to disassociate entities
     * @param entityType The type of entity to disassociate user with.
     */
    void removeFavouritesForUser(final ApplicationUser user, SharedEntity.TypeDescriptor<S> entityType);

    /**
     * @deprecated Use {@link #removeFavouritesForUser(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.sharing.SharedEntity.TypeDescriptor)} instead. Since v6.0.
     *
     * Remove the favourite associations for the given User and the given type
     *
     * @param user The {@link com.atlassian.jira.user.ApplicationUser} with whom to disassociate entities
     * @param entityType The type of entity to disassociate user with.
     */
    void removeFavouritesForUser(final User user, SharedEntity.TypeDescriptor<S> entityType);

    /**
     * Remove all favourite associations for a given entity for entity deletion. This method is for only for when an entity is deleted as it does not
     * adjust favourite counts.
     *
     * @param entity The entity that is being deleted
     */
    void removeFavouritesForEntityDelete(final SharedEntity entity);

    /**
     * Increases the position of the {@link SharedEntity} relative to the user's set of all other entities of the same type.
     *
     * @param user the user whom the entity belongs to
     * @param entity The entity in question
     * @throws com.atlassian.jira.exception.PermissionException when the user does not have permission to perform the action.
     */
    void increaseFavouriteSequence(final ApplicationUser user, final S entity) throws PermissionException;

    /**
     * @deprecated Use {@link #increaseFavouriteSequence(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.sharing.SharedEntity)} instead. Since v6.0.
     *
     * Increases the position of the {@link SharedEntity} relative to the user's set of all other entities of the same type.
     *
     * @param user the user whom the entity belongs to
     * @param entity The entity in question
     * @throws com.atlassian.jira.exception.PermissionException when the user does not have permission to perform the action.
     */
    void increaseFavouriteSequence(final User user, final S entity) throws PermissionException;

    /**
     * Decreases the position of the {@link SharedEntity} relative to the user's set of all other entities of the same type.
     *
     * @param user the user whom the entity belongs to
     * @param entity The entity in question
     * @throws com.atlassian.jira.exception.PermissionException when the user does not have permission to perform the action.
     */
    void decreaseFavouriteSequence(final ApplicationUser user, final S entity) throws PermissionException;

    /**
     * @deprecated Use {@link #decreaseFavouriteSequence(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.sharing.SharedEntity)} instead. Since v6.0.
     * Decreases the position of the {@link SharedEntity} relative to the user's set of all other entities of the same type.
     *
     * @param user the user whom the entity belongs to
     * @param entity The entity in question
     * @throws com.atlassian.jira.exception.PermissionException when the user does not have permission to perform the action.
     */
    void decreaseFavouriteSequence(final User user, final S entity) throws PermissionException;

    /**
     * Moves the position of the {@link SharedEntity} to the start relative to the user's set of all other entities of the same type.
     *
     * @param user the user whom the entity belongs to
     * @param entity The entity in question
     * @throws com.atlassian.jira.exception.PermissionException when the user does not have permission to perform the action.
     */
    void moveToStartFavouriteSequence(final ApplicationUser user, final S entity) throws PermissionException;

    /**
     * @deprecated Use {@link #moveToStartFavouriteSequence(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.sharing.SharedEntity)} instead. Since v6.0.
     *
     * Moves the position of the {@link SharedEntity} to the start relative to the user's set of all other entities of the same type.
     *
     * @param user the user whom the entity belongs to
     * @param entity The entity in question
     * @throws com.atlassian.jira.exception.PermissionException when the user does not have permission to perform the action.
     */
    void moveToStartFavouriteSequence(final User user, final S entity) throws PermissionException;

    /**
     * Moves the position of the {@link SharedEntity} to the end relative to the user's set of all other entities of the same type.
     *
     * @param user the user whom the entity belongs to
     * @param entity The entity in question
     * @throws com.atlassian.jira.exception.PermissionException when the user does not have permission to perform the action.
     */
    void moveToEndFavouriteSequence(final ApplicationUser user, final S entity) throws PermissionException;

    /**
     * @deprecated Use {@link #moveToEndFavouriteSequence(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.sharing.SharedEntity)} instead. Since v6.0.
     *
     * Moves the position of the {@link SharedEntity} to the end relative to the user's set of all other entities of the same type.
     *
     * @param user the user whom the entity belongs to
     * @param entity The entity in question
     * @throws com.atlassian.jira.exception.PermissionException when the user does not have permission to perform the action.
     */
    void moveToEndFavouriteSequence(final User user, final S entity) throws PermissionException;
}
