package com.atlassian.jira.sharing.type;

import com.atlassian.jira.sharing.SharePermission;

/**
 * Compare two Group share permissions.
 *
 * @since v3.13
 */
class GroupSharePermissionComparator extends DefaultSharePermissionComparator
{
    GroupSharePermissionComparator()
    {
        super(GroupShareType.TYPE);
    }

    @Override
    public int comparePermissions(final SharePermission perm1, final SharePermission perm2)
    {
        int compareResult = compareNull(perm1.getParam1(), perm2.getParam1());
        if ((compareResult == 0) && (perm1.getParam1() != null))
        {
            compareResult = perm1.getParam1().compareTo(perm2.getParam1());
        }
        return compareResult;
    }
}
