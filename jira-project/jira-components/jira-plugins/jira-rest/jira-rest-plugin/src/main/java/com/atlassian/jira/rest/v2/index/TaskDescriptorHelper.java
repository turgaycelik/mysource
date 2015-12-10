package com.atlassian.jira.rest.v2.index;

import java.util.Collection;

import javax.annotation.Nullable;

import com.atlassian.jira.config.IndexTaskContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskMatcher;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;

/**
 * Helper methods to determine the current/last indexing Task
 *
 * @since v6.1.4
 */
public class TaskDescriptorHelper
{
    private final TaskManager taskManager;

    private final Ordering<TaskDescriptor> byIdOrdering = new Ordering<TaskDescriptor>() {
        public int compare(TaskDescriptor left, TaskDescriptor right) {
            return Longs.compare(left.getTaskId(), right.getTaskId());
        }
    };

    public TaskDescriptorHelper(final TaskManager taskManager)
    {
        this.taskManager = taskManager;
    }

    @Nullable
    public TaskDescriptor<IndexCommandResult> getActiveIndexTask()
    {
        return taskManager.getLiveTask(new IndexTaskContext());
    }

    @Nullable
    public TaskDescriptor<IndexCommandResult> getLastindexTask()
    {
        final Collection<TaskDescriptor<?>> indexingTasks = taskManager.findTasks(new TaskMatcher()
        {
            @Override
            public boolean match(final TaskDescriptor<?> descriptor)
            {
                return descriptor.getTaskContext() instanceof  IndexTaskContext ;
            }
        });
        if (indexingTasks.size() > 0)
        {
            return (TaskDescriptor<IndexCommandResult>) byIdOrdering.max(indexingTasks);
        }
        else
        {
            return null;
        }

    }

    @Nullable
    public TaskDescriptor<IndexCommandResult> getIndexTask(long taskId)
    {
        TaskDescriptor<IndexCommandResult> indexingTask = null;
        final TaskDescriptor<?> task = taskManager.getTask(taskId);
        if (task != null && task.getTaskContext() instanceof IndexTaskContext)
        {
            indexingTask = (TaskDescriptor<IndexCommandResult>)task;
        }
        return indexingTask;
    }

}
