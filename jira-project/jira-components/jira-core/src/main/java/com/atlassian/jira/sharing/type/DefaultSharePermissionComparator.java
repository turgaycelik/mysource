package com.atlassian.jira.sharing.type;

import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.type.ShareType.Name;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.Comparator;

/**
 * A comparator that checks if two share permissions are of the same type.
 * 
 * @since v3.13
 */
class DefaultSharePermissionComparator implements Comparator<SharePermission>
{
    private final Name type;

    DefaultSharePermissionComparator(final Name type)
    {
        Assertions.notNull("type", type);
        this.type = type;
    }

    public final int compare(final SharePermission perm1, final SharePermission perm2)
    {

        final int compareResult = DefaultSharePermissionComparator.compareNull(perm1, perm2);
        if ((compareResult == 0) && (perm1 != null))
        {
            if (!type.equals(perm1.getType()) || !type.equals(perm2.getType()))
            {
                throw new IllegalArgumentException();
            }

            return comparePermissions(perm1, perm2);
        }
        return compareResult;
    }

    protected static int compareNull(final Object obj1, final Object obj2)
    {
        if (obj1 == null)
        {
            if (obj2 == null)
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else if (obj2 == null)
        {
            return 1;
        }
        else
        {
            return 0;
        }

    }

    protected int comparePermissions(final SharePermission perm1, final SharePermission perm2)
    {
        return 0;
    }
}
