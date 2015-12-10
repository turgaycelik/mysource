package com.atlassian.jira.bc.favourites;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Service for basic Favourites functionality.  Used for adding, removing and checking favourites of generic entities.
 * Initially used by SearchRequests and Dashboards but can be easily extended.
 *
 * @since v3.13
 */
@PublicApi
public interface FavouritesService
{
    /**
     * Add the given entity as a favourite of the user passed in the context
     *
     * @param ctx    JIRA Service context
     * @param entity The entity to favourite
     */
    void addFavourite(final JiraServiceContext ctx, final SharedEntity entity);

    /**
     * Add the given entity as a favourite of the user passed in the context and place it in the specified position.
     * The entity currently in the specified position and all those after will be moved down one position.
     *
     * @param ctx    JIRA Service Context holding the current user
     * @param entity the entity to favourite
     * @param position the position in which this entity should be added in the favourites order.
     */
    void addFavouriteInPosition(final JiraServiceContext ctx, final SharedEntity entity, long position);

    /**
     * Remove the given entity as a favourite of the user passed in the context
     *
     * @param ctx    JIRA Service context
     * @param entity The entity to unfavourite
     */
    void removeFavourite(final JiraServiceContext ctx, final SharedEntity entity);

    /**
     * Check that the given entity is a favourite of the user passed in the context
     *
     * @param user   the user to check for
     * @param entity The entity to check favourite
     * @return true if entity is a favourite, otherwise false
     */
    boolean isFavourite(final ApplicationUser user, final SharedEntity entity);

    /**
     * @deprecated Use {@link #isFavourite(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.sharing.SharedEntity)} instead. Since v6.0.
     * Check that the given entity is a favourite of the user passed in the context
     *
     * @param user   the user to check for
     * @param entity The entity to check favourite
     * @return true if entity is a favourite, otherwise false
     */
    boolean isFavourite(final User user, final SharedEntity entity);
}
