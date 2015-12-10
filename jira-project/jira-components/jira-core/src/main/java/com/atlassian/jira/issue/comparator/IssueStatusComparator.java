package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.status.Status;
import org.ofbiz.core.entity.GenericValue;

public class IssueStatusComparator extends IssueConstantsComparator
{
    protected IssueConstant getConstant(GenericValue i1)
    {
        return ComponentAccessor.getConstantsManager().getStatusObject(i1.getString("status"));
    }

    protected IssueConstant getConstant(Issue i1)
    {
        Status statusObject = i1.getStatusObject();
        if (statusObject != null)
            return statusObject;
        else
            return null;
    }
}
