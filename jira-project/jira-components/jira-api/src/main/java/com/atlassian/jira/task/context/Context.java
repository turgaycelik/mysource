package com.atlassian.jira.task.context;

import com.atlassian.annotations.PublicApi;

/**
 * Task context. Can be used to provide status updates to clients.
 * <p/>
 * Null is not allowed for any method arguments or return values.
 * <p/>
 * Instances should internally protect any state from concurrent updates and so should
 * require no external synchronisation if shared by multiple threads.
 *
 * @since v3.13
 */
@PublicApi
public interface Context
{
    /**
     * Set the name of the current step.
     *
     * @param name the name
     */
    void setName(String name);

    /**
     * Start a new sub-task. These should be completed in a <pre>finally</pre> block.
     *
     * @param input the object of the task, can be used for tracking currently executing tasks.
     * @return the new Task.
     */
    Task start(Object input);

    /**
     * Returns number of tasks not started yet, after finishing which progress is complete.
     */
    public int getNumberOfTasksToCompletion();

    /**
     * A Task is a unit of work. They should be completed in a <pre>finally</pre> block.
     */
    interface Task
    {
        void complete();
    }
}
