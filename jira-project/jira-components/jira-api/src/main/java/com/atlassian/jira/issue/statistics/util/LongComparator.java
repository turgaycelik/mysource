package com.atlassian.jira.issue.statistics.util;

import java.util.Comparator;

public class LongComparator implements Comparator<Long>
{
    public static final Comparator<Long> COMPARATOR = new LongComparator();

    private LongComparator()
    {
    // use static instance
    }

    public int compare(final Long o1, final Long o2)
    {
        if (o1 == null && o2 == null)
        {
            return 0;
        }
        if (o1 == null)
        {
            return -1;
        }
        if (o2 == null)
        {
            return 1;
        }
        return (o1).compareTo(o2);
    }
}
