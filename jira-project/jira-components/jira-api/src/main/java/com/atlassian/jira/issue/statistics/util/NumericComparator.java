package com.atlassian.jira.issue.statistics.util;

import java.util.Comparator;

/**
 * Class for comparing numeric fields (allows decimals)
 */
public class NumericComparator implements Comparator<Double>
{
    public static final Comparator<Double> COMPARATOR = new NumericComparator();

    private NumericComparator()
    {
    // use static instance
    }

    public int compare(final Double o1, final Double o2)
    {
        if (o1.equals(o2))
        {
            return 0;
        }
        if (o2 == null)
        {
            return 1;
        }
        return o1.compareTo(o2);
    }
}
