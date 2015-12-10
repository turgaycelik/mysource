package com.atlassian.jira.task;

import com.atlassian.jira.util.dbc.Assertions;

import java.util.concurrent.RejectedExecutionException;

/**
 * This exception is thrown when attempting to start a task in a {@link com.atlassian.jira.task.TaskManager} that already
 * has a live task with the same context.
 *
 * @since v3.13
 */
public class AlreadyExecutingException extends RejectedExecutionException
{
    private final TaskDescriptor taskDescriptor;

    public AlreadyExecutingException(final TaskDescriptor taskDescriptor, final String message)
    {
        super(message);
        Assertions.notNull("taskDescriptor", taskDescriptor);
        this.taskDescriptor = taskDescriptor;
    }

    public TaskDescriptor getTaskDescriptor()
    {
        return taskDescriptor;
    }
}
