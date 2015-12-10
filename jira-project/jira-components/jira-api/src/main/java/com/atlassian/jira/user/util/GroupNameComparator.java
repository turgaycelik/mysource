/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.Group;

import java.util.Comparator;

public class GroupNameComparator implements Comparator<Group>
{
    public int compare(final Group g1, final Group g2)
    {
        if (g1 == g2)
        {
            return 0;
        }
        else if (g2 == null) // any value is less than null
        {
            return -1;
        }
        else if (g1 == null) // null is greater than any value
        {
            return 1;
        }

        final String name1 = g1.getName();
        final String name2 = g2.getName();

        if ((name1 == null) || (name2 == null))
        {
            throw new RuntimeException("Null group name");
        }

        return g1.getName().compareTo(g2.getName());
    }
}
