package com.atlassian.jira.issue.comparator.util;

import java.util.Comparator;

public class DelegatingComparator implements Comparator
{
    private final Comparator comparator1;
    private final Comparator comparator2;

    /**
     * Constructs an instace of this comparator setting the first
     * (top-priority) comparator and second (lower-priority) comparator.
     * @param comparator1 comparator
     * @param comparator2 comparator
     */
    public DelegatingComparator(Comparator comparator1, Comparator comparator2)
    {
        this.comparator1 = comparator1;
        this.comparator2 = comparator2;
    }

    /**
     * Compares two given objects. Uses {@link #comparator1} first and returns
     * the result of comparison if not 0. In case of 0, it continues and
     * returns the result of comparison using {@link #comparator2}.
     * @param o1 object to compare
     * @param o2 object to compare
     * @return result of comparison
     */
    public int compare(Object o1, Object o2)
    {
        int result = comparator1.compare(o1, o2);
        if (result != 0)
        {
            return result;
        }
        else
        {
            return comparator2.compare(o1, o2);
        }
    }
}
