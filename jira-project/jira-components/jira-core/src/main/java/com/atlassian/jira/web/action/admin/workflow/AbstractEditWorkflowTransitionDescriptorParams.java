package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowModuleDescriptor;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public abstract class AbstractEditWorkflowTransitionDescriptorParams extends AbstractWorkflowTransitionAction
{
    private AbstractDescriptor workflowDescriptor;

    // count needs to be a String to support nested conditions blocks
    private String count = "";

    private Map descriptorParams;
    private AbstractWorkflowModuleDescriptor workflowModuleDescriptor;

    public AbstractEditWorkflowTransitionDescriptorParams(JiraWorkflow workflow, StepDescriptor step,
            ActionDescriptor transition, PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public AbstractEditWorkflowTransitionDescriptorParams(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    public String doDefault() throws Exception
    {
        if (!TextUtils.stringSet(getCount()))
        {
            addErrorMessage(getText("admin.errors.workflows.cannot.find.count"));
        }
        if (getIndexCount() < 0)
        {
            addErrorMessage(getText("admin.errors.workflows.count.must.be.positive"));
        }

        if (invalidInput())
        {
            return ERROR;
        }

        // Get the workflowDescriptor
        setupWorkflowDescriptor();

        // Get the module descriptor for the workflow descriptor
        setupWorkflowModuleDescriptor();

        // We need to display 'input' parameters template for the workflow workflowDescriptor
        return super.doDefault();
    }

    protected void doValidation()
    {
        // Get the workflow descriptor
        setupWorkflowDescriptor();

        // Get the module descriptor for the workflow descriptor
        setupWorkflowModuleDescriptor();

        // Setup the parameters returned from the view
        setupWorkflowDescriptorParams(ActionContext.getParameters());
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Edit the parameters of the workflow descriptor
        editWorkflowDescriptor(workflowDescriptor, descriptorParams);

        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());

        return getRedirect(getRedirectUrl());
    }

    protected String getRedirectUrl()
    {
        String redirectUrl;
        if (getStep() == null)
        {
            redirectUrl = "ViewWorkflowTransition.jspa" + getBasicWorkflowParameters() +
                          "&workflowTransition=" + getTransition().getId() +
                          "&currentCount=" + getHighLightParamPrefix() + getCount();
        }
        else
        {
            redirectUrl = "ViewWorkflowTransition.jspa" + getBasicWorkflowParameters() +
                          "&workflowStep=" + getStep().getId() +
                          "&workflowTransition=" + getTransition().getId() +
                          "&currentCount=" + getHighLightParamPrefix() + getCount();
        }

        if (TextUtils.stringSet(getCount()))
        {
            redirectUrl += "&currentCount=" + getCount();
        }

        return redirectUrl;
    }

    protected abstract String getHighLightParamPrefix();

    protected void setupWorkflowModuleDescriptor()
    {
        String type = null;
        Map args = null;
        if (workflowDescriptor instanceof FunctionDescriptor)
        {
            type = ((FunctionDescriptor) workflowDescriptor).getType();
            args = ((FunctionDescriptor) workflowDescriptor).getArgs();
        }
        else if (workflowDescriptor instanceof ConditionDescriptor)
        {
            type = ((ConditionDescriptor) workflowDescriptor).getType();
            args = ((ConditionDescriptor) workflowDescriptor).getArgs();
        }
        else if (workflowDescriptor instanceof ValidatorDescriptor)
        {
            type = ((ValidatorDescriptor) workflowDescriptor).getType();
            args = ((ValidatorDescriptor) workflowDescriptor).getArgs();
        }
        else
        {
            throw new IllegalArgumentException("Invalid workflowDescriptor type");
        }

        if ("class".equalsIgnoreCase(type) && args.containsKey("class.name"))
        {
            final String className = (String) args.get("class.name");
            final String moduleKey = (String) args.get("full.module.key");
            try
            {
                workflowModuleDescriptor = (AbstractWorkflowModuleDescriptor) getWorkflowModuleDescriptor(className, moduleKey, getPluginType());
                return;
            }
            catch (PluginParseException e)
            {
                final String message = "Cannot find module descriptor.";
                log.error(message, e);
                throw new RuntimeException(message, e);
            }
        }

        throw new IllegalArgumentException("Cannot find workflowDescriptor.");
    }

    /**
     * Find the module descriptor given a workflow descriptor class name
     */
    protected AbstractWorkflowModuleDescriptor getWorkflowModuleDescriptor(final String className, final String moduleKey, final String pluginType) throws PluginParseException
    {
        final Collection moduleDescriptors = getPluginAccessor().getEnabledModuleDescriptorsByType(pluginType);

        for (final Object moduleDescriptor : moduleDescriptors)
        {
            final AbstractWorkflowModuleDescriptor abstractWorkflowModuleDescriptor = (AbstractWorkflowModuleDescriptor) moduleDescriptor;

            if (moduleKey == null)
            {
                if (abstractWorkflowModuleDescriptor.getImplementationClass().getName().equals(className))
                {
                    return abstractWorkflowModuleDescriptor;
                }
            }
            else
            {
                if (AbstractWorkflowAction.getFullModuleKey(abstractWorkflowModuleDescriptor.getPluginKey(), abstractWorkflowModuleDescriptor.getKey()).equals(moduleKey) &&
                        abstractWorkflowModuleDescriptor.getImplementationClass().getName().equals(className))
                {
                    return abstractWorkflowModuleDescriptor;
                }
            }
        }

        return null;
    }

    protected void setupWorkflowDescriptorParams(Map parameters)
    {
        descriptorParams = new HashMap(parameters);

        removeKeyOrAddError(descriptorParams, "workflowName", "admin.errors.workflows.cannot.find.name");
        removeKeyOrAddError(descriptorParams, "workflowStep", "admin.errors.workflows.cannot.find.step");
        removeKeyOrAddError(descriptorParams, "workflowTransition", "admin.errors.workflows.cannot.find.transition");
        removeKeyOrAddError(descriptorParams, "count", "admin.errors.workflows.cannot.find.count");

        // The Submit button name
        descriptorParams.remove("Update");
    }

    public String getDescriptorHtml()
    {
        if (workflowModuleDescriptor != null)
        {
            // We have a plugin module that can generate HTML for the edit page for us - so use it
            return workflowModuleDescriptor.getHtml(JiraWorkflowPluginConstants.RESOURCE_NAME_EDIT_PARAMETERS, workflowDescriptor);
        }
        else
        {
            return null;
        }
    }

    public String getCount()
    {
        return count;
    }

    public void setCount(String count)
    {
        this.count = count;
    }

    protected int getIndexCount()
    {
        String[] counts = StringUtils.split(getCount(), ".");
        return Integer.parseInt(counts[counts.length - 1]);
    }

    protected void setWorkflowDescriptor(AbstractDescriptor workflowDescriptor)
    {
        this.workflowDescriptor = workflowDescriptor;
    }

    protected Map getDescriptorParams()
    {
        return descriptorParams;
    }

    protected AbstractWorkflowModuleDescriptor getDescriptor()
    {
        return workflowModuleDescriptor;
    }

    protected abstract void setupWorkflowDescriptor();

    public abstract String getWorkflowDescriptorName();

    protected abstract String getPluginType();

    protected abstract void editWorkflowDescriptor(AbstractDescriptor descriptor, Map params);
}
