package com.atlassian.jira.security.roles;

import java.util.Comparator;

/**
 * Compares ProjectRole objects based on case insensitive name comparison.
 */
public final class ProjectRoleComparator implements Comparator<ProjectRole>
{
    public static final Comparator<ProjectRole> COMPARATOR = new ProjectRoleComparator();

    /**
     * Don't construct these, they're a singleton.
     */
    private ProjectRoleComparator()
    {
    // this page intentionally left blank
    }

    public int compare(final ProjectRole o1, final ProjectRole o2)
    {
        if ((o1 == null) && (o2 == null))
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
        final String name1 = (o1).getName();
        final String name2 = (o2).getName();
        if ((name1 == null) && (name2 == null))
        {
            return 0;
        }
        if (name1 == null)
        {
            return 1;
        }
        if (name2 == null)
        {
            return -1;
        }
        return name1.compareToIgnoreCase(name2);
    }
}
