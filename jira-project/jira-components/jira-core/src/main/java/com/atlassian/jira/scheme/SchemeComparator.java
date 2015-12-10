package com.atlassian.jira.scheme;

import java.util.Comparator;

/**
 * Comparator used for ordering Schemes by Name.
 */
public class SchemeComparator implements Comparator<Scheme>
{
    public int compare(final Scheme o1, final Scheme o2)
    {
        if ((o1 != null) && (o2 != null))
        {
            return o1.getName().compareTo(o2.getName());
        }
        return 0;
    }
}
