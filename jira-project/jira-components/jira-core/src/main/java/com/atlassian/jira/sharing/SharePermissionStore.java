package com.atlassian.jira.sharing;

import com.atlassian.jira.sharing.SharedEntity.SharePermissions;

/**
 * Store interface for the persistence of SharePermissions
 * 
 * @since v3.13
 */
public interface SharePermissionStore
{
    /**
     * Retrieve all {@link com.atlassian.jira.sharing.SharePermission} instances associated with a given
     * {@link com.atlassian.jira.sharing.SharedEntity}
     * 
     * @param entity The entity that SharePermissions are associated with
     * @return SharePermissions containing SharePermission instances that are associated with a given entity
     */
    SharePermissions getSharePermissions(SharedEntity entity);

    /**
     * Associate given entity with passed in Collection {@link com.atlassian.jira.sharing.SharePermission}. Store is responsible for updating and
     * creating current shares associated with entity.
     * 
     * @param entity The entity to associate SharePermissions with
     * @return SharePermissions containing the {@link com.atlassian.jira.sharing.SharePermission} instances that were persisted
     */
    SharePermissions storeSharePermissions(SharedEntity entity);

    /**
     * Delete all {@link com.atlassian.jira.sharing.SharePermission} instances associate with given entity.
     * 
     * @param entity The entity that SharePermissions are associated with
     * @return the number of permissions that were deleted.
     */
    int deleteSharePermissions(SharedEntity entity);

    /**
     * Deletes {@link com.atlassian.jira.sharing.SharePermission}'s that have the same "shape" as the provided share permission. It uses the type,
     * param1, amd param2 to as the basis for deleting SharePermission
     * 
     * @param permission a 'template' of the type of SharePermission you want to delete
     * @return the number of rows affected by the delete
     * @throws IllegalArgumentException if you provide a null SharePermission or one that does not have a type and (param1 or param2).
     */
    int deleteSharePermissionsLike(SharePermission permission);
}
