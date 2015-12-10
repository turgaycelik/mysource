package com.atlassian.jira.task.context;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Percentage counting context. Reports the percentage to the supplied {@link ContextSink}
 * when the percentage changes.
 *
 * @since v3.13
 */
@Internal
class PercentageContext implements Context
{
    private final Sink sink;
    private final Progress progress;
    private final Task task = new Task()
    {
        public void complete()
        {
            progress.increment();
        }
    };

    public PercentageContext(final int total, final Sink sink)
    {
        Assertions.notNull("sink", sink);
        this.sink = sink;
        progress = new Progress(total);
    }

    public Task start(final Object input)
    {
        return task;
    }

    public void setName(final String string)
    {
        sink.setName(string);
        progress.update();
    }

    public int getNumberOfTasksToCompletion()
    {
        return progress.tasksNotStarted();
    }

    /**
     * Used to notify interested parties of progress.
     */
    interface Sink
    {
        /**
         * What index is currently being processed.
         *
         * @param currentIndex
         */
        void setName(final String currentIndex);

        /**
         * Progress has been made. Should only be called when the int changes, not after every task.
         *
         * @param progress an int between 0-100
         */
        void updateProgress(final int progress);
    }

    /**
     * Adaptor that works out the current percentage and then updates the sink if the percentage has changed.
     */
    private class Progress
    {
        private final PercentageProgressCounter counter;
        private final int total;

        Progress(final int total)
        {
            counter = new PercentageProgressCounter(total);
            this.total = total;
        }

        void increment()
        {
            if (counter.increment())
            {
                update();
            }
        }

        int tasksNotStarted()
        {
            return total - counter.getCountComplete();
        }

        void update()
        {
            sink.updateProgress(counter.getPercentComplete());
        }
    }
}
