package com.atlassian.jira.plugin.workflow;

import com.opensymphony.workflow.loader.AbstractDescriptor;

import java.util.Map;

public class WorkflowChangeHistoryFunctionPluginFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory
{
    protected void getVelocityParamsForEdit(Map velocityParams)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void getVelocityParamsForInput(Map velocityParams)
    {
        // The function does not take any arguments so it does not need to be configured
    }

    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor)
    {
        // Nothing needs to be done as the function does not need any parameters for the view
    }

    public Map getDescriptorParams(Map conditionParams)
    {
        return null;
    }
}
