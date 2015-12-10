package com.atlassian.jira.sharing;

import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.sharing.type.ProjectShareType;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * This is an adapter class that can be called when an entity, like groups and projects and roles, is deleted
 * to allow the other associated SharePermissions to be deleted as well.
 *
 * @since v3.13
 */
public class SharePermissionDeleteUtils
{
    private final ShareManager shareManager;

    public SharePermissionDeleteUtils(final ShareManager shareManager)
    {
        this.shareManager = shareManager;
    }

    /**
     * This is called when a group is about to be deleted.  This will clean up any SharePermissions associated
     * with that group
     *
     * @param groupName the name of the group being deleted
     */
    public void deleteGroupPermissions(final String groupName)
    {
        Assertions.notNull("groupName", groupName);
        final SharePermission permission = new SharePermissionImpl(GroupShareType.TYPE, groupName, null);
        shareManager.deleteSharePermissionsLike(permission);
    }

    /**
     * This is called when a role is about to be deleted.  This will clean up any SharePermissions associated
     * with that role for ALL projects.
     *
     * @param roleId the id of the role being deleted
     */
    public void deleteRoleSharePermissions(final Long roleId)
    {
        Assertions.notNull("roleId", roleId);
        // This uses a special package level constructor that sets project id to null.
        final SharePermission permission = new SharePermissionImpl(ProjectShareType.TYPE, roleId.toString());
        shareManager.deleteSharePermissionsLike(permission);
    }

    /**
     * This is called when a project is about to be deleted.  This will clean up any SharePermissions associated
     * with that Project, including those share with a role in that project.
     *
     * @param projectId the id of the project being deleted
     */
    public void deleteProjectSharePermissions(final Long projectId)
    {
        Assertions.notNull("projectId", projectId);
        final SharePermission permission = new SharePermissionImpl(ProjectShareType.TYPE, projectId.toString(), null);
        shareManager.deleteSharePermissionsLike(permission);
    }
}
