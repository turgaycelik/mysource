package com.atlassian.jira.plugin.workflow;

import com.opensymphony.workflow.loader.AbstractDescriptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WorkflowAllowOnlyReporterConditionFactoryImpl implements WorkflowPluginConditionFactory
{
    public Map getVelocityParams(String resourceName, AbstractDescriptor descriptor)
    {
        return new HashMap();
    }

    public Map getDescriptorParams(Map conditionParams)
    {
        return Collections.EMPTY_MAP;
    }
}
