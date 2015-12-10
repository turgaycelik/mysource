package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class EditWorkflowTransitionPostFunctionParams extends AbstractEditWorkflowTransitionDescriptorParams
{
    public EditWorkflowTransitionPostFunctionParams(JiraWorkflow workflow, StepDescriptor step,
            ActionDescriptor transition, PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public EditWorkflowTransitionPostFunctionParams(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    protected String getHighLightParamPrefix()
    {
        return "workflow-function";
    }

    protected void setupWorkflowDescriptor()
    {
        ResultDescriptor unconditionalResult = getTransition().getUnconditionalResult();
        if (unconditionalResult != null)
        {
            List postFunctions = unconditionalResult.getPostFunctions();
            if (postFunctions != null && !postFunctions.isEmpty())
            {
                if (postFunctions.size() >= getIndexCount())
                {
                    FunctionDescriptor functionDescriptor = (FunctionDescriptor) postFunctions.get(getIndexCount() - 1);
                    setWorkflowDescriptor(functionDescriptor);
                }
                else
                {
                    addErrorMessage(getText("admin.errors.workflows.count.outside.range", "'" + getIndexCount() + "'"));
                }
            }
            else
            {
                addErrorMessage(getText("admin.errors.workflows.no.post.functions","'" + getTransition().getName() + "'"));
            }
        }
        else
        {
            addErrorMessage(getText("admin.errors.workflows.no.unconditional.result","'" + getTransition().getName() + "'"));
        }
    }

    protected String getPluginType()
    {
        return JiraWorkflowPluginConstants.MODULE_NAME_WORKFLOW_FUNCTION;
    }

    protected void editWorkflowDescriptor(AbstractDescriptor descriptor, Map params)
    {
        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;

        final Map functionArgs = functionDescriptor.getArgs();

        // Add parameters to the workflow function descriptor
        // Make the factory process it
        WorkflowFunctionModuleDescriptor functionModuleDescriptor = (WorkflowFunctionModuleDescriptor) getDescriptor();
        WorkflowPluginFunctionFactory workflowFunctionFactory = (WorkflowPluginFunctionFactory) functionModuleDescriptor.getModule();
        functionArgs.put("full.module.key", getFullModuleKey(functionModuleDescriptor.getPluginKey(), functionModuleDescriptor.getKey()));
        functionArgs.putAll(workflowFunctionFactory.getDescriptorParams(getDescriptorParams()));
    }

    public String getWorkflowDescriptorName()
    {
        return getDescriptor().getName() + " Function";
    }
}
