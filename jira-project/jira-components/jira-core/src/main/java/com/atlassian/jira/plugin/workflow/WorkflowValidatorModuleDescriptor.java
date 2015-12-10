package com.atlassian.jira.plugin.workflow;

import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.module.ModuleFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

public class WorkflowValidatorModuleDescriptor extends AbstractWorkflowModuleDescriptor<WorkflowPluginValidatorFactory>
{
    public WorkflowValidatorModuleDescriptor(final JiraAuthenticationContext authenticationContext,
            final OSWorkflowConfigurator workflowConfigurator, final ComponentClassManager componentClassManager, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, workflowConfigurator, componentClassManager, moduleFactory);
    }

    @Override
    protected String getParameterName()
    {
        return "validator-class";
    }

    @Override
    public String getHtml(final String resourceName, final AbstractDescriptor descriptor)
    {
        if ((descriptor != null) && !(descriptor instanceof ValidatorDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a ValidatorDescriptor");
        }

        final ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) descriptor;
        final WorkflowPluginValidatorFactory workflowPluginValidatorFactory = getModule();
        return super.getHtml(resourceName, workflowPluginValidatorFactory.getVelocityParams(resourceName, validatorDescriptor));
    }

    @Override
    public boolean isOrderable()
    {
        return false;
    }

    @Override
    public boolean isUnique()
    {
        return false;
    }

    @Override
    public boolean isDeletable()
    {
        return true;
    }

    @Override
    public boolean isAddable(final String actionType)
    {
        return true;
    }
}
