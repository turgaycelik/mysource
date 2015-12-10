package com.atlassian.jira.task;

import java.util.EventListener;

/**
 * A listener that is told when progress is made on a long running task.
 *
 * @since v3.13
 */
public interface TaskProgressListener extends EventListener
{
    /**
     * Called to indicate that task progress has been made. The passed event indicates the current progress of
     * the task.
     *
     * @param event the current progress of the task. 
     */
    void onProgressMade(TaskProgressEvent event);
}
