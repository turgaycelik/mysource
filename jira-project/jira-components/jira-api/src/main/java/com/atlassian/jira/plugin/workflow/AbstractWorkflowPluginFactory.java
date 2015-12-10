package com.atlassian.jira.plugin.workflow;

import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A base {@link WorkflowPluginFactory} class that most concrete implementations should extend.
 * A WorkflowPluginFactory mainly exists to provide velocity parameters to the templates.
 * WorkflowPluginFactory implements methods used to configure a workflow plugin.
 * Generally there will be a WorkflowPluginFactory implementation for each workflow plugin type.
 */
public abstract class AbstractWorkflowPluginFactory implements WorkflowPluginFactory
{
    /**
     * Creates a Map of parameters for a view-specific velocity context given
     * the name of the resource (i.e. configured velocity template name:
     * one of view, input-parameters or edit-parameters) and delegates
     * the parameter adding to the appropriate abstract method for that view.
     * @param resourceName the name of the velocity
     * @param descriptor
     * @return the populated velocity params.
     */
    public Map<String, Object> getVelocityParams(final String resourceName, final AbstractDescriptor descriptor)
    {
        final Map<String, Object> velocityParams = new HashMap<String, Object>();

        if (JiraWorkflowPluginConstants.RESOURCE_NAME_VIEW.equals(resourceName))
        {
            getVelocityParamsForView(velocityParams, descriptor);
        }
        else if (JiraWorkflowPluginConstants.RESOURCE_NAME_INPUT_PARAMETERS.equals(resourceName))
        {
            getVelocityParamsForInput(velocityParams);
        }
        else if (JiraWorkflowPluginConstants.RESOURCE_NAME_EDIT_PARAMETERS.equals(resourceName))
        {
            getVelocityParamsForEdit(velocityParams, descriptor);
        }

        return velocityParams;
    }

    /**
     * Get velocity parameters for 'input-parameters' velocity template.
     * @param velocityParams Map to populate.
     * @see #getVelocityParams(String, com.opensymphony.workflow.loader.AbstractDescriptor)
     */
    protected abstract void getVelocityParamsForInput(Map<String, Object> velocityParams);

    /**
     * Populates the given map with velocity parameters for 'edit-parameters' velocity template.
     * Typically an implementation would call {@link ConditionDescriptor#getArgs() descriptor.getArgs()}
     * to retrieve the current configuration, and populate velocityParams from that.
     * @param velocityParams Map to populate.
     * @param descriptor Eg. {@link FunctionDescriptor} or {@link ConditionDescriptor} describing the function/condition and its current configuration.
     * @see #getVelocityParams(String, com.opensymphony.workflow.loader.AbstractDescriptor)
     */
    protected abstract void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor descriptor);

    /**
     * Populates the given map with velocity parameters for 'view' velocity template.
     * Eg. call {@link ConditionDescriptor#getArgs() descriptor.getArgs()} to retrieve the current
     * configuration, look up a displayable string for the value and and populate velocityParams with that.
     * @param velocityParams Map to populate.
     * @param descriptor Eg. a {@link FunctionDescriptor} or {@link ConditionDescriptor} describing the function/condition and its current configuration.
     * @see #getVelocityParams(String, com.opensymphony.workflow.loader.AbstractDescriptor)
     */
    protected abstract void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor descriptor);

    protected Map<String, ?> extractMultipleParams(final Map<String, Object> params, final Collection<String> paramNames)
    {
        final Map<String, String> extractedParams = new HashMap<String, String>();

        for (final String paramName : paramNames)
        {
            final String paramValue = extractSingleParam(params, paramName);
            extractedParams.put(paramName, paramValue);
        }

        return createMap(extractedParams);
    }

    protected String extractSingleParam(final Map<String, Object> conditionParams, final String paramName)
    {
        if (conditionParams.containsKey(paramName))
        {
            final Object argument = conditionParams.get(paramName);
            if (argument instanceof String[])
            {
                final String[] args = (String[]) argument;
                if (args.length == 1)
                {
                    return args[0];
                }
                else
                {
                    throw new IllegalArgumentException("Found " + args.length + " '" + paramName + "' arguments instead of 1.");
                }
            }
            else
            {
                throw new IllegalArgumentException("Argument '" + paramName + "' is not a String array.");
            }
        }
        else
        {
            throw new IllegalArgumentException("Cannot find expected argument '" + paramName + "' in parameters.");
        }
    }

    protected Map<String, String> createMap(final Map<String, String> extractedParams)
    {
        return extractedParams;
    }
}
