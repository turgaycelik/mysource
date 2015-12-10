package com.atlassian.jira.plugin.workflow;

import java.util.Map;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;

import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

public class WorkflowPermissionValidatorPluginFactory extends AbstractWorkflowPermissionPluginFactory
        implements WorkflowPluginValidatorFactory, WorkflowValidatorDescriptorEditPreprocessor
{
    public WorkflowPermissionValidatorPluginFactory(JiraAuthenticationContext authenticationContext,
            PermissionManager permissionManager)
    {
        super(authenticationContext, permissionManager);
    }

    @Override
    protected Map<?, ?> extractArgs(AbstractDescriptor descriptor)
    {
        if (!(descriptor instanceof ValidatorDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a ValidatorDescriptor.");
        }

        ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) descriptor;
        return validatorDescriptor.getArgs();
    }

    @Override
    public void beforeSaveOnEdit(ValidatorDescriptor descriptor)
    {
        clearLegacyPermissionArgument(descriptor.getArgs());
    }
}
