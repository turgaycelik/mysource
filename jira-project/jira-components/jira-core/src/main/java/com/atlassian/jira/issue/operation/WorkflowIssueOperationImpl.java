package com.atlassian.jira.issue.operation;

import com.opensymphony.workflow.loader.ActionDescriptor;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class WorkflowIssueOperationImpl implements WorkflowIssueOperation
{
    private final ActionDescriptor actionDescriptor;

    public WorkflowIssueOperationImpl(ActionDescriptor actionDescriptor)
    {
        this.actionDescriptor = actionDescriptor;
    }

    public ActionDescriptor getActionDescriptor()
    {
        return actionDescriptor;
    }

    public String getNameKey()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public String getDescriptionKey()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof WorkflowIssueOperation)) return false;

        final WorkflowIssueOperation workflowIssueOperation = (WorkflowIssueOperation) o;

        if (actionDescriptor != null)
        {
            if (workflowIssueOperation.getActionDescriptor() != null)
            {
                return (actionDescriptor.getId() == workflowIssueOperation.getActionDescriptor().getId());
            }
            else
            {
                return false;
            }
        }
        else
        {
            return (workflowIssueOperation.getActionDescriptor() == null);
        }
    }

    public int hashCode()
    {
        return (actionDescriptor != null ? actionDescriptor.getId() : 0);
    }
}
