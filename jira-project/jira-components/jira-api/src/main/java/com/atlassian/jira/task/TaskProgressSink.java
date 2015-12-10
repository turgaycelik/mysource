package com.atlassian.jira.task;

/**
 * Interface that can be used by tasks to indicate progress.
 *
 * @since v3.13
 */
public interface TaskProgressSink
{
    /**
     * A simple sink that does nothing.
     */
    public static final TaskProgressSink NULL_SINK = new TaskProgressSink()
    {
        public void makeProgress(final long taskProgress, final String currentSubTask, final String message)
        {
        /* do nothing in the null sink. */
        }
    };

    /**
     * This method can be called to indicate that progress is being made by a task.
     *
     * @param taskProgress   an amount that indicates what progress has been made.
     * @param currentSubTask the name of the current sub task or null if there isnt one
     * @param message        an optional message about the progress or null
     */
    void makeProgress(long taskProgress, String currentSubTask, String message);
}
