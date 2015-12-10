package com.atlassian.jira.functest.framework.admin;

/**
 * Grouping of admin tasks related to the Sub-Tasks section
 *
 * @since v4.0
 */
public interface Subtasks
{
    /**
     * Enables sub-tasks.
     */
    void enable();

    /**
     * Disables sub-tasks.
     */
    void disable();

    /**
     * @return true if sub-tasks are currently enabled.
     * @since v4.2
     */
    boolean isEnabled();

    /**
     * Adds a new sub-task type. Enables sub-tasks if they are disabled.
     * @param subTaskName The name of the new sub-task type.
     * @param subTaskDescription The description of the new sub-task type.
     */
    void addSubTaskType(String subTaskName, String subTaskDescription);

    /**
     * Delete an existing sub-task type.
     * @param subTaskName The name of the sub-task type to be deleted.
     */
    void deleteSubTaskType(String subTaskName);
}
