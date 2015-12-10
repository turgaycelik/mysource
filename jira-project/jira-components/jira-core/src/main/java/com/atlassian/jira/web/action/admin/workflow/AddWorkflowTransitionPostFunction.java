/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.Collection;

@WebSudoRequired
public class AddWorkflowTransitionPostFunction extends AbstractAddWorkflowTransitionDescriptor
{
    public AddWorkflowTransitionPostFunction(JiraWorkflow workflow, StepDescriptor step, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public AddWorkflowTransitionPostFunction(JiraWorkflow workflow, ActionDescriptor transition,
            PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    protected String getWorkflowModuleDescriptorType()
    {
        return JiraWorkflowPluginConstants.MODULE_NAME_WORKFLOW_FUNCTION;
    }

    public String getDescriptorNameKey()
    {
        return "admin.workflowtransition.post.function";
    }

    protected String getParamsActionName()
    {
        return "AddWorkflowTransitionFunctionParams!default.jspa";
    }

    protected boolean isModulePresent(Class implementationClass)
    {
        // Check if the module is present in either the unconiditional result post functions or in global post-functions
        // (as they are always executed).
        return isModulePresent(getTransition().getUnconditionalResult().getPostFunctions(), implementationClass) ||
               isModulePresent(getTransition().getPostFunctions(), implementationClass);
    }

    private boolean isModulePresent(Collection functionCollection, Class implementationClass)
    {
        for (final Object aFunctionCollection : functionCollection)
        {
            FunctionDescriptor functionDescriptor = (FunctionDescriptor) aFunctionCollection;
            if (functionDescriptor.getType().equals("class"))
            {
                if (implementationClass.getName().equals(functionDescriptor.getArgs().get("class.name")))
                {
                    return true;
                }
            }
        }

        return false;
    }
}