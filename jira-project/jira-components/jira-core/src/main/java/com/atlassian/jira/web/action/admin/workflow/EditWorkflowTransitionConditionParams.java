package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.plugin.workflow.WorkflowConditionDescriptorEditPreprocessor;
import com.atlassian.jira.plugin.workflow.WorkflowConditionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowPluginConditionFactory;
import com.atlassian.jira.web.action.util.workflow.WorkflowEditorTransitionConditionUtil;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class EditWorkflowTransitionConditionParams extends AbstractEditWorkflowTransitionDescriptorParams
{
    WorkflowEditorTransitionConditionUtil wetcu;

    public EditWorkflowTransitionConditionParams(JiraWorkflow workflow, StepDescriptor step,
            ActionDescriptor transition, PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
        wetcu = new WorkflowEditorTransitionConditionUtil();
    }

    public EditWorkflowTransitionConditionParams(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
        wetcu = new WorkflowEditorTransitionConditionUtil();
    }

    protected String getHighLightParamPrefix()
    {
        return "workflow-condition";
    }

    protected void setupWorkflowDescriptor()
    {
        RestrictionDescriptor restriction = getTransition().getRestriction();
        if (restriction != null)
        {
            ConditionsDescriptor conditionsDescriptor = wetcu.getParentConditionsDescriptor(restriction, getCount());
            if (conditionsDescriptor != null)
            {
                List conditions = conditionsDescriptor.getConditions();
                if (conditions.size() >= getIndexCount())
                {
                    Object descriptor = conditions.get(getIndexCount() - 1);
                    if (descriptor instanceof ConditionDescriptor)
                    {
                        setWorkflowDescriptor((ConditionDescriptor) descriptor);
                    }
                    else if (descriptor instanceof ConditionsDescriptor)
                    {
                        addErrorMessage(getText("admin.errors.workflows.cannot.edit.condition.descriptor"));
                    }
                    else
                    {
                        addErrorMessage(getText("admin.errors.workflows.invalid.condition.descriptor"));
                    }
                }
                else
                {
                    addErrorMessage(getText("admin.errors.workflows.count.outside.range", "'" + getIndexCount() + "'"));
                }
            }
            else
            {
                addErrorMessage(getText("admin.errors.workflows.no.condition.descriptor","'" + getTransition().getName() + "'"));
            }
        }
        else
        {
            addErrorMessage(getText("admin.errors.workflows.no.restrictions.descriptor","'" + getTransition().getName() + "'"));
        }
    }

    protected String getPluginType()
    {
        return JiraWorkflowPluginConstants.MODULE_NAME_WORKFLOW_CONDITION;
    }

    protected void editWorkflowDescriptor(AbstractDescriptor descriptor, Map params)
    {
        ConditionDescriptor conditionDescriptor = (ConditionDescriptor) descriptor;

        final Map conditionArgs = conditionDescriptor.getArgs();

        // Add parameters to the workflow condition descriptor
        // Make the factory process it
        WorkflowConditionModuleDescriptor conditionModuleDescriptor = (WorkflowConditionModuleDescriptor) getDescriptor();
        WorkflowPluginConditionFactory workflowPluginConditionFactory = conditionModuleDescriptor.getModule();
        conditionArgs.putAll(workflowPluginConditionFactory.getDescriptorParams(getDescriptorParams()));

        if (workflowPluginConditionFactory instanceof WorkflowConditionDescriptorEditPreprocessor)
        {
            ((WorkflowConditionDescriptorEditPreprocessor) workflowPluginConditionFactory).beforeSaveOnEdit(conditionDescriptor);
        }
    }

    public String getWorkflowDescriptorName()
    {
        return getDescriptor().getName() + " Condition";
    }
}
