package com.atlassian.jira.task.context;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.task.context.PercentageContext.Sink;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A {@link Context} implementation that doesn't update percentage progress as it doesn't have a usable total.
 *
 * @since v3.13
 */
@Internal
class UnboundContext implements Context
{
    private static final Task TASK = new Task()
    {
        public void complete()
        {
        }
    };

    // Reuse the PercentageContext.Sink for now, consider other implementation if required
    private final Sink sink;

    UnboundContext(final Sink sink)
    {
        this.sink = notNull("sink", sink);
        this.sink.updateProgress(100);
    }

    public void setName(final String name)
    {
        sink.setName(name);
    }

    public Task start(final Object input)
    {
        return TASK;
    }

    @Override
    public int getNumberOfTasksToCompletion()
    {
        // There is nothing to report, so we have no idea how many tasks are left.
        return 0;
    }
}
