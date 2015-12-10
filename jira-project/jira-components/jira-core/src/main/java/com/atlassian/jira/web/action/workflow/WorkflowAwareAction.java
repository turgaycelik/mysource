package com.atlassian.jira.web.action.workflow;

public interface WorkflowAwareAction
{
    String getWorkflowTransitionDisplayName();
}