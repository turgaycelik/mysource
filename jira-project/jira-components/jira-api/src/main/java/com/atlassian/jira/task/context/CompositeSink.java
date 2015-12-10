package com.atlassian.jira.task.context;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.task.context.PercentageContext.Sink;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Composite version of the Sink. Takes multiple Sink implementations and calls them in order.
 *
 * @since v3.13
 */
@Internal
class CompositeSink implements Sink
{
    private final List<Sink> delegates;

    CompositeSink(final Sink... delegates)
    {
        Assertions.notNull("delegates", delegates);
        this.delegates = Collections.unmodifiableList(Arrays.asList(delegates));
    }

    public void setName(final String name)
    {
        Assertions.notNull("name", name);
        for (final Object element : delegates)
        {
            final Sink delegate = (Sink) element;
            delegate.setName(name);
        }
    }

    public void updateProgress(final int progress)
    {
        for (final Object element : delegates)
        {
            final Sink delegate = (Sink) element;
            delegate.updateProgress(progress);
        }
    }
}
