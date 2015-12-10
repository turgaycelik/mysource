package com.atlassian.jira.imports.project.taskprogress;


/**
 * Processes a complete percentage based on the inputs of a subtask and its progress.
 *
 * @since v3.13
 */
public class AbstractSubtaskProgressProcessor
{
    private final int numEntities;
    private final long startPercentage;
    private final long interval;

    public AbstractSubtaskProgressProcessor(final TaskProgressInterval taskProgressInterval, final int numEntities)
    {
        if (taskProgressInterval == null)
        {
            // Just make a no-op processor
            this.numEntities = numEntities;
            startPercentage = 0;
            interval = 0;
        }
        else
        {
            this.numEntities = numEntities;
            startPercentage = taskProgressInterval.getStartPercent();
            interval = taskProgressInterval.getEndPercent() - taskProgressInterval.getStartPercent();
        }
    }

    public int getNumEntities()
    {
        return numEntities;
    }

    /**
     * Gets the percentage taking into account the subtask that this was made with.
     * @param entityCount the current entity count.
     * @return the overall percentage that can be used to make progress on a task progress bean.
     */
    protected long getOverallPercentageComplete(final long entityCount)
    {
        if (numEntities == 0)
        {
            // Trivial for divide by zero exception.
            return startPercentage;
        }
        else
        {
            return startPercentage + ((interval * entityCount) / numEntities);
        }
    }
}
