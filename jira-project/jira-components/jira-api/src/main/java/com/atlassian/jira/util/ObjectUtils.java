package com.atlassian.jira.util;

import org.apache.commons.collections.Predicate;

public class ObjectUtils
{
    private static final Long UNSELECTED_LONG = new Long(-1);

    private ObjectUtils()
    {
    }

    public static boolean isNotEmpty(Object o)
    {
        return o != null && !"".equals(o);
    }

    public static Predicate getIsSetPredicate()
    {
        return  new Predicate()
        {
            public boolean evaluate(Object o)
            {
                return isNotEmpty(o);
            }
        };
    }

    public static boolean isValueSelected(Object selectValue)
    {
        return selectValue != null && !"".equals(selectValue) && !"-1".equals(selectValue) && !UNSELECTED_LONG.equals(selectValue);
    }

    /**
     * Compares the two objects. Returns true if both are null references or
     * both are not not null and equal.
     * @param o1 object to compare
     * @param o2 object to compare
     * @return true if equal or both null, false otherwise
     */
    public static boolean equalsNullSafe(Object o1, Object o2)
    {
        if (o1 == null)
        {
            return o2 == null;
        }
        else
        {
            return o1.equals(o2);
        }
    }


}
