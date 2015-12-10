/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue.bulkedit;

public class WorkflowTransitionKey
{
    private final String workflowName;
    private final String actionDescriptorId;
    private final String destinationStatus;

    public WorkflowTransitionKey(String workflowName, String actionDescriptor, String destinationStatus)
    {
        this.workflowName = workflowName;
        this.actionDescriptorId = actionDescriptor;
        this.destinationStatus = destinationStatus;
    }

    public String getWorkflowName()
    {
        return workflowName;
    }

    public String getActionDescriptorId()
    {
        return actionDescriptorId;
    }

    public String getDestinationStatus()
    {
        return destinationStatus;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        WorkflowTransitionKey that = (WorkflowTransitionKey) o;

        if (actionDescriptorId != null ? !actionDescriptorId.equals(that.actionDescriptorId) : that.actionDescriptorId != null)
        {
            return false;
        }
        if (destinationStatus != null ? !destinationStatus.equals(that.destinationStatus) : that.destinationStatus != null)
        {
            return false;
        }
        if (workflowName != null ? !workflowName.equals(that.workflowName) : that.workflowName != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (workflowName != null ? workflowName.hashCode() : 0);
        result = 31 * result + (actionDescriptorId != null ? actionDescriptorId.hashCode() : 0);
        result = 31 * result + (destinationStatus != null ? destinationStatus.hashCode() : 0);
        return result;
    }
}
