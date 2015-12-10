package com.atlassian.jira.task;

/**
 * Long running tasks that implement <code>ProvidesTaskProgress</code> will be able to tell the
 * task infrastructure about what progress they are making
 *
 * @since v3.13
 */
public interface ProvidesTaskProgress
{
    /**
     * This is called to set in a TaskProgressSink that can be used to send task progress information to
     *
     * @param taskProgressSink a TaskProgressSink that can be used to send task progress information to
     */
    public void setTaskProgressSink(TaskProgressSink taskProgressSink);
}
