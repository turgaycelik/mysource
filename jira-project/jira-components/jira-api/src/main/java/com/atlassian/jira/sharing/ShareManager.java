package com.atlassian.jira.sharing;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Interface for managing {@link com.atlassian.jira.sharing.SharePermission} objects.
 *
 * @since v3.13
 */
public interface ShareManager
{
    /**
     * Retrieve all {@link com.atlassian.jira.sharing.SharePermission} instances associated with a passed in
     * {@link com.atlassian.jira.sharing.SharedEntity}
     *
     * @param entity The entity that has permissions associated with it
     * @return A set of {@link SharePermission} instances associated with the entity.
     */
    SharePermissions getSharePermissions(SharedEntity entity);

    /**
     * Delete all permissions associated with an entity
     *
     * @param entity The entity that will have all its permissions deleted
     */
    void deletePermissions(SharedEntity entity);

    /**
     * Updates the {@link com.atlassian.jira.sharing.SharePermission} associated with an entity. Looks after creation and updating of permissions.
     *
     * @param entity The entity that has permissions associated with it
     * @return The new SharePermissions associated with the entity
     */
    SharePermissions updateSharePermissions(SharedEntity entity);

    /**
     * Whether this entity has been shared with the specified user.
     * @param user The user to check.
     * @param sharedEntity The entity to check.
     * @return true if the entity has been shared with the user, or if the user owns the entity; otherwise, false.
     */
    boolean isSharedWith(ApplicationUser user, SharedEntity sharedEntity);

    /**
     * Whether this entity has been shared with the specified user.
     * @param user The user to check.
     * @param sharedEntity The entity to check.
     * @return true if the entity has been shared with the user, or if the user owns the entity; otherwise, false.
     */
    boolean isSharedWith(User user, SharedEntity sharedEntity);

    /**
     * Checks to see if the user has permission to see this {@link com.atlassian.jira.sharing.SharedEntity}
     *
     * @param user The user to check permissions for
     * @param entity The entity that has permissions associated with it
     * @return returns true if the user has appropriate permissions, otherwise false.
     * @deprecated since 5.0 please use {@link #isSharedWith(com.atlassian.crowd.embedded.api.User, SharedEntity)}
     * to get the same logic. Permission checks should be done in the service layer according to this information.
     */
    @Deprecated
    boolean hasPermission(User user, SharedEntity entity);

    /**
     * Deletes {@link com.atlassian.jira.sharing.SharePermission}'s that have the same "shape" as the provided share permission. It uses the type,
     * param1, and param2 to as the basis for deleting SharePermission's.
     *
     * @param permission a 'template' of the type of SharePermission you want to delete
     * @throws IllegalArgumentException if you provide a null SharePermission or one that does not have a type and (param1 or param2).
     */
    void deleteSharePermissionsLike(SharePermission permission);
}
