package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.portal.PortalPage;

import java.util.Comparator;

/**
 * Compares a {@link PortalPage} by its name
 *
 * @since v3.13
 */
public class PortalPageNameComparator implements Comparator<PortalPage>
{
    public static final Comparator<PortalPage> COMPARATOR = new PortalPageNameComparator();

    public int compare(final PortalPage o1, final PortalPage o2)
    {
        // check nulls
        if ((o1 == null) && (o2 == null))
        {
            return 0;
        }
        else if (o1 == null) // null is less than any value
        {
            return -1;
        }
        else if (o2 == null) // any value is greater than null
        {
            return 1;
        }

        //compare the filter names (alphabetically)
        final String name1 = o1.getName();
        final String name2 = o2.getName();

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
        return name1.compareToIgnoreCase(name2);
    }
}