package com.atlassian.jira.imports.project.taskprogress;

import com.atlassian.jira.task.TaskProgressSink;

/**
 * Represents an interval of an overall Task Progress bar.
 *
 * @since v3.13
 */
public class TaskProgressInterval
{
    final int startPercent;
    final int endPercent;
    final TaskProgressSink taskProgressSink;

    public TaskProgressInterval(final TaskProgressSink taskProgressSink, final int startPercent, final int endPercent)
    {
        this.taskProgressSink = taskProgressSink;
        this.startPercent = startPercent;
        this.endPercent = endPercent;
    }

    /**
     * Returns a subinterval of this interval.
     * You pass in the start and end percent of the subinterval. A TaskProgressInterval is returned which contains the
     * start and end of the overall progress for the subinterval.
     * <p>
     * eg If the parent interval runs from 20% - 60%, and we ask for a sub interval that runs from 25% - 50% of its parent,
     * then the returned TaskProgressInterval would start at 30% and end at 40%.
     * </p>
     *
     * @param subIntervalStartPercent The start percent of the sub interval within the parent interval.
     * @param subIntervalEndPercent   The end percent of the sub interval within the parent interval.
     * @return a subinterval of this interval.
     */
    public TaskProgressInterval getSubInterval(final int subIntervalStartPercent, final int subIntervalEndPercent)
    {
        final int parentIntervalLength = endPercent - startPercent;
        final int overallStartPercent = getStartPercent() + (subIntervalStartPercent * parentIntervalLength / 100);
        final int overallEndPercent = getStartPercent() + (subIntervalEndPercent * parentIntervalLength / 100);
        return new TaskProgressInterval(taskProgressSink, overallStartPercent, overallEndPercent);
    }

    /**
     * Returns the overall percentage done at which this interval starts.
     * @return the overall percentage done at which this interval starts.
     */
    public int getStartPercent()
    {
        return startPercent;
    }

    /**
     * Returns the overall percentage done at which this interval ends.
     * @return the overall percentage done at which this interval ends.
     */
    public int getEndPercent()
    {
        return endPercent;
    }

    /**
     * Returns the TaskProgressSink that this interval is to be used for.
     * @return the TaskProgressSink that this interval is to be used for.
     */
    public TaskProgressSink getTaskProgressSink()
    {
        return taskProgressSink;
    }

}
