package com.atlassian.jira.imports.project.taskprogress;

import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.I18nHelper;

/**
 * TaskProgressProcessor used in the first pass of the import backup file.
 * At this stage we don't know the count of entities, just the number of entity types.
 *
 * @since v3.13
 */
public class EntityTypeTaskProgressProcessor implements TaskProgressProcessor
{
    private final int numEntityTypes;
    private final TaskProgressSink taskProgressSink;
    private final I18nHelper i18n;

    /**
     *
     * @param numEntityTypes the total number of top-level entities we believe this SAX handler will encounter
     *                          (should be the number of entities registered with OfBiz).
     * @param taskProgressSink
     * @param i18n allows us to i18n text
     */
    public EntityTypeTaskProgressProcessor(final int numEntityTypes, final TaskProgressSink taskProgressSink, final I18nHelper i18n)
    {
        this.numEntityTypes = numEntityTypes;
        this.taskProgressSink = taskProgressSink;
        this.i18n = i18n;
    }

    public void processTaskProgress(final String entityTypeName, final int entityTypeCount, final long entityCount, final long currentEntityCount)
    {
        // Be kind to null taskProgress
        if (taskProgressSink == null)
        {
            return;
        }
        final int percent = ((entityTypeCount - 1) * 100) / numEntityTypes;
        taskProgressSink.makeProgress(percent, i18n.getText("admin.message.task.progress.processing", entityTypeName), currentEntityCount + "");
    }
}
