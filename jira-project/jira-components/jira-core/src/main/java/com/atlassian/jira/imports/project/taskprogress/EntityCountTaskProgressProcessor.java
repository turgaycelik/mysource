package com.atlassian.jira.imports.project.taskprogress;

import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.I18nHelper;

/**
 * TaskProgressProcessor that knows the entity count for a backup XML file, or other project import subtask.
 * This way it can give an accurate picture of percentage complete for this task.
 * Also, processing each task is just one part of a bigger picture, so the Processor accepts a startPercentage, and endPercentage
 * value that describes which section of the overall progress this subtask is for.
 *
 * @since v3.13
 */
public class EntityCountTaskProgressProcessor extends AbstractSubtaskProgressProcessor implements TaskProgressProcessor
{
    private final TaskProgressSink taskProgressSink;
    private final String subTaskName;
    private final I18nHelper i18n;

    public EntityCountTaskProgressProcessor(final TaskProgressInterval taskProgressInterval, final String subTaskName, final int numEntities, final I18nHelper i18n)
    {
        super(taskProgressInterval, numEntities);
        taskProgressSink = (taskProgressInterval == null) ? null : taskProgressInterval.getTaskProgressSink();
        this.subTaskName = subTaskName;
        this.i18n = i18n;
    }

    public void processTaskProgress(final String entityTypeName, final int entityTypeCount, final long entityCount, final long currentEntityCount)
    {
        processTaskProgress(entityTypeName, entityCount);
    }

    public void processTaskProgress(final String entityTypeName, final long entityCount)
    {
        // Be kind to null taskProgress
        if (taskProgressSink == null)
        {
            return;
        }
        final long percent = getOverallPercentageComplete(entityCount);
        taskProgressSink.makeProgress(percent, subTaskName + ". " + i18n.getText("admin.message.task.progress.processing", entityTypeName),
            i18n.getText("admin.message.task.progress.entity.of", entityCount + "", getNumEntities() + ""));
    }
}
