package com.atlassian.jira.task;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A task progress sink that is made up of several other task progress sinks.
 * <p/>
 * It can be used to log progress against several sinks simultaneously.
 *
 * @since v4.4
 */
public class CompositeProgressSink implements TaskProgressSink
{
    private final Collection<TaskProgressSink> delegates;

    public CompositeProgressSink(final TaskProgressSink... delegates)
    {
        this.delegates = Collections.unmodifiableList(Arrays.asList(delegates));
    }

    @Override
    public void makeProgress(long taskProgress, String currentSubTask, String message)
    {
        for (TaskProgressSink delegate : delegates)
        {
            delegate.makeProgress(taskProgress, currentSubTask, message);
        }
    }
}
