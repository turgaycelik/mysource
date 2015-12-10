/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.InfrastructureException;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.atlassian.jira.plugin.workflow.WorkflowValidatorModuleDescriptor;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

import java.util.Map;

/**
 * Used to query the user for validator's parameters if any and actually create the
 * validator workflow descriptor. The action will create the workflow descriptor without
 * querying the user if the validator does not need any parameters.
 */
@WebSudoRequired
public class AddWorkflowTransitionValidatorParams extends AbstractAddWorkflowTransitionDescriptorParams
{
    private String currentValidatorCount;

    public AddWorkflowTransitionValidatorParams(JiraWorkflow workflow, StepDescriptor step, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public AddWorkflowTransitionValidatorParams(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    protected void addWorkflowDescriptor() throws WorkflowException
    {
        ValidatorDescriptor validatorDescriptor = DescriptorFactory.getFactory().createValidatorDescriptor();
        validatorDescriptor.setType("class");
        final Map validatorArgs = validatorDescriptor.getArgs();
        validatorArgs.put("class.name", getDescriptor().getImplementationClass().getName());

        // Add parameters to the workflow validator descriptor
        // Make the factory process it
        WorkflowPluginValidatorFactory workflowPluginValidatorFactory = (WorkflowPluginValidatorFactory) getDescriptor().getModule();
        validatorArgs.putAll(workflowPluginValidatorFactory.getDescriptorParams(getDescriptorParams()));

        if (getTransition().getValidators() != null)
        {
            getTransition().getValidators().add(validatorDescriptor);
            currentValidatorCount = "" + getTransition().getValidators().size();
        }
        else
        {
            throw new InfrastructureException("Validators collection is null for workflow '" + getWorkflow().getName() + "' step '" + getStep().getName() + "' transition '" + getTransition().getName() + "'.");
        }

        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());
    }

    protected String getRedirectUrl()
    {
        if (TextUtils.stringSet(currentValidatorCount))
            return super.getRedirectUrl() + "&currentCount=workflow-validator" + currentValidatorCount;
        else
            return super.getRedirectUrl();
    }

    public String getWorkflowDescriptorName()
    {
        return "Validator";
    }

    protected Class getWorkflowModuleDescriptorClass()
    {
        return WorkflowValidatorModuleDescriptor.class;
    }
}