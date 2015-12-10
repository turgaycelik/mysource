package com.atlassian.jira.task;

import java.io.Serializable;

/**
 * This interface is used by parts of the code that kick off tasks to give the task a "context" to operate in.
 * <p/>
 * This MUST implement the equals/hashCode design pattern because the TaskManager uses it to prevent
 * multiple submissions of task with the same "context".
 *
 * @since v3.13
 */
public interface TaskContext extends Serializable
{
    /**
     * This factory method is called to build a progress URL so the TaskDescriptor can allow some one to navigate to the
     * task's "web page".  The URL should start with "/" and be ready for the servlet context path to be prepended.
     *
     * @param taskId - the id of the task.  Since this is not known until task submission, this call back is informed of it once created.
     * @return returns a context specific progress URL that a user can go to to view the progress of a task.
     */
    String buildProgressURL(Long taskId);
}
