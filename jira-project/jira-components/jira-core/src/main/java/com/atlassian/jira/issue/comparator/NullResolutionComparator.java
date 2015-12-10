/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.issue.Issue;

import java.util.Comparator;

/**
 * This comparator just checks for the existance of a resolution - it does not actually use it.  Only works for Issue Objects.
 * <p>
 * This is to fix JRA-1305 (sort by resolution first in the road map).  However, we don't want to just sort
 * by the resolution, but rather sort by the existance of the resolution.
 */
public class NullResolutionComparator implements Comparator
{
    public int compare(Object o1, Object o2)
    {
        if (o1 == null || o2 == null) //very very simplistic null check
            return 0;
        else if (!(o1 instanceof Issue) || !(o2 instanceof Issue))
            throw new IllegalArgumentException("Both objects must be GenericValues");

        final Object resolution1 = ((Issue) o1).getResolution();
        final Object resolution2 = ((Issue) o2).getResolution();

        if (resolution1 == null && resolution2 == null)
            return 0;
        else if (resolution1 != null && resolution2 != null)
            return 0;
        else if (resolution1 == null)
            return -1;
        else //resolution2 == null

            return 1;
    }
}
