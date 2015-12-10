package com.atlassian.jira.imports.project;

import com.atlassian.jira.task.TaskContext;

/**
 * This does almost nothing. Uses the default equals and hashcode so that when you register a task with the task
 * manager it will always be seen as unique. Also, we don't want a progress url since we don't want anyone
 * jumping into the middle of a ProjectImport wizard.
 *
 * @since v3.13
 */
public class ProjectImportTaskContext implements TaskContext
{
    public String buildProgressURL(final Long taskId)
    {
        return null;
    }
}
