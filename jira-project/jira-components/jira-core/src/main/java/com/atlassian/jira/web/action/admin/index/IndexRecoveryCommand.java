package com.atlassian.jira.web.action.admin.index;

import java.util.concurrent.Callable;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.index.ha.IndexRecoveryService;
import com.atlassian.jira.task.ProvidesTaskProgress;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;

import com.google.common.annotations.VisibleForTesting;

import org.apache.log4j.Logger;

/**
 * Recover the index from a backup
 *
 * @since v6.2
 */
@Internal
@VisibleForTesting
public class IndexRecoveryCommand implements Callable<IndexCommandResult>, ProvidesTaskProgress
{
    public static final String JIRA_INDEX_RECOVERY_MSG = "JIRA index is being recovered. Depending on how large the database is, this may take a few minutes. JIRA will automatically become available as soon as this task is complete.";

    private final ApplicationUser user;
    private final IndexRecoveryService indexRecoveryService;
    private final Logger log;
    private final I18nHelper i18nHelper;
    private volatile TaskProgressSink taskProgressSink;
    private String recoveryFilename;

    public IndexRecoveryCommand(final ApplicationUser user, final IndexRecoveryService indexRecoveryService, final Logger log, final I18nHelper i18nHelper, final String recoveryFilename)
    {
        this.user = user;
        this.indexRecoveryService = indexRecoveryService;
        this.log = log;
        this.i18nHelper = i18nHelper;
        this.recoveryFilename = recoveryFilename;
    }

    @Override
    public IndexCommandResult call() throws Exception
    {
        final Event appEvent = new Event(EventType.get("reindex"), JIRA_INDEX_RECOVERY_MSG, EventLevel.get(EventLevel.WARNING));
        try
        {
            final Context context = Contexts.percentageReporter(indexRecoveryService, taskProgressSink, i18nHelper, log, "Index Recovery",
                    "admin.indexing.percent.complete", "admin.indexing.current.index", appEvent);
            log.info("Re-indexing started");
            return indexRecoveryService.recoverIndexFromBackup(user, context, i18nHelper, recoveryFilename, taskProgressSink);
        }
        finally
        {
            log.info("Index Recovery finished");
        }
    }

    @Override
    public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
    {
        this.taskProgressSink = taskProgressSink;
    }
}
