package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.List;

@WebSudoRequired
public class DeleteWorkflowTransitionValidator extends AbstractDeleteWorkflowTransitionDescriptor
{
    public DeleteWorkflowTransitionValidator(JiraWorkflow workflow, StepDescriptor step, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public DeleteWorkflowTransitionValidator(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    protected void checkDescriptor()
    {
        // Nothing to check
    }

    protected List getDescriptorCollection()
    {
        return getTransition().getValidators();
    }

    public String getWorkflowDescriptorName()
    {
        return "Validator";
    }

    protected void deleteWorkflowDescriptor() throws WorkflowException
    {
        getDescriptorCollection().remove(count - 1);
    }
}
