package com.atlassian.jira.plugin.workflow;

import java.util.Map;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;

import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;

public class WorkflowPermissionConditionFactoryImpl extends AbstractWorkflowPermissionPluginFactory
        implements WorkflowPluginConditionFactory, WorkflowConditionDescriptorEditPreprocessor
{
    public WorkflowPermissionConditionFactoryImpl(JiraAuthenticationContext authenticationContext,
            PermissionManager permissionManager)
    {
        super(authenticationContext, permissionManager);
    }

    @Override
    protected Map<?, ?> extractArgs(AbstractDescriptor descriptor)
    {
        if (!(descriptor instanceof ConditionDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a ConditionDescriptor.");
        }

        ConditionDescriptor conditionDescriptor = (ConditionDescriptor) descriptor;
        return conditionDescriptor.getArgs();
    }

    @Override
    public void beforeSaveOnEdit(ConditionDescriptor descriptor)
    {
        clearLegacyPermissionArgument(descriptor.getArgs());
    }
}
