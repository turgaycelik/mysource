package com.atlassian.jira.util;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;

public class GroupPermissionCheckerImpl implements GroupPermissionChecker
{
    private final PermissionManager permissionManager;
    private final GroupManager groupManager;

    public GroupPermissionCheckerImpl(PermissionManager permissionManager, GroupManager groupManager)
    {
        this.permissionManager = permissionManager;
        this.groupManager = groupManager;
    }

    public boolean hasViewGroupPermission(final String groupName, final User user)
    {
        // Admins can view all groups
        if (permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return true;
        }

        Group group = groupManager.getGroup(groupName);
        return group != null && user != null && groupManager.isUserInGroup(user, group);
    }
}
