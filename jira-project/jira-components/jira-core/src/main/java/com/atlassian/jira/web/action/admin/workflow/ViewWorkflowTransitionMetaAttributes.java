package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

@WebSudoRequired
public class ViewWorkflowTransitionMetaAttributes extends AbstractWorkflowAction
{
    private final StepDescriptor step;
    private final ActionDescriptor transition;

    public ViewWorkflowTransitionMetaAttributes(JiraWorkflow workflow, StepDescriptor step,
            ActionDescriptor transition)
    {
        super(workflow);
        this.step = step;
        this.transition = transition;
    }

    public ViewWorkflowTransitionMetaAttributes(JiraWorkflow workflow, ActionDescriptor transition)
    {
        this(workflow, null, transition);
    }

    public StepDescriptor getStep()
    {
        return step;
    }

    public ActionDescriptor getTransition()
    {
        return transition;
    }

}
