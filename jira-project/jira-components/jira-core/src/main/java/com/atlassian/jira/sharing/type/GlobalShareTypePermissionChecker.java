package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Permissions Checker for globally shared {@link com.atlassian.jira.sharing.SharedEntity}.
 *
 * @since v3.13
 */
public class GlobalShareTypePermissionChecker implements ShareTypePermissionChecker
{
    /**
     * All JIRA users are able to see/use globally shared {@link com.atlassian.jira.sharing.SharedEntity} so this
     * will always return true, regardless of user passed in.
     *
     * @param user User is ignored.
     * @param permission Must be a permission for a {@link GlobalShareType}
     * @return true if permission has type of {@link GlobalShareType#TYPE}, else false
     */
    public boolean hasPermission(final User user, final SharePermission permission)
    {
        Assertions.notNull("permission", permission);
        Assertions.equals(GlobalShareType.TYPE.toString(), GlobalShareType.TYPE, permission.getType());
        
        return true;
    }
}
