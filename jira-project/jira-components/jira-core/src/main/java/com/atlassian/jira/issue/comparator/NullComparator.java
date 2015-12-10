package com.atlassian.jira.issue.comparator;

import java.util.Comparator;

/**
 * This comparator compares two given objects and is null safe.
 */
public class NullComparator implements Comparator
{
    /**
     * Compares two given objects. Returns 0 if both objects are null, 1 if o2
     * is null, -1 if o1 is null. In case when both objects are not null,
     * returns the result of o1.compareTo(o2) as long as o1 implements
     * Comparable, otherwise returns 0.
     * <br/>
     * Note that if o1 is an instance of {@link Comparable} and o2 is not of
     * the same type may result in {@link ClassCastException}.
     *
     * @param o1 object to compare
     * @param o2 object to compare
     * @return result of comparison
     * @throws ClassCastException if o1 is an instance of {@link Comparable} and o2 is not of the same type
     */
    public int compare(Object o1, Object o2) throws ClassCastException
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
        if (o1 instanceof Comparable)
        {
            return ((Comparable) o1).compareTo(o2);
        }
        return 0;
    }
}
