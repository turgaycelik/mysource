package com.atlassian.jira.plugin.workflow;

import com.opensymphony.workflow.loader.AbstractDescriptor;

import java.util.Collections;
import java.util.Map;

/**
 * Factory for the Always False workflow condition.
 *
 * @since 6.3.3
 */
public class WorkflowAlwaysFalseConditionFactoryImpl extends AbstractWorkflowPluginFactory implements WorkflowPluginConditionFactory
{
    @Override
    protected void getVelocityParamsForInput(final Map<String, Object> velocityParams)
    {

    }

    @Override
    protected void getVelocityParamsForEdit(final Map<String, Object> velocityParams, final AbstractDescriptor descriptor)
    {

    }

    @Override
    protected void getVelocityParamsForView(final Map<String, Object> velocityParams, final AbstractDescriptor descriptor)
    {

    }

    @Override
    public Map<String, ?> getDescriptorParams(final Map<String, Object> formParams)
    {
        return Collections.EMPTY_MAP;
    }
}
