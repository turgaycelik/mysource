/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

public class IssueLongFieldComparator implements Comparator
{
    private String field;

    public IssueLongFieldComparator(String field)
    {
        this.field = field;
    }

    public int compare(Object o1, Object o2)
    {
        GenericValue i1 = (GenericValue) o1;
        GenericValue i2 = (GenericValue) o2;

        if (i1 == null && i2 == null)
            return 0;
        else if (i2 == null) // any value is less than null

            return -1;
        else if (i1 == null) // null is greater than any value

            return 1;

        Long l1 = i1.getLong(field);
        Long l2 = i2.getLong(field);

        if (l1 == null && l2 == null)
            return 0;
        else if (l1 == null)
            return -1;
        else if (l2 == null)
            return 1;

        return l1.compareTo(l2);
    }
}
