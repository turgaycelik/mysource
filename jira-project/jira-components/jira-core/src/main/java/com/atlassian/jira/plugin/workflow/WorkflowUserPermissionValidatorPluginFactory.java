package com.atlassian.jira.plugin.workflow;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.collect.CollectionBuilder;

public class WorkflowUserPermissionValidatorPluginFactory extends WorkflowPermissionValidatorPluginFactory
{
    public WorkflowUserPermissionValidatorPluginFactory(JiraAuthenticationContext authenticationContext,
            PermissionManager permissionManager)
    {
        super(authenticationContext, permissionManager);
    }

    @Override
    protected void populateTemplateParamsForInputAndEdit(Map<String, Object> velocityParams, Map<?, ?> descriptorArgs)
    {
        super.populateTemplateParamsForInputAndEdit(velocityParams, descriptorArgs);

        velocityParams.put("nullallowedoptions", EasyMap.build(Boolean.TRUE.toString(), "True", Boolean.FALSE.toString(), "False"));

        if (descriptorArgs != null)
        {
            velocityParams.put("vars-key", descriptorArgs.get("vars.key"));
            velocityParams.put("nullallowed", descriptorArgs.get("nullallowed"));
        }
    }

    @Override
    protected void populateTemplateParamsForView(Map<String, Object> velocityParams, Map<?, ?> descriptorArgs)
    {
        super.populateTemplateParamsForView(velocityParams, descriptorArgs);
        velocityParams.put("vars-key", descriptorArgs.get("vars.key"));
        velocityParams.put("nullallowed", Boolean.valueOf((String) descriptorArgs.get("nullallowed")));
    }

    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> conditionParams)
    {
        return extractMultipleParams(conditionParams, CollectionBuilder.newBuilder("permissionKey", "vars.key", "nullallowed").asList());
    }
}
