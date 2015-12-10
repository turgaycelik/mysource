package com.atlassian.jira.task;

import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link TaskProgressSink} thats keeps track of progress. It keeps track of the last progress stored
 * sent. The actual reporting of progress will be made through another sink.
 *
 * @since v3.13
 */
public class StatefulTaskProgressSink implements TaskProgressSink
{
    private final TaskProgressSink delegateSink;
    private long currentProgress;
    private final long maxProgress;
    private final long minProgress;

    /**
     * Create the sink.
     *
     * @param minProgress     the smallest task progress that the sink can report.
     * @param maxProgress     the largest task progress that the sink can report.
     * @param currentProgress the initial task progress stored within the sink.
     * @param delegateSink    the sink that will be used for reporting.
     */
    public StatefulTaskProgressSink(final long minProgress, final long maxProgress, final long currentProgress, final TaskProgressSink delegateSink)
    {
        if (minProgress > maxProgress)
        {
            throw new IllegalArgumentException("minProgress must be < maxProgress.");
        }

        Assertions.notNull("delegateSink", delegateSink);

        this.minProgress = minProgress;
        this.maxProgress = maxProgress;
        this.delegateSink = delegateSink;
        this.currentProgress = StatefulTaskProgressSink.clamp(minProgress, maxProgress, currentProgress);
    }

    /**
     * Create the sink. The initial current progress stored in the sink will be set to its configured minimum.
     *
     * @param minProgress  the smallest task progress that the sink can report.
     * @param maxProgress  the largest task progress that the sink can report.
     * @param delegateSink the sink that will be used for reporting.
     */
    public StatefulTaskProgressSink(final long minProgress, final long maxProgress, final TaskProgressSink delegateSink)
    {
        this(minProgress, maxProgress, minProgress, delegateSink);
    }

    /**
     * This method can be called to indicate that progress is being made by a
     * task.
     *
     * @param taskProgress   the current status of the task. This value will be clamped
     *                       between the minimum and maximum progress specified when the object is constructed.
     * @param currentSubTask the name of the current sub task or null if there isn't one
     * @param message        an optional message about the progress or null
     */
    public void makeProgress(final long taskProgress, final String currentSubTask, final String message)
    {
        setProgress(taskProgress);
        delegateSink.makeProgress(getProgress(), currentSubTask, message);
    }

    /**
     * This method can be called to indicate that progress is being made by a
     * task. The current status is incremented by the passed amount.
     *
     * @param increment      the amount to increment the current progress by.
     * @param currentSubTask the name of the current sub task or null if there isn't one
     * @param message        an optional message about the progress or null
     */
    public void makeProgressIncrement(final long increment, final String currentSubTask, final String message)
    {
        makeProgress(getProgress() + increment, currentSubTask, message);
    }

    /**
     * Send a new progress message without changing the current task progress.
     *
     * @param currentSubTask the name of the current sub task or null if there isn't one
     * @param message        an optional message about the progress or null
     */
    public void makeProgress(final String currentSubTask, final String message)
    {
        delegateSink.makeProgress(getProgress(), currentSubTask, message);
    }

    /**
     * Create a {@link StepTaskProgressSink} that maps its progress onto a specific range of this
     * sink. This can be used to keep track of a part of a task that is divided into a number of
     * smaller steps.
     *
     * @param startProgress   the start of the range on this sink to map back to.
     * @param length          the length of the range on this sink the view will map back to.
     * @param numberOfActions to number of steps the returned sink should work for.
     * @return a {@link StepTaskProgressSink} that will publish its results in the specified
     *         range of this sink.
     */
    public StepTaskProgressSink createStepSinkView(long startProgress, final long length, final int numberOfActions)
    {
        if (length < 0)
        {
            throw new IllegalArgumentException("length must be >= 0.");
        }
        if (numberOfActions < 0)
        {
            throw new IllegalArgumentException("numberOfActions must be >= 0.");
        }

        startProgress = clamp(startProgress);
        final long endProgress = clamp(startProgress + length);

        return new StepTaskProgressSink(startProgress, endProgress, numberOfActions, this);
    }

    /**
     * Create a {@link StepTaskProgressSink} that maps its progress onto a specific range of this
     * sink. This can be used to keep track of a part of a task that is divided into a number of
     * smaller steps. The start of the range is taken to be the current progress stored in this
     * project.
     *
     * @param length          the length of the range on this sink the view will map back to.
     * @param numberOfActions to number of steps the returned sink should work for.
     * @return a {@link StepTaskProgressSink} that will publish its results in the specified
     *         range of this sink.
     */
    public StepTaskProgressSink createStepSinkView(final long length, final int numberOfActions)
    {
        return createStepSinkView(getProgress(), length, numberOfActions);
    }

    /**
     * Return a collection of sinks that allows progress to be reported across the specified ragnge. Each sink
     * will service a small section of that range. This is commonly used when a task can be divided up into
     * a number of steps that each need to process their progress independently. For example:
     * <p/>
     * <pre>
     *  Collection projects = ...;
     *  Iterator<StatefulTaskProgressSink> sinkIterator = StatefulTaskProgressSink.createPercentageSinksForRange(0, 100, projects.size(), ...);
     *  for (project : projects)
     *  {
     *      migrateProject(projects, sinkIterator.next());
     *  }
     * </pre>
     *
     * @param startRange        start of the progress range to divide.
     * @param endRange          end of the progress range to divide.
     * @param numberOfDivisions the number of intervals in the range.
     * @param sink              the sink used to report progress.
     * @return a Collection of StateTaskProgressSink that divides the interval as specified.
     */
    public static Collection<StatefulTaskProgressSink> createPercentageSinksForRange(final long startRange, final long endRange, final int numberOfDivisions, final TaskProgressSink sink)
    {
        Assertions.notNull("sink", sink);
        Assertions.not("startRange must be <= endRange.", startRange > endRange);
        Assertions.not("divisions must be >= 0", numberOfDivisions < 0);

        if (numberOfDivisions > 0)
        {
            long currentStart = startRange;
            final double increment = (double) (endRange - startRange) / numberOfDivisions;

            final List<StatefulTaskProgressSink> list = Lists.newArrayListWithCapacity(numberOfDivisions);
            for (int i = 0; i < numberOfDivisions - 1; i++)
            {
                final long nextStart = Math.min(Math.round(currentStart + increment), endRange);
                list.add(new StatefulTaskProgressSink(0, 100, new ScalingTaskProgessSink(currentStart, nextStart, 0, 100, sink)));
                currentStart = nextStart;
            }
            list.add(new StatefulTaskProgressSink(0, 100, new ScalingTaskProgessSink(currentStart, endRange, 0, 100, sink)));
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * @return the progress recorded in this object. It is the last progress sent.
     */
    public long getProgress()
    {
        return currentProgress;
    }

    /**
     * Set the progress stored in the sink.
     *
     * @param currentProgress the progress to store in sink.
     */
    public void setProgress(final long currentProgress)
    {
        this.currentProgress = clamp(currentProgress);
    }

    /** 
     * @return the largest progress allowed.
     */
    public long getMaxProgress()
    {
        return maxProgress;
    }

    /**
     * @return the smallest progess allowed.
     */
    public long getMinProgress()
    {
        return minProgress;
    }

    private long clamp(final long value)
    {
        return StatefulTaskProgressSink.clamp(minProgress, maxProgress, value);
    }

    private static long clamp(long min, long max, final long value)
    {
        if (min > max)
        {
            final long tmp = min;
            min = max;
            max = tmp;
        }

        if (value < min)
        {
            return min;
        }
        else if (value > max)
        {
            return max;
        }
        return value;
    }
}
