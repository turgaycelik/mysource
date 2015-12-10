/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.InfrastructureException;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Used to query the user for function's parameters if any and actually create the
 * function workflow descriptor. The action will create the workflow descriptor without
 * querying the user if the function does not need any parameters.
 */
@WebSudoRequired
public class AddWorkflowTransitionFunctionParams extends AbstractAddWorkflowTransitionDescriptorParams
{
    private String currentPostFunctionCount;

    public AddWorkflowTransitionFunctionParams(JiraWorkflow workflow, StepDescriptor step, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public AddWorkflowTransitionFunctionParams(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    protected Class getWorkflowModuleDescriptorClass()
    {
        return WorkflowFunctionModuleDescriptor.class;
    }

    /**
     * Constructs and adds a {@link FunctionDescriptor} in its correct position in the list of post-functions.
     * @throws WorkflowException
     */
    protected void addWorkflowDescriptor() throws WorkflowException
    {
        FunctionDescriptor functionDescriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        functionDescriptor.setType("class");
        final Map functionArgs = functionDescriptor.getArgs();
        functionArgs.put("class.name", getDescriptor().getImplementationClass().getName());

        // Add parameters to the workflow function descriptor
        // Make the factory process it
        WorkflowFunctionModuleDescriptor functionModuleDescriptor = (WorkflowFunctionModuleDescriptor) getDescriptor();
        WorkflowPluginFunctionFactory workflowFunctionFactory = (WorkflowPluginFunctionFactory) functionModuleDescriptor.getModule();
        functionArgs.put("full.module.key", getFullModuleKey(functionModuleDescriptor.getPluginKey(), functionModuleDescriptor.getKey()));
        functionArgs.putAll(workflowFunctionFactory.getDescriptorParams(getDescriptorParams()));

        ResultDescriptor unconditionalResult = getTransition().getUnconditionalResult();

        if (unconditionalResult == null)
        {
            unconditionalResult = DescriptorFactory.getFactory().createResultDescriptor();
            getTransition().setUnconditionalResult(unconditionalResult);
        }

        final List postFunctions = unconditionalResult.getPostFunctions();

        int position = -1;

        // If the function has a weight, determine position to add the function to
        if (functionModuleDescriptor.getWeight() != null)
        {
            // NOTE: this code is slow - however it should not be executed too often as
            // the only functions that should have weight are the 'system' ones - and all of them should be
            // added during AddWorkflowTransition stage by default. So the oly time this code should be executed
            // is if a system function is not in the post functions list (e.g. workflow imported via XML) and is being added.
            for (int i = 0; i < postFunctions.size(); i++)
            {
                // Look for the first existing post fuction with weight greater than the weight of the
                // one we already have
                FunctionDescriptor descriptor = (FunctionDescriptor) postFunctions.get(i);
                if (descriptor.getType().equals("class") && descriptor.getArgs().containsKey("class.name"))
                {
                    WorkflowFunctionModuleDescriptor workflowModuleDescriptor = null;
                    try
                    {
                        workflowModuleDescriptor = getWorkflowModuleDescriptor((String) descriptor.getArgs().get("class.name"));
                        if (workflowModuleDescriptor != null)
                        {
                            final Integer weight = workflowModuleDescriptor.getWeight();
                            if (weight != null)
                            {
                                if (weight.compareTo(functionModuleDescriptor.getWeight()) > 0)
                                {
                                    position = i;
                                    break;
                                }
                            }
                        }
                    }
                    catch (PluginParseException e)
                    {
                        throw new InfrastructureException(e);
                    }
                }
            }

            if (position == -1)
            {
                // If the function has a weight but we did not find any functions with a weight larger than this one
                // add the function at the end
                position = postFunctions.size();
            }
        }
        else
        {
            // Default to adding the function in the first position
            // So the user does not have to move it up before all the system (default) post-fucntions
            position = 0;
        }

        postFunctions.add(position, functionDescriptor);

        currentPostFunctionCount = "" + (position + 1);

        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());
    }

    public String getWorkflowDescriptorName()
    {
        return "Function";
    }

    protected String getRedirectUrl()
    {
        if (TextUtils.stringSet(currentPostFunctionCount))
            return super.getRedirectUrl() + "&currentCount=workflow-function" + currentPostFunctionCount;
        else
            return super.getRedirectUrl();
    }

    protected WorkflowFunctionModuleDescriptor getWorkflowModuleDescriptor(String className) throws PluginParseException
    {
        final Collection moduleDescriptors = getPluginAccessor().getEnabledModuleDescriptorsByType(JiraWorkflowPluginConstants.MODULE_NAME_WORKFLOW_FUNCTION);

        for (final Object moduleDescriptor : moduleDescriptors)
        {
            WorkflowFunctionModuleDescriptor workflowFunctionModuleDescriptor = (WorkflowFunctionModuleDescriptor) moduleDescriptor;
            if (workflowFunctionModuleDescriptor.getImplementationClass().getName().equals(className))
            {
                return workflowFunctionModuleDescriptor;
            }
        }

        return null;
    }
}