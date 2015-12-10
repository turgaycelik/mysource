/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.priority.Priority;
import org.ofbiz.core.entity.GenericValue;

/**
 * Compares priority {@linkn GenericValue GenericValues}. See also
 * {@link com.atlassian.jira.issue.comparator.PriorityObjectComparator}.
 */
public class PriorityComparator extends IssueConstantsComparator
{
    protected IssueConstant getConstant(GenericValue i1)
    {
        return ComponentAccessor.getConstantsManager().getPriorityObject(i1.getString("priority"));
    }

    protected IssueConstant getConstant(Issue i1)
    {
        Priority priorityObject = i1.getPriorityObject();
        if (priorityObject == null)
            return null;
        else
            return priorityObject;
    }
}
