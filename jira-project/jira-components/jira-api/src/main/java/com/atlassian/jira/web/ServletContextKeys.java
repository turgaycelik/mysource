package com.atlassian.jira.web;

/**
 * Provides constants for referencing objects in the servlet context in JIRA.
 *
 * @since v4.4
 */
public interface ServletContextKeys
{
    public static final String DATA_IMPORT_TASK_MANAGER = "jira.import.task.manager";
    public static final String DATA_IMPORT_CURRENT_TASK = "jira.import.current.task";
}
