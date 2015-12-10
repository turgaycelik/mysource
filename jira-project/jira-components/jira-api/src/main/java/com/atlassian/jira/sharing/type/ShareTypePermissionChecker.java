package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;

/**
 * Classes that implement this interface check that a user has permission to see
 * a {@link com.atlassian.jira.sharing.SharedEntity} shared by the associated ShareType.
 *
 * @since v3.13
 */
public interface ShareTypePermissionChecker
{
    /**
     * Checks if the passed user is given rights by the ShareType to user/view a {@link SharedEntity} with the passed
     * permission.
     *
     * @param user the user whose permission should be validated.
     * @param permission the permission to validate against.
     * @return true if the user is given rights or false otherwise.
     */
    boolean hasPermission(User user, SharePermission permission);
}
