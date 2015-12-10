package com.atlassian.jira.task;

import com.atlassian.johnson.event.Event;

/**
 * A task progress sink that can update a Johnson {@link Event} with progress updates.
 *
 * @since v4.4
 */
public class JohnsonEventProgressSink implements TaskProgressSink
{
    private final Event event;

    public JohnsonEventProgressSink(Event event)
    {
        this.event = event;
    }

    @Override
    public void makeProgress(long taskProgress, String currentSubTask, String message)
    {
        event.setProgress((int) taskProgress);
    }
}
