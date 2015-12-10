/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.crowd.embedded.api.User;

import java.util.Comparator;

public class UserComparator implements Comparator<User>
{
    public int compare(User o1, User o2)
    {
        if (o1 == o2)
            return 0;

        if (o1 == null)
            return -1;

        if (o2 == null)
            return 1;

        return o1.getName().compareTo(o2.getName());
    }
}
