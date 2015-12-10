package com.atlassian.jira.task;

import com.atlassian.jira.util.dbc.Assertions;

/**
 * Sink that divides a range a progress range into a number of steps. For instance, this can be used
 * to divide a percentage range up to progress can be reported.
 * <p/>
 * This can be used to track the progress of a task that is made up of a number of individual
 * steps. For example, migrating issue workflow requires that each issue in a project be updated
 * with the new workflow. This class can be used to output a percentage based on the number
 * of issues currently processed using:
 * <p/>
 * <pre>
 *   TaskProgressSink stepSink = new StepTaskProgressSink(0, 100, numberOfIssues, rootSink).
 *   for (each issue)
 *   {
 *      stepSink.makeProgress(currentIssue, "Migrating Issue Workflow", "Processing issue.....");
 *   }
 * </pre>
 *
 * @since v3.13
 */
public class StepTaskProgressSink implements TaskProgressSink
{
    private final TaskProgressSink delegateSink;

    /**
     * Create the Sink with the passed parameters.
     *
     * @param startProgress the start of the range.
     * @param endProgress   the end of the range.
     * @param actions       the number of actions to divide the range into.
     * @param sink          the sink actually used to publish the results.
     */
    public StepTaskProgressSink(final long startProgress, final long endProgress, final long actions, final TaskProgressSink sink)
    {
        Assertions.not("actions must be >= 0", actions < 0);
        Assertions.not("startProgress must be < endProgress.", startProgress > endProgress);
        Assertions.notNull("sink", sink);

        delegateSink = new ScalingTaskProgessSink(startProgress, endProgress, 0, actions, sink);
    }

    /**
     * This method can be called to indicate that progress is being made by a task. In this case an progress
     * is made by completeing a particular action.
     *
     * @param taskProgress   the current step.
     * @param currentSubTask the name of the current sub task or null if there isnt one
     * @param message        an optional message about the progress or null
     */
    public void makeProgress(final long taskProgress, final String currentSubTask, final String message)
    {
        delegateSink.makeProgress(taskProgress, currentSubTask, message);
    }
}
