package com.atlassian.jira.task;

import java.io.Serializable;
import java.util.Date;

/**
 * TaskDescriptor decribes the state of a long running task.
 *
 * @param <V> the result type.
 * @since v3.13
 */
public interface TaskDescriptor<V extends Serializable> extends Serializable
{
    /**
     * This returns the result of the long running task. Returns null if the task is not finished.
     *
     * @return the result of the long running task or null if it has not return value.
     */
    V getResult();

    /**
     * True if the task has been started.
     *
     * @return true if the task has been started.
     */
    boolean isStarted();

    /**
     * Tells if caller if the task has finished running or not.
     *
     * @return true if the task has finished running.
     */
    boolean isFinished();

    /**
     * Reuturn the identifier for this task. This is only unique in the current execution of the JVM.
     *
     * @return The unique id of the task
     */
    Long getTaskId();

    /**
     * Return the date when the task was started.
     *
     * @return the time that task was started. <code>null</code> will be returned if the task has not started executing.
     */
    Date getStartedTimestamp();

    /**
     * Return the date when the task was finished.
     *
     * @return the time that task finished executing. <code>null</code> will be returned if the task has not finished
     *         executing.
     */
    Date getFinishedTimestamp();

    /**
     * Return the date when the task was submitted.
     *
     * @return the time that task was submited to the {@link com.atlassian.jira.task.TaskManager}. A <code>null</code>
     *         value will never be returned as the task will always have a submission time.
     */
    Date getSubmittedTimestamp();

    /**
     * This returns number of milliseconds the task has been running for. Will return zero if the task
     * has not started. When the task has started but not finished, it will return the the difference between
     * the current time and the time was started (i.e. it will change). When the task has finished, it will
     * return the difference between the start time and the end time (i.e. it will not change). 
     *
     * @return the elapsed run time in milliseconds.
     */
    long getElapsedRunTime();

    /**
     * Return the user that started to task.
     *
     * @return the user that caused the task to be submitted.  This may be null.
     */
    String getUserName();

    /**
     * Return the description of the task passed when it was created.
     *
     * @return a meaningful description of the task
     */
    String getDescription();

    /**
     * Return he context of task.  Code that starts long running tasks can implement their own variants of this.
     *
     * @return the context of the task. This method will never return <code>null</code> as a task must always
     *         have a context.
     */
    TaskContext getTaskContext();

    /**
     * Returns the URL that displays progress on this task.  It is built using the {@link com.atlassian.jira.task.TaskContext}.
     *
     * @return the URL that displays progress for this task. <code>null</code> cannot be returned.
     */
    String getProgressURL();

    /**
     * Return the {@link TaskProgressIndicator} associated with the task. A task will only have an indictator if its
     * callable implements the {@link com.atlassian.jira.task.ProvidesTaskProgress} interface.
     *
     * @return the {@link TaskProgressIndicator} associated with the task or <code>null</code> if there isn't one.
     */
    TaskProgressIndicator getTaskProgressIndicator();

    /**
     * Returns whether this supports requests to cancel it.
     *
     * @return <code>true</code> if cancellation is supported,
     *    and <code>false</code> otherwise
     * @since v5.2
     */
    public boolean isCancellable();

    /**
     * Returns whether cancellation of task has been requested.
     * Long-running operations should poll to see if task
     * has been requested.
     *
     * @return <code>true</code> if cancellation has been requested,
     *    and <code>false</code> otherwise
     * @since v5.2
     */
    public boolean isCancelled();

    /**
     * Set flag to indicate this task has been cancelled;
     * @param cancelled Cancelled flag
     */
    void setCancelled(boolean cancelled);

    /**
     * Store the result in the descriptor.
     * @param result The Result
     */
    void setResult(V result);
}
