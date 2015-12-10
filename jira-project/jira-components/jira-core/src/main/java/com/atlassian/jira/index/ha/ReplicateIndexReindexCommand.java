package com.atlassian.jira.index.ha;

import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.task.ProvidesTaskProgress;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.index.Contexts;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;


/**
 *  A helper class to reindex the replicated index
 *
 * @since v6.1
 */
public class ReplicateIndexReindexCommand implements Callable<IndexCommandResult>, ProvidesTaskProgress
{

    private final boolean useBackgroundIndexing;
    private final IssueIndexManager indexManager;
    private final JohnsonEventContainer eventContainer;
    private final I18nHelper i18nHelper;
    private final Logger log;
    private volatile TaskProgressSink taskProgressSink;


    public final static String REINDEXING = "The backup JIRA is currently being reindexed. Depending on how large the database is, this may take a few minutes. JIRA will automatically become available as soon as this task is complete.";

    public ReplicateIndexReindexCommand(final boolean useBackgroundIndexing, final IssueIndexManager indexManager, final I18nHelper i18nHelper, final Logger log)
    {
        this.useBackgroundIndexing = useBackgroundIndexing;
        this.indexManager = indexManager;
        this.i18nHelper = i18nHelper;
        this.log = log;
        eventContainer = JohnsonEventContainer.get(ServletContextProvider.getServletContext());
    }

    @Override
    public IndexCommandResult call() throws Exception
    {
        final Event appEvent = new Event(EventType.get("reindex"), REINDEXING, EventLevel.get(EventLevel.WARNING));
        if (!useBackgroundIndexing)
        {
            eventContainer.addEvent(appEvent);
        }
        final Context context = Contexts.percentageReporter(indexManager, taskProgressSink, i18nHelper, log, appEvent);
        log.info("Re-indexing started");
        return new IndexCommandResult(indexManager.reIndexAll(context, useBackgroundIndexing, false)) ;
    }

    @Override
    public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
    {
        this.taskProgressSink = taskProgressSink;
    }
}
