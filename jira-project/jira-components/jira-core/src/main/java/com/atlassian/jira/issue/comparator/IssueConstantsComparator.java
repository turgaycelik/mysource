/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstant;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.concurrent.Immutable;
import java.util.Comparator;

@Immutable
public abstract class IssueConstantsComparator implements java.util.Comparator
{
    public int compare(Object o1, Object o2)
    {
        if (o1 == null && o2 == null)
            return 0;
        else if (o2 == null) // any value is less than null
            return -1;
        else if (o1 == null) // null is greater than any value
            return 1;

        IssueConstant constant1;
        IssueConstant constant2;
        if (o1 instanceof Issue)
        {
            constant1 = getConstant((Issue) o1);
            constant2 = getConstant((Issue) o2);
        }
        else if (o1 instanceof IssueConstant)
        {
            constant1 = (IssueConstant) o1;
            constant2 = (IssueConstant) o2;
        }
        else
        {
            constant1 = getConstant((GenericValue) o1);
            constant2 = getConstant((GenericValue) o2);
        }

        return ConstantsComparator.COMPARATOR.compare(constant1, constant2);
    }

    protected abstract IssueConstant getConstant(GenericValue i1);
    protected abstract IssueConstant getConstant(Issue i1);
}
