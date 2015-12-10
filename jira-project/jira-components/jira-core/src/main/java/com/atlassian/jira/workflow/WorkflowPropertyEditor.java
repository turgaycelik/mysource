package com.atlassian.jira.workflow;

import com.atlassian.jira.bc.ServiceOutcome;

import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

/**
 * An editor that can be used to perform CRUD operations on the properties of a workflow.
 *
 * @since v6.2
 */
public interface WorkflowPropertyEditor
{
    static final String DEFAULT_NAME_KEY = "key";
    static final String DEFAULT_VALUE_KEY = "value";

    /**
     * Add a property to the workflow. It is an error to try an update a property that already exists.
     *
     * The caller must check the returned {@link com.atlassian.jira.workflow.WorkflowPropertyEditor.Result} to
     * work out the actual name and value saved as they may have been transformed during the save.
     *
     * @param name the name of the property.
     * @param value the value of the property.
     * @return the result of the operation. The {@link com.atlassian.jira.bc.ServiceOutcome} either contains errors
     *  or the {@link Result} of the operation if successful.
     */
    ServiceOutcome<Result> addProperty(String name, String value);

    /**
     * Update a property on the workflow. A property that does not exist will be added if necessary.
     *
     * The caller must check the returned {@link com.atlassian.jira.workflow.WorkflowPropertyEditor.Result} to
     * work out the actual name and value saved as they may have been transformed during the save.
     *
     * @param name the name of the property.
     * @param value the value of the property.
     * @return the result of the operation. The {@link com.atlassian.jira.bc.ServiceOutcome} either contains errors
     *  or the {@link Result} of the operation if successful.
     */
    ServiceOutcome<Result> updateProperty(String name, String value);

    /**
     * Delete a property from the workflow.
     *
     * @param name the name of the property.
     * @return the result of the operation. The {@link com.atlassian.jira.bc.ServiceOutcome} either contains errors
     *  or the {@link Result} of the operation if successful.
     */
    ServiceOutcome<Result> deleteProperty(String name);

    /**
     * Set the key used by the editor in the {@link com.atlassian.jira.util.ErrorCollection} when reporting
     * errors on the property name. The default is
     * {@link com.atlassian.jira.workflow.DefaultWorkflowPropertyEditor#DEFAULT_NAME_KEY}.
     *
     * @param nameKey the name of the key to use.
     * @return the current editor.
     */
    WorkflowPropertyEditor nameKey(String nameKey);

    /**
     * Set the key used by the editor in the {@link com.atlassian.jira.util.ErrorCollection} when reporting
     * errors on the property value. The default is
     * {@link com.atlassian.jira.workflow.DefaultWorkflowPropertyEditor#DEFAULT_VALUE_KEY}.
     *
     * @param valueKey the name of the key to use.
     * @return the current editor.
     */
    WorkflowPropertyEditor valueKey(String valueKey);

    /**
     * Return the key used by the editor in the {@link com.atlassian.jira.util.ErrorCollection} when reporting
     * errors on the property key.
     *
     * @return the key used by the editor to report property name errors.
     */
    String getNameKey();

    /**
     * Return the key used by the editor in the {@link com.atlassian.jira.util.ErrorCollection} when reporting
     * errors on the property value.
     *
     * @return the key used by the editor to report property key errors.
     */
    String getValueKey();

    /**
     * Factory for {@link WorkflowPropertyEditor} instances.
     */
    interface WorkflowPropertyEditorFactory
    {
        /**
         * Create a property editor for the transition on the passed workflow. The workflow and transition must
         * match.
         *
         * @param workflow the workflow to create an editor for.
         * @param descriptor the transition to create and editor for.
         * @return the editor.
         */
        WorkflowPropertyEditor transitionPropertyEditor(JiraWorkflow workflow, ActionDescriptor descriptor);

        /**
         * Create a property editor for the step on the passed workflow. The workflow and step must
         * match.
         *
         * @param workflow the workflow to create an editor for.
         * @param descriptor the step to create and editor for.
         * @return the editor.
         */
        WorkflowPropertyEditor stepPropertyEditor(JiraWorkflow workflow, StepDescriptor descriptor);
    }

    /**
     * Contains the result of a successful operation.
     */
    interface Result
    {
        /**
         * Returns true when the workflow property was changed.
         *
         * @return true when the workflow property was changed.
         */
        boolean isModified();

        /**
         * The actual workflow key saved. The editor may need to transform the key before it can be saved.
         *
         * @return the actual workflow key saved.
         */
        String name();

        /**
         * The actual workflow value saved. The editor may need to transform the value before it can be saved.
         *
         * @return the actual workflow value saved.
         */
        String value();
    }
}
