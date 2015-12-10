package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Permissions Checker for {@link com.atlassian.jira.sharing.SharedEntity} objects that are shared with a group.
 *
 * @since v3.13
 */
public class GroupShareTypePermissionChecker implements ShareTypePermissionChecker
{
    private final GroupManager groupManager;

    public GroupShareTypePermissionChecker(final GroupManager groupManager)
    {
        this.groupManager = groupManager;
    }

    /**
     * Checks to see if user is part of the group specified in the permission
     *
     * @param user       the user to check
     * @param permission The permission containing the group
     * @return true if user is part of group, else false.
     */
    public boolean hasPermission(final User user, final SharePermission permission)
    {
        Assertions.notNull("permission", permission);
        Assertions.notNull("permission.param1", permission.getParam1());
        Assertions.equals(GroupShareType.TYPE.toString(), GroupShareType.TYPE, permission.getType());

        if (user == null)
        {
            return false;
        }

        final Group group = groupManager.getGroup(permission.getParam1());
        return group != null && groupManager.isUserInGroup(user, group);
    }
}
