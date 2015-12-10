/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.issue.Issue;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

public class IssueKeyComparator implements Comparator
{
    public static final Comparator COMPARATOR = new IssueKeyComparator();

    public int compare(Object o1, Object o2)
    {
        String key1 = null;
        String key2 = null;

        if (o1 == o2)
            return 0;
        else if (o2 == null) // any value is greater than null
            return 1;
        else if (o1 == null) // null is less than any value
            return -1;

        if(o1 instanceof GenericValue)
        {
            key1 = ((GenericValue ) o1).getString("key");
        }
        else if (o1 instanceof Issue)
        {
            key1 = ((Issue ) o1).getKey();
        }
        else if (o1 instanceof String)
        {
            key1 = (String) o1;
        }
        else
        {
            throw new ClassCastException("Cannot turn the " + o1.getClass().getName() + " object: " + o1 + " into an IssueKey");
        }

        if(o2 instanceof GenericValue)
        {
            key2 = ((GenericValue ) o2).getString("key");
        }
        else if (o2 instanceof Issue)
        {
            key2 = ((Issue ) o2).getKey();
        }
        else if (o2 instanceof String)
        {
            key2 = (String) o2;
        }
        else
        {
            throw new ClassCastException("Cannot turn the " + o2.getClass().getName() + " object: " + o2 + " into an IssueKey");
        }

        return KeyComparator.COMPARATOR.compare(key1, key2);
    }
}