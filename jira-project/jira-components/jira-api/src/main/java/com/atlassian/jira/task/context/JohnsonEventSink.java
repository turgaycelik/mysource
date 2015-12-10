package com.atlassian.jira.task.context;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.task.context.PercentageContext.Sink;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.johnson.event.Event;

/**
 * Sink that writes output to a Johnson {@link Event}.
 *
 * @since v3.13
 */
@Internal
class JohnsonEventSink implements Sink
{
    private final Event event;

    JohnsonEventSink(final Event event)
    {
        Assertions.notNull("event", event);
        this.event = event;
    }

    public void setName(final String currentIndex)
    {
    }

    public void updateProgress(final int progress)
    {
        event.setProgress(progress);
    }
}
