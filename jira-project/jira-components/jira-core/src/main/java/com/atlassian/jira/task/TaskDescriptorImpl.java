package com.atlassian.jira.task;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

import com.atlassian.jira.cluster.ClusterSafe;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation of {@link com.atlassian.jira.task.TaskDescriptor}.
 *
 * @since v3.13
 */
class TaskDescriptorImpl<V extends Serializable> implements TaskDescriptor<V>
{
    private static final long serialVersionUID = 6656416609727582193L;

    private final TaskContext taskContext;
    private final Date submittedTime;
    private final Long taskId;
    private final String description;
    private final String userName;
    private final String progressURL;

    private final TaskProgressIndicator taskProgressIndicator;

    private AtomicLong startedTimestamp = new AtomicLong();
    private AtomicLong finishedTimestamp = new AtomicLong();
    private final boolean cancellable;
    private volatile boolean cancelled;
    private volatile V result;

    TaskDescriptorImpl(@Nonnull final Long taskId, @Nonnull final String description, @Nonnull final TaskContext taskContext, final String userName, final TaskProgressIndicator taskProgressIndicator, final boolean cancellable)
    {
        this.cancellable = cancellable;
        notNull("taskId", taskId);
        notNull("description", description);
        notNull("taskContext", taskContext);

        this.taskContext = taskContext;
        this.description = description;
        this.taskId = taskId;
        this.userName = userName;
        submittedTime = new Date();
        this.taskProgressIndicator = taskProgressIndicator;
        progressURL = taskContext.buildProgressURL(taskId);
    }

    /**
     * THREAD: Thread safe copy contructor for TaskDescriptorImpls. Synchronizes on the copied TaskDescriptorImpl to
     * ensure that its mutable fields are not updated during the copy.
     *
     * @param copiedTaskDescriptor the task descriptor to copy
     */

    TaskDescriptorImpl(@Nonnull final TaskDescriptorImpl<V> copiedTaskDescriptor)
    {
        notNull("copiedTaskDescriptor", copiedTaskDescriptor);

        synchronized (copiedTaskDescriptor)
        {
            taskContext = copiedTaskDescriptor.getTaskContext();
            description = copiedTaskDescriptor.getDescription();
            taskId = copiedTaskDescriptor.getTaskId();
            userName = copiedTaskDescriptor.getUserName();
            submittedTime = copiedTaskDescriptor.getSubmittedTimestamp();
            taskProgressIndicator = copiedTaskDescriptor.getTaskProgressIndicator();
            progressURL = copiedTaskDescriptor.getProgressURL();
            cancellable = copiedTaskDescriptor.isCancellable();
            cancelled = copiedTaskDescriptor.isCancelled();
            result = copiedTaskDescriptor.getResult();
            this.startedTimestamp.set(copiedTaskDescriptor.getStartedTimestampMillis());
            this.finishedTimestamp.set(copiedTaskDescriptor.finishedTimestampMillis());
        }
    }

    @ClusterSafe("TaskDescriptors are only updated on their originating node")
    public synchronized long getElapsedRunTime()
    {
        if (startedTimestamp.get() == 0)
        {
            return 0;
        }
        if (finishedTimestamp.get() == 0)
        {
            return System.currentTimeMillis() - startedTimestamp.get();
        }
        else
        {
            return finishedTimestamp.get() - startedTimestamp.get();
        }
    }

    public V getResult()
    {
        return result;
    }

    @Override
    public boolean isCancellable()
    {
        return cancellable;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    public void setCancelled(final boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    @ClusterSafe("TaskDescriptors are only updated on their originating node")
    public synchronized boolean isStarted()
    {
        return startedTimestamp.get() != 0;
    }

    @ClusterSafe("TaskDescriptors are only updated on their originating node")
    public synchronized boolean isFinished()
    {
        return finishedTimestamp.get() != 0;
    }

    @ClusterSafe("TaskDescriptors are only updated on their originating node")
    public synchronized Date getFinishedTimestamp()
    {
        if (finishedTimestamp.get() != 0)
        {
            return new Date(finishedTimestamp.get());
        }
        return null;
    }

    private long finishedTimestampMillis()
    {
        return finishedTimestamp.get();
    }

    @ClusterSafe("TaskDescriptors are only updated on their originating node")
    synchronized void setFinishedTimestamp()
    {
        if (startedTimestamp.get() == 0)
        {
            throw new IllegalStateException("Task has not yet started.");
        }
        if (!finishedTimestamp.compareAndSet(0, System.currentTimeMillis()))
        {
            throw new IllegalStateException("Task has already finished.");
        }

    }

    @ClusterSafe("TaskDescriptors are only updated on their originating node")
    public synchronized Date getStartedTimestamp()
    {
        if (startedTimestamp.get() != 0)
        {
            return new Date(startedTimestamp.get());
        }
        else
        {
            return null;
        }
    }

    private long getStartedTimestampMillis()
    {
        return startedTimestamp.get();
    }

    @ClusterSafe("TaskDescriptors are only updated on their originating node")
    synchronized void setStartedTimestamp()
    {
        if (!startedTimestamp.compareAndSet(0, System.currentTimeMillis()))
        {
            throw new IllegalStateException("Task has already started.");
        }
    }

    public Date getSubmittedTimestamp()
    {
        return new Date(submittedTime.getTime());
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getDescription()
    {
        return description;
    }

    public TaskContext getTaskContext()
    {
        return taskContext;
    }

    public TaskProgressIndicator getTaskProgressIndicator()
    {
        return taskProgressIndicator;
    }

    public String getProgressURL()
    {
        return progressURL;
    }

    @Override
    public void setResult(final V result)
    {
        this.result = result;
    }
}
