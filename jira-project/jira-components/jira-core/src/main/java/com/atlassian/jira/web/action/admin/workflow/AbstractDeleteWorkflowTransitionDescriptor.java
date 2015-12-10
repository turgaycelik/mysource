package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.plugin.PluginAccessor;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.net.URLEncoder;
import java.util.List;

public abstract class AbstractDeleteWorkflowTransitionDescriptor extends AbstractWorkflowTransitionAction
{
    int count = Integer.MAX_VALUE;

    protected AbstractDeleteWorkflowTransitionDescriptor(JiraWorkflow workflow, StepDescriptor step,
            ActionDescriptor transition, PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    protected AbstractDeleteWorkflowTransitionDescriptor(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        this(workflow, null, transition, pluginAccessor, workflowService);
    }

    protected void doValidation()
    {
        if (count < 1)
        {
            addErrorMessage(getText("admin.errors.workflows.invalid.count","" + count));
        }

        checkDescriptor();

        if (!invalidInput())
        {
            final List descriptors = getDescriptorCollection();

            if (descriptors == null || descriptors.isEmpty())
            {
                addErrorMessage(getText("admin.errors.workflows.no.descriptors.to.delete"));
            }
            else if (descriptors.size() < count)
            {
                addErrorMessage(getText("admin.errors.workflows.count.too.large","" + count, "" + descriptors.size()));
            }
        }
    }

    protected abstract void checkDescriptor();

    protected abstract List getDescriptorCollection();

    public abstract String getWorkflowDescriptorName();

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        deleteWorkflowDescriptor();

        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());

        if (getStep() == null)
        {
            return getRedirect("ViewWorkflowTransition.jspa" + getBasicWorkflowParameters() +
                               "&workflowTransition=" + getTransition().getId());
        }
        else
        {
            return getRedirect("ViewWorkflowTransition.jspa" + getBasicWorkflowParameters() +
                               "&workflowStep=" + getStep().getId() +
                               "&workflowTransition=" + getTransition().getId()); 
        }
    }

    protected abstract void deleteWorkflowDescriptor() throws WorkflowException;

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }
}
