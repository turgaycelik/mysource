/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.resolution.Resolution;
import org.ofbiz.core.entity.GenericValue;

public class ResolutionComparator extends IssueConstantsComparator
{
    protected IssueConstant getConstant(GenericValue i1)
    {
        return ComponentAccessor.getConstantsManager().getResolutionObject(i1.getString("resolution"));
    }

    protected IssueConstant getConstant(Issue i1)
    {
        Resolution resolutionObject = i1.getResolutionObject();
        if (resolutionObject == null)
            return null;
        else
            return resolutionObject;
    }
}
