package com.atlassian.jira.web.action.admin.workflow;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.atlassian.jira.plugin.workflow.WorkflowValidatorDescriptorEditPreprocessor;
import com.atlassian.jira.plugin.workflow.WorkflowValidatorModuleDescriptor;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class EditWorkflowTransitionValidatorParams extends AbstractEditWorkflowTransitionDescriptorParams
{
    public EditWorkflowTransitionValidatorParams(JiraWorkflow workflow, StepDescriptor step,
            ActionDescriptor transition, PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public EditWorkflowTransitionValidatorParams(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    protected String getHighLightParamPrefix()
    {
        return "workflow-validator";
    }

    protected void setupWorkflowDescriptor()
    {
        List validators = getTransition().getValidators();
        if (validators != null && !validators.isEmpty())
        {
            if (validators.size() >= getIndexCount())
            {
                ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) validators.get(getIndexCount() - 1);
                setWorkflowDescriptor(validatorDescriptor);
            }
            else
            {
                addErrorMessage(getText("admin.errors.count.outside.range","'" + getIndexCount() + "'"));
            }
        }
        else
        {
            addErrorMessage(getText("admin.errors.no.validators","'" + getTransition().getName() + "'"));
        }
    }

    protected String getPluginType()
    {
        return JiraWorkflowPluginConstants.MODULE_NAME_WORKFLOW_VALIDATOR;
    }

    protected void editWorkflowDescriptor(AbstractDescriptor descriptor, Map params)
    {
        ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) descriptor;

        final Map validatorArgs = validatorDescriptor.getArgs();

        // Add parameters to the workflow validator descriptor
        // Make the factory process it
        WorkflowValidatorModuleDescriptor validatorModuleDescriptor = (WorkflowValidatorModuleDescriptor) getDescriptor();
        WorkflowPluginValidatorFactory workflowPluginValidatorFactory = validatorModuleDescriptor.getModule();
        validatorArgs.putAll(workflowPluginValidatorFactory.getDescriptorParams(getDescriptorParams()));

        if (workflowPluginValidatorFactory instanceof WorkflowValidatorDescriptorEditPreprocessor)
        {
            ((WorkflowValidatorDescriptorEditPreprocessor) workflowPluginValidatorFactory).beforeSaveOnEdit(validatorDescriptor);
        }
    }

    public String getWorkflowDescriptorName()
    {
        return getText("admin.validator.label","'" + getDescriptor().getName() + "'");
    }
}
