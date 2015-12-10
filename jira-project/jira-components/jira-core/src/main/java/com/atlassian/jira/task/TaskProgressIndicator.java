package com.atlassian.jira.task;

/**
 * Can be used to either pull or listen to the progress of a long running task.
 *
 * @since v3.13
 */
public interface TaskProgressIndicator
{
    /**
     * Register the passed listener for notification of task progress.
     *
     * @param listener the listener to register.
     */
    void addListener(TaskProgressListener listener);

    /**
     * De-register the passed listener
     *
     * @param listener the listener to de-register.
     */
    void removeListener(TaskProgressListener listener);

    /**
     * Return the last actual event sent to the indicator.
     *
     * @return the last event sent to the indicator or <code>null</code> if there was no last
     *         event.
     */
    TaskProgressEvent getLastProgressEvent();
}
