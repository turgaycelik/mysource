package com.atlassian.jira.util;

import java.util.Comparator;

/**
 * Comparison function that sorts based on the name of a {@link Named} object.
 * That is, it sorts based on the value of the {@code getName()} method.
 */
public class NameComparator implements Comparator<Named>
{
    public static final Comparator<Named> COMPARATOR = new NameComparator();

    public int compare(final Named o1, final Named o2)
    {
        if (o1 == o2)
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

        return name1.compareTo(name2);
    }
}
