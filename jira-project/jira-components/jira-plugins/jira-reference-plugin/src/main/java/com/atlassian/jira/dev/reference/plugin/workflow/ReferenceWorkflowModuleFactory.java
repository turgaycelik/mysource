package com.atlassian.jira.dev.reference.plugin.workflow;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginConditionFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

import java.util.Map;

public class ReferenceWorkflowModuleFactory extends AbstractWorkflowPluginFactory
        implements WorkflowPluginConditionFactory, WorkflowPluginValidatorFactory
{
    static final String RESULT_PARAM = "reference-module-result";
    private static final String RESULTS_PARAM = "reference-module-results";

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams)
    {
        velocityParams.put(RESULTS_PARAM, ImmutableMap.of(Boolean.TRUE.toString(), Boolean.TRUE.toString(), Boolean.FALSE.toString(),
                Boolean.FALSE.toString()));
    }

    protected void getVelocityParamsForEdit(Map<String,Object> velocityParams, AbstractDescriptor descriptor)
    {
        getVelocityParamsForInput(velocityParams);
        velocityParams.put(RESULT_PARAM, getEvalResult(descriptor));
    }

    protected void getVelocityParamsForView(Map<String,Object> velocityParams, AbstractDescriptor descriptor)
    {
        velocityParams.put(RESULT_PARAM, getEvalResult(descriptor));
    }

    public Map<String,Object> getDescriptorParams(Map<String,Object> conditionParams)
    {
        String value = extractSingleParam(conditionParams, RESULT_PARAM);
        return ImmutableMap.<String,Object>of(RESULT_PARAM, Boolean.valueOf(value));
    }

    private String getEvalResult(AbstractDescriptor descriptor)
    {
        if (descriptor instanceof ConditionDescriptor)
        {
            return (String) asConditionDescriptor(descriptor).getArgs().get(RESULT_PARAM);
        }
        else if (descriptor instanceof ValidatorDescriptor)
        {
            return (String) asValidatorDescriptor(descriptor).getArgs().get(RESULT_PARAM);
        }
        throw new IllegalArgumentException("Unsupported descriptor: " + descriptor);
    }

    private ConditionDescriptor asConditionDescriptor(AbstractDescriptor desc)
    {
        return (ConditionDescriptor) desc;
    }

    private ValidatorDescriptor asValidatorDescriptor(AbstractDescriptor desc)
    {
        return (ValidatorDescriptor) desc;
    }
}
