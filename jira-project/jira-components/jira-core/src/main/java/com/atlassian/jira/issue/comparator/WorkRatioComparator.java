/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.issue.worklog.WorkRatio;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

public class WorkRatioComparator implements Comparator
{
    public int compare(Object o1, Object o2)
    {
        GenericValue i1 = (GenericValue) o1;
        GenericValue i2 = (GenericValue) o2;

        long ratio1 = WorkRatio.getWorkRatio(i1);
        long ratio2 = WorkRatio.getWorkRatio(i2);

        if (ratio1 > ratio2)
            return 1;
        else if (ratio2 > ratio1)
            return -1;
        else if (ratio1 == ratio2)
            return 0;

        return 1;
    }
}
