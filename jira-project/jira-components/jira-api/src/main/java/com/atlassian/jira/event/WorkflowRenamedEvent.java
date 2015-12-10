package com.atlassian.jira.event;

import com.atlassian.jira.workflow.JiraWorkflow;

public class WorkflowRenamedEvent extends AbstractWorkflowEvent
{
    private final String oldWorkflowName;
    private final String newWorkflowName;

    public WorkflowRenamedEvent(JiraWorkflow workflow, String oldWorkflowName, String newWorkflowName)
    {
        super(workflow);

        this.oldWorkflowName = oldWorkflowName;
        this.newWorkflowName = newWorkflowName;
    }

    public String getOldWorkflowName()
    {
        return oldWorkflowName;
    }

    public String getNewWorkflowName()
    {
        return newWorkflowName;
    }
}
