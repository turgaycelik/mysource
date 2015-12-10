/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.issuetype.IssueType;
import org.ofbiz.core.entity.GenericValue;

public class IssueTypeComparator extends IssueConstantsComparator
{
    protected IssueConstant getConstant(GenericValue i1)
    {
        return ComponentAccessor.getConstantsManager().getIssueTypeObject(i1.getString("type"));
    }

    protected IssueConstant getConstant(Issue i1)
    {
        IssueType issueType = i1.getIssueTypeObject();
        if (issueType == null)
            return null;
        else
            return issueType;
    }
}
