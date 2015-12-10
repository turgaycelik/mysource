package com.atlassian.jira.functest.framework.admin;

import java.util.Map;

/**
 * Represents functionality of the workflow transition configuration page. 
 *
 * @since v4.3
 */
public interface WorkflowTransition
{
    public static enum Tabs
    {
        ALL("view_all_trans"),
        CONDITIONS("view_conditions"),
        VALIDATORS("view_validators"),
        POST_FUNCTIONS("view_post_functions"),
        TRIGGERS("triggers"),
        OTHER("view_other");

        // this would be in impl if I wasnt feeling laaaazy
        private final String pageId;

        Tabs(String pageId)
        {
            this.pageId = pageId;
        }

        public String linkId()
        {
            return pageId;
        }
    }

    /**
     * Go to the 'Add Workflow Condition' form where user can choose between available workflow conditions to add to
     * the transition.
     *
     * @return this workflow transition
     */
    WorkflowTransition goToAddWorkflowCondition();


    /**
     * Check if condition with given <tt>workflowConditionKey</tt> can be added to this transition, i.e. is present
     * on the 'Add Workflow Condition' screen
     *
     * @param workflowConditionKey key of the condition to check
     * @return <code>true</code>, if condition can be added to this transition
     */
    boolean canAddWorkflowCondition(String workflowConditionKey);

    /**
     * <p>
     * Add workflow condition with given <tt>workflowConditionKey</tt> to the transition.
     *
     * <p>
     * For non-configurable conditions only, otherwise use {@link #addWorkflowCondition(String, java.util.Map)}.
     *
     * @param workflowConditionKey full plugin key of the workflow condition to add
     * @return this workflow transition
     */
    WorkflowTransition addWorkflowCondition(String workflowConditionKey);

    /**
     * <p>
     * Add workflow condition with given <tt>workflowConditionKey</tt> to the transition.
     *
     * <p>
     * Use <tt>configFormParams</tt> to provide parameters for the custom condition configuration form.
     *
     * @param workflowConditionKey full plugin key of the workflow condition to add
     * @param configFormParams form parameters to fill the configure condition screen
     * @return this workflow transition
     */
    WorkflowTransition addWorkflowCondition(String workflowConditionKey, Map<String,String> configFormParams);


    /**
     * Go to the 'Add Workflow Validator' form where user can choose among available workflow validators to add to
     * the transition.
     *
     * @return this workflow transition
     */
    WorkflowTransition goToAddWorkflowValidator();


    /**
     * Check if validator with given <tt>workflowValidatorKey</tt> can be added to this transition, i.e. is present
     * on the 'Add Workflow Validator' screen
     *
     * @param workflowValidatorKey key of the validator to check
     * @return <code>true</code>, if condition can be added to this transition
     */
    boolean canAddWorkflowValidator(String workflowValidatorKey);

    /**
     * <p>
     * Add workflow validator with given <tt>workflowValidatorKey</tt> to the transition.
     *
     * <p>
     * For non-configurable validators only, otherwise use {@link #addWorkflowValidator(String, java.util.Map)}.
     *
     * @param workflowValidatorKey full plugin key of the workflow validator to add
     * @return this workflow transition
     */
    WorkflowTransition addWorkflowValidator(String workflowValidatorKey);

    /**
     * <p>
     * Add workflow validator with given <tt>workflowValidatorKey</tt> to the transition.
     *
     * <p>
     * Use <tt>configFormParams</tt> to provide parameters for the custom validator configuration form.
     *
     * @param workflowValidatorKey full plugin key of the workflow validator to add
     * @param configFormParams form parameters to fill the configure validator screen
     * @return this workflow transition
     */
    WorkflowTransition addWorkflowValidator(String workflowValidatorKey, Map<String,String> configFormParams);


    /**
     * Check if function with given <tt>workflowFunctionKey</tt> can be added to this transition, i.e. is present
     * on the 'Add Workflow Function' screen
     *
     * @param workflowFunctionKey key of the function to check
     * @return <code>true</code>, if condition can be added to this transition
     */
    boolean canAddWorkflowFunction(String workflowFunctionKey);

    /**
     * <p>
     * Go to the 'Add Workflow Function' form where user can choose between available workflow functions to add to
     * the transition.
     *
     * @return this workflow transition
     */
    WorkflowTransition goToAddWorkflowFunction();

    /**
     * <p>
     * Add workflow function with given <tt>workflowFunctionKey</tt> to the transition.
     *
     * <p>
     * This is to use for workflow functions that do not define any configuration screen, i.e. after submitting the
     * function type there is no function-specific configuration screen. Otherwise use
     * {@link #addWorkflowFunction(String, java.util.Map)}.
     *
     * @param workflowFunctionKey full plugin key of the workflow function to add
     * @return this workflow transition 
     */
    WorkflowTransition addWorkflowFunction(String workflowFunctionKey);

    /**
     * <p>
     * Add workflow function with given <tt>workflowFunctionKey</tt> to the transition.
     *
     * <p>
     * This is to use in case where the function has custom configuration screen. Pass the custom parameter values
     * (by HTML filed name) in the <tt>configFormParams</tt> map. Otherwise use {@link #addWorkflowFunction(String)}
     * instead, or pass empty map.
     *
     * @param workflowFunctionKey full plugin key of the workflow function to add
     * @param configFormParams form parameters to fill the configure function screen
     * @return this workflow transition
     */
    WorkflowTransition addWorkflowFunction(String workflowFunctionKey, Map<String,String> configFormParams);


    /**
     * Check if given tab is currently open
     *
     * @param tab tab to check
     * @return <code>true</code>, if given <tt>tab</tt> is currently open
     */
    boolean isTabOpen(Tabs tab);

    /**
     * Open given tab.
     *
     * @param tab tab to open
     * @return this workflow transition instance
     */
    WorkflowTransition openTab(Tabs tab);
}
