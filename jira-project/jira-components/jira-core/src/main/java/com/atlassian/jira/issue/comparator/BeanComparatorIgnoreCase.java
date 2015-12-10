package com.atlassian.jira.issue.comparator;

import org.apache.commons.beanutils.BeanUtils;

import java.util.Comparator;

public class BeanComparatorIgnoreCase<T> implements Comparator<T>
{
    private final String property;

    public BeanComparatorIgnoreCase(final String property)
    {
        this.property = property;
    }

    public int compare(final T o1, final T o2)
    {
        try
        {
            final String value1 = BeanUtils.getProperty(o1, property);
            final String value2 = BeanUtils.getProperty(o2, property);
            return String.CASE_INSENSITIVE_ORDER.compare(value1, value2);
        }
        catch (final Exception e)
        {
            throw new ClassCastException(e.toString());
        }
    }
}
