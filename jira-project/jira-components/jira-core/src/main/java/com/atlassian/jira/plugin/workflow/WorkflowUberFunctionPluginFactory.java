package com.atlassian.jira.plugin.workflow;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

import java.util.Map;

public class WorkflowUberFunctionPluginFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory
{
    private static final Map<String, String> PARAMS_MAP = ImmutableMap.of("eventType", "genericevent");

    protected void getVelocityParamsForInput(Map velocityParams)
    {
        // Nothing to do
    }

    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor)
    {
        // Nothing to do
    }

    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor)
    {
        if (!(descriptor instanceof FunctionDescriptor))
        {
            throw new IllegalArgumentException("Desriptor must be a FunctionDescriptor.");
        }

        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;

        if (functionDescriptor.getArgs().containsKey("eventType"))
        {
            velocityParams.put("eventType", functionDescriptor.getArgs().get("eventType"));
        }
    }

    protected Map createMap(String paramName, String arg)
    {
        return null;
    }

    public Map<String, String> getDescriptorParams(Map conditionParams)
    {
        // Create a 'hard coded' parameter
        return PARAMS_MAP;
    }

}
