package com.atlassian.jira.task;

import com.atlassian.jira.util.dbc.Assertions;

import java.io.Serializable;
import java.util.Date;

/**
 * Event that represents the progress of a long running task.
 *
 * @since v3.13
 */
public class TaskProgressEvent implements Serializable
{
    private static final long serialVersionUID = -3295135558200242598L;

    private final Date creationTimeStamp;
    private final Long taskId;
    private final long elapsedRunTime;
    private final long taskProgress;
    private final String message;
    private final String currentSubTask;

    /**
     * Create an event and initialise it with the passed parameters.
     *
     * @param taskId the identifier of the task that generated the event.
     * @param elapsedRunTime the elapsed run time to store in the event.
     * @param taskProgress the progress to store in the event.
     * @param currentSubTask the current sub-task to store in the event.
     * @param message the current message to store in the event.
     */
    public TaskProgressEvent(final Long taskId, final long elapsedRunTime, final long taskProgress, final String currentSubTask, final String message)
    {
        Assertions.notNull("taskId", taskId);

        this.taskId = taskId;
        this.elapsedRunTime = elapsedRunTime;
        this.taskProgress = taskProgress;
        this.currentSubTask = currentSubTask;
        this.message = message;
        creationTimeStamp = new Date();
    }

    /**
     * This returns the name of the current sub-task stored in the event.
     * It may be null.
     *
     * @return the name of the current sub-task.
     */
    public String getCurrentSubTask()
    {
        return currentSubTask;
    }

    /**
     * Return the elasped run time stored in the event.
     *
     * @return the elapsed run time.
     */
    public long getElapsedRunTime()
    {
        return elapsedRunTime;
    }

    /**
     * The progress stored in the event.
     *
     * @return It will commonly be a number in [0,100] to represent a percentage, though,
     * this is left up to the task to define. A value of -1 can be returned to indicate
     * that the progress is unknown. 
     */
    public long getTaskProgress()
    {
        return taskProgress;
    }

    /**
     * Find the time the event was created.
     *
     * @return the time that this event was created. Can never be null.
     */
    public Date getCreationTimeStamp()
    {
        return new Date(creationTimeStamp.getTime());
    }

    /**
     * Get the message associated with the event.
     *
     * @return the message associated with the event
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Gets the id of the  {@link com.atlassian.jira.task.TaskDescriptor} associated with the task that is running.
     *
     * @return the id of the {@link com.atlassian.jira.task.TaskDescriptor} associated with the task that is running.
     * It cannot be null.
     */
    public Long getTaskId()
    {
        return taskId;
    }


}
