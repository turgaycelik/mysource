package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.InfrastructureException;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowModuleDescriptor;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractAddWorkflowTransitionDescriptorParams extends AbstractWorkflowTransitionAction
{
    /** The complete key for the module that we are creating */
    private String pluginModuleKey;
    private Map descriptorParams;
    private AbstractWorkflowModuleDescriptor descriptor;

    public AbstractAddWorkflowTransitionDescriptorParams(JiraWorkflow workflow, StepDescriptor step,
            ActionDescriptor transition, PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
    }

    public AbstractAddWorkflowTransitionDescriptorParams(JiraWorkflow workflow,
            ActionDescriptor transition, PluginAccessor pluginAccessor, WorkflowService workflowService)
    {
        super(workflow, transition, pluginAccessor, workflowService);
    }

    @RequiresXsrfCheck
    public String doDefault() throws Exception
    {
        setupDescriptor();

        // Test if this workflow descriptor requires any arguments
        final ResourceDescriptor parametersResourceDescriptor = getParametersResourceDescriptor(descriptor);
        if (parametersResourceDescriptor == null)
        {
            // The descriptor does not need any parameters - just create its workflow descriptor
            descriptorParams = Collections.EMPTY_MAP;
            return doExecute();
        }
        else
        {
            // We need to display 'input' parameters template for the workflow descriptor
            return super.doDefault();
        }
    }

    protected void doValidation()
    {
        setupDescriptor();

        // Setup the parameters returned from the view
        setupWorkflowDescriptorParams(ActionContext.getParameters());
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Add the workflow descriptor descriptor to workflow
        addWorkflowDescriptor();

        return getRedirect(getRedirectUrl());
    }

    protected String getRedirectUrl()
    {
        if (getStep() == null)
        {
            return "ViewWorkflowTransition.jspa" + getBasicWorkflowParameters() +
                   "&workflowTransition=" + getTransition().getId() + "&atl_token=" + urlEncode(getXsrfToken());
        }
        else
        {
            return "ViewWorkflowTransition.jspa" + getBasicWorkflowParameters() +
                   "&workflowStep=" + getStep().getId() +
                   "&workflowTransition=" + getTransition().getId() + "&atl_token=" + urlEncode(getXsrfToken());
        }
    }


    public String getPluginModuleKey()
    {
        return pluginModuleKey;
    }

    public void setPluginModuleKey(String pluginModuleKey)
    {
        this.pluginModuleKey = pluginModuleKey;
    }

    protected void setupWorkflowDescriptorParams(Map startingParams)
    {
        descriptorParams = new HashMap(startingParams);

        removeKeyOrAddError(descriptorParams, "workflowName", "admin.errors.workflows.cannot.find.name");
        removeKeyOrAddError(descriptorParams, "workflowStep", "admin.errors.workflows.cannot.find.step");
        removeKeyOrAddError(descriptorParams, "workflowTransition", "admin.errors.workflows.cannot.find.transition");
        removeKeyOrAddError(descriptorParams, "pluginModuleKey", "admin.errors.workflows.cannot.find.cannot.find.plugin.module.key");
        removeKeyOrAddError(descriptorParams, "count", "admin.errors.workflows.cannot.find.count");
        removeKeyOrAddError(descriptorParams, "nested", "admin.errors.workflows.cannot.find.nested");

        // The Submit button name
        descriptorParams.remove("Add");
    }

    protected void setupDescriptor()
    {
        ModuleDescriptor pluginModuleDescriptor = getPluginAccessor().getPluginModule(pluginModuleKey);

        if (!getWorkflowModuleDescriptorClass().isInstance(pluginModuleDescriptor))
        {
            throw new InfrastructureException("Module descriptor for '" + pluginModuleKey + "' is not a " + getWorkflowModuleDescriptorClass().getName() + ".");
        }

        descriptor = (AbstractWorkflowModuleDescriptor) pluginModuleDescriptor;
    }

    /**
     * Returns null if the descriptor does not need parameters or return the ResourceDescriptor representing the
     * view for the parameters
     *
     * @param descriptor
     */
    private ResourceDescriptor getParametersResourceDescriptor(AbstractWorkflowModuleDescriptor descriptor)
    {
        Collection<ResourceDescriptor> resourceDescriptors = descriptor.getResourceDescriptors();
        for (final ResourceDescriptor resourceDescriptor : resourceDescriptors)
        {
            if (JiraWorkflowPluginConstants.RESOURCE_NAME_INPUT_PARAMETERS.equals(resourceDescriptor.getName()))
            {
                return resourceDescriptor;
            }
        }

        return null;
    }

    protected Map getDescriptorParams()
    {
        return descriptorParams;
    }

    protected AbstractWorkflowModuleDescriptor getDescriptor()
    {
        return descriptor;
    }

    public List getWorkflowModuleDescriptors()
    {
        return getPluginAccessor().getEnabledModuleDescriptorsByClass(getWorkflowModuleDescriptorClass());
    }

    /**
     * Generate HTML prompting for this condition's parameters.
     */
    public String getDescriptorHtml()
    {
        return descriptor.getHtml(JiraWorkflowPluginConstants.RESOURCE_NAME_INPUT_PARAMETERS, (AbstractDescriptor) null);
    }

    protected abstract Class getWorkflowModuleDescriptorClass();

    protected abstract void addWorkflowDescriptor() throws WorkflowException, PluginParseException;

    public abstract String getWorkflowDescriptorName();
}
