package com.atlassian.jira.plugin.workflow;

import com.opensymphony.workflow.loader.AbstractDescriptor;

import java.util.Collections;
import java.util.Map;

public class WorkflowNoInputPluginFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory
{
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
        // Nothing to do
    }

    public Map getDescriptorParams(Map conditionParams)
    {
        return Collections.EMPTY_MAP;
    }

}
