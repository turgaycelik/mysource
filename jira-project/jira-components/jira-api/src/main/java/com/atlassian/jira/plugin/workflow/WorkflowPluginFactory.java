package com.atlassian.jira.plugin.workflow;

import com.opensymphony.workflow.loader.AbstractDescriptor;

import java.util.Map;

/**
 * A WorkflowPluginFactory implements methods used to configure a workflow plugin.
 * Generally there will be a WorkflowPluginFactory implementation for each workflow plugin type.
 * <p>
 * There are two methods to implement - {@link #getVelocityParams(String, com.opensymphony.workflow.loader.AbstractDescriptor) getVelocityParams}
 * provides parameters for the velocity template that configures the plugin, and {@link #getDescriptorParams(java.util.Map)}
 * parses the velocity form submission, and extracts plugin args from it.
 *
 * @see com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider
 */
public interface WorkflowPluginFactory
{
    /**
     * Given a plugin resource name, return the parameters it needs. When workflow editor is rendering or configuring a workflow
     * condition, this method will be called to fill in a Velocity template.
     * @param resourceName Typically "view", "input-parameters" or "edit-parameters".
     * @param descriptor A {@link com.opensymphony.workflow.loader.ConditionDescriptor} describing the Condition and its current configuration.
     * @return Map of params, eg. {"group": ['jira-users', 'jira-developers']}
     */
    Map<String, ?> getVelocityParams(String resourceName, AbstractDescriptor descriptor);

    /**
     * Given a set of name:value parameters from the plugin configuration page (ie. the 'input-parameters' velocity template)
     * return a map of sanitized parameters which will be passed into workflow plugin instances.
     * For example, the results are passed in the 'arg' parameter
     * of post-functions' {@link com.opensymphony.workflow.FunctionProvider#execute(java.util.Map, java.util.Map, com.opensymphony.module.propertyset.PropertySet) execute()}
     * or conditions' {@link com.opensymphony.workflow.Condition#passesCondition(java.util.Map, java.util.Map, com.opensymphony.module.propertyset.PropertySet) passesCondition}
     * methods.
     * The velocity page often submits values in array form, and this method extracts just the relevant value.
     * @param formParams Parameters from the velocity template, eg. {"fieldId" : ["assignee"], "fieldValue":["-1"]}
     * @return Parameters to be passed into workflow functions via the 'args' map. Eg. {"assignee": "-1"}
     */
    Map<String, ?> getDescriptorParams(Map<String, Object> formParams);
}
