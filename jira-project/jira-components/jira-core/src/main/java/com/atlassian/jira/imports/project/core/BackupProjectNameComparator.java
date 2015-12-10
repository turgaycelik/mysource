package com.atlassian.jira.imports.project.core;

import com.atlassian.jira.external.beans.ExternalProject;

import java.util.Comparator;

/**
 * Used to sort {@link com.atlassian.jira.imports.project.core.BackupProject}'s by their name.
 *
 * @since v3.13
 */
public class BackupProjectNameComparator implements Comparator
{
    public int compare(final Object o1, final Object o2)
    {
        final BackupProject bp1 = (BackupProject) o1;
        final BackupProject bp2 = (BackupProject) o2;

        if ((bp1 == null) && (bp2 == null))
        {
            return 0;
        }
        else if (bp2 == null) // any value is less than null
        {
            return -1;
        }
        else if (bp1 == null) // null is greater than any value
        {
            return 1;
        }

        final ExternalProject project1 = bp1.getProject();
        final ExternalProject project2 = bp2.getProject();
        // check nulls
        if ((project1 == null) && (project2 == null))
        {
            return 0;
        }
        else if (project1 == null) // null is less than any value
        {
            return -1;
        }
        else if (project2 == null) // any value is greater than null
        {
            return 1;
        }

        // Really get the names to compare on
        final String name1 = project1.getName();
        final String name2 = project2.getName();

        // check nulls
        if ((name1 == null) && (name2 == null))
        {
            return 0;
        }
        else if (name1 == null) // null is less than any value
        {
            return -1;
        }
        else if (name2 == null) // any value is greater than null
        {
            return 1;
        }

        return name1.compareTo(name2);
    }
}
