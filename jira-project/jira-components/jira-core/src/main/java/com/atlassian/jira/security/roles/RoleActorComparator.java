package com.atlassian.jira.security.roles;

import java.util.Comparator;

/**
 * Compares {@link RoleActor} objects case insensitively by name. This is a singleton.
 */
public final class RoleActorComparator implements Comparator<RoleActor>
{

    /**
     * Singleton instance of this class.
     */
    public static final RoleActorComparator COMPARATOR = new RoleActorComparator();

    /**
     * Don't construct these, they're singletons.
     */
    private RoleActorComparator()
    {
    }

    public int compare(RoleActor o1, RoleActor o2)
    {
        if (o1 == null && o2 == null)
        {
            return 0;
        }
        if (o1 == null)
        {
            return 1;
        }
        if (o2 == null)
        {
            return -1;
        }
        String name1 = o1.getDescriptor();
        String name2 = o2.getDescriptor();
        if (name1 == null && name2 == null)
        {
            //if same params return 0
            if (o1.equals(o2))
            {
                return 0;
            }
            else
            {
                // Perhaps we should return the sort order of the parameter of the role actor
                return -1;
            }
        }
        if (name1 == null)
        {
            return 1;
        }
        if (name2 == null) {
            return -1;
        }

        int descriptorComparisonResult = name1.compareToIgnoreCase(name2);
        if (descriptorComparisonResult == 0)
        {
            if (o1.equals(o2))
            {
                return 0;
            }
            else
            {
                // Perhaps we should return the sort order of the parameter of the role actor
                return -1;
            }
        }
        return descriptorComparisonResult;
    }
}
