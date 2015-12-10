package com.atlassian.jira.task;

import java.io.Serializable;

/**
 * A long running task that implements <code>RequiresTaskInformation</code> will be told about the
 * {@link com.atlassian.jira.task.TaskDescriptor} that describes the task.
 *
 * @since v3.13
 */
public interface RequiresTaskInformation<T extends Serializable>
{
    /**
     * Called to give the task its {@link com.atlassian.jira.task.TaskDescriptor}. This will be called before the
     * tasks starts running. The passed descriptor is live and may change during the life of the task.
     *
     * @param taskDescriptor the task's descriptor. It will not be null. The descriptor is live and may change
     * during the life of the task.
     */
    void setTaskDescriptor(TaskDescriptor<T> taskDescriptor);
}
