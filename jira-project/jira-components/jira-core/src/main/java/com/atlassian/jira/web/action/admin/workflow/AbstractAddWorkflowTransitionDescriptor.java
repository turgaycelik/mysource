package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowModuleDescriptor;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractAddWorkflowTransitionDescriptor extends AbstractWorkflowTransitionAction
{
    String type;

    public AbstractAddWorkflowTransitionDescriptor(JiraWorkflow workflow, StepDescriptor step,
            ActionDescriptor transition, PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public AbstractAddWorkflowTransitionDescriptor(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    protected void doValidation()
    {
        if (!TextUtils.stringSet(type))
            addErrorMessage(getText("admin.errors.workflows.you.must.select.type", getText(getDescriptorNameKey())));
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        return getRedirect(getRedirectUrl());
    }

    protected String getRedirectUrl()
    {
        // Redirect to the action that will query the user for workflow descriptor's parameters (if any) and actually create the
        // workflow descriptor
        if (getStep() == null)
        {
            return getParamsActionName() + getBasicWorkflowParameters() +
                   "&workflowTransition=" + getTransition().getId() +
                   "&pluginModuleKey=" + urlEncode(type) + "&atl_token=" + urlEncode(getXsrfToken());
        }
        else
        {
            return getParamsActionName() + getBasicWorkflowParameters() +
                   "&workflowStep=" + getStep().getId() +
                   "&workflowTransition=" + getTransition().getId() +
                   "&pluginModuleKey=" + urlEncode(type) + "&atl_token=" + urlEncode(getXsrfToken());
        }
    }

    public List getWorkflowModuleDescriptors() throws PluginParseException
    {
        final List moduleDescriptors = new ArrayList(getPluginAccessor().getEnabledModuleDescriptorsByType(getWorkflowModuleDescriptorType()));

        for (Iterator iterator = moduleDescriptors.iterator(); iterator.hasNext();)
        {
            AbstractWorkflowModuleDescriptor abstractWorkflowModuleDescriptor = (AbstractWorkflowModuleDescriptor) iterator.next();

            // Check if the module is unique, and if so check if it is already present
            if (abstractWorkflowModuleDescriptor.isUnique() && isModulePresent(abstractWorkflowModuleDescriptor.getImplementationClass()))
            {
                // If so, remove from the addition list
                iterator.remove();
            }
            // Check if the module descriptor cannot be added to this transition type.
            // This is useful when certain module descriptors are added by default to certain action types (e.g. initial action),
            // but it should not be possible to add them to other actions types.
            else if (!abstractWorkflowModuleDescriptor.isAddable(getWorkflow().getActionType(getTransition())))
            {
                // If so, remove from the addition list
                iterator.remove();
            }
        }

        Collections.sort(moduleDescriptors);
        return moduleDescriptors;
    }

    protected abstract String getWorkflowModuleDescriptorType();

    /**
     * Gets the i18n key for the name of the descriptor in play.
     * @return the i18n key for the name of the descriptor in play.
     */
    public abstract String getDescriptorNameKey();

    protected abstract String getParamsActionName();

    protected abstract boolean isModulePresent(Class implementationClass);

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
