package com.atlassian.jira.task;

import com.atlassian.jira.util.dbc.Assertions;

/**
 * Takes the "virtual progress" made and scales it into an "actual progress" for the contained sink. For example,
 * this can take the progress range from virtual range [0, 100] and map it to the  actual range [23, 47] on the
 * contained sink.
 *
 * @since v3.13
 */
public class ScalingTaskProgessSink implements TaskProgressSink
{
    private final long virtualStart;
    private final long virtualEnd;
    private final long actualStart;
    private final long actualEnd;
    private final double scale;
    private final TaskProgressSink delegateSink;

    /**
     * Create the sink with the specified virtual and actual progress ranges.
     *
     * @param actualStart  the start of the actual range inclusive.
     * @param actualEnd    the end of the actual range inclusive.
     * @param virtualStart the start of the virtual range inclusive.
     * @param virtualEnd   the end of the virtual range inclusive.
     * @param delegateSink the sink to actually report progress on.
     */
    public ScalingTaskProgessSink(final long actualStart, final long actualEnd, final long virtualStart, final long virtualEnd, final TaskProgressSink delegateSink)
    {
        Assertions.notNull("delegateSink", delegateSink);
        Assertions.not("virtualEnd should be >= virtualStart.", virtualStart > virtualEnd);
        Assertions.not("actualEnd should be >= actualStart.", actualStart > actualEnd);

        this.delegateSink = delegateSink;
        this.virtualStart = virtualStart;
        this.virtualEnd = virtualEnd;
        this.actualStart = actualStart;
        this.actualEnd = actualEnd;

        if (actualEnd != actualStart)
        {
            scale = (double) (actualEnd - actualStart) / (virtualEnd - virtualStart);
        }
        else
        {
            scale = 0;
        }
    }

    /**
     * Create the sink with the specified actual range. The virtual range will be set to [0, 100].
     *
     * @param actualStart  the start of the actual range inclusive.
     * @param actualEnd    the end of the actual range inclusive.
     * @param delegateSink the sink to actually report progress on.
     */
    public ScalingTaskProgessSink(final long actualStart, final long actualEnd, final TaskProgressSink delegateSink)
    {
        this(actualStart, actualEnd, 0L, 100L, delegateSink);
    }

    /**
     * This method can be called to indicate that progress is being made by a task.
     *
     * @param taskProgress   the current "virtual progress" for the task.
     * @param currentSubTask the name of the current sub task or null if there isnt one
     * @param message        an optional message about the progress or null
     */
    public void makeProgress(long taskProgress, final String currentSubTask, final String message)
    {
        if (taskProgress < virtualStart)
        {
            taskProgress = virtualStart;
        }
        else if (taskProgress > virtualEnd)
        {
            taskProgress = virtualEnd;
        }

        if (taskProgress == virtualEnd)
        {
            delegateSink.makeProgress(actualEnd, currentSubTask, message);
        }
        else
        {
            double actualProgress = taskProgress - virtualStart;
            actualProgress = actualProgress * scale + actualStart;
            delegateSink.makeProgress(Math.min(Math.round(actualProgress), actualEnd), currentSubTask, message);
        }
    }
}
