/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
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
public class DeleteWorkflowTransitionPostFunction extends AbstractDeleteWorkflowTransitionDescriptor
{

    public DeleteWorkflowTransitionPostFunction(JiraWorkflow workflow, StepDescriptor step, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public DeleteWorkflowTransitionPostFunction(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    protected void checkDescriptor()
    {
        if (getTransition().getUnconditionalResult() == null)
        {
            addErrorMessage(getText("admin.errors.workflows.cannot.delete.function"));
        }
    }

    protected List getDescriptorCollection()
    {
        return getTransition().getUnconditionalResult().getPostFunctions();
    }

    public String getWorkflowDescriptorName()
    {
        return "Post Function";
    }

    protected void deleteWorkflowDescriptor() throws WorkflowException
    {
        getTransition().getUnconditionalResult().getPostFunctions().remove(count - 1);
    }
}