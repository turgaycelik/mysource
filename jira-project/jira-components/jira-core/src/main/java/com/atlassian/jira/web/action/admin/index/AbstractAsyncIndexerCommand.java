package com.atlassian.jira.web.action.admin.index;

import com.atlassian.jira.task.ProvidesTaskProgress;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.index.Contexts;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * A base class that allows re-index to be done via the Task framework.
 *
 * @since v3.13
 */
abstract class AbstractAsyncIndexerCommand implements Callable<IndexCommandResult>, ProvidesTaskProgress
{
    private final JohnsonEventContainer eventCont;
    private final IndexLifecycleManager indexManager;
    private final Logger log;
    private final I18nHelper i18nHelper;
    private final I18nHelper.BeanFactory i18nBeanFactory;
    private volatile TaskProgressSink taskProgressSink;

    public AbstractAsyncIndexerCommand(final JohnsonEventContainer eventCont, final IndexLifecycleManager indexManager, final Logger log, final I18nHelper i18nHelper, final I18nHelper.BeanFactory i18nBeanFactory)
    {
        Assertions.notNull("indexManager", indexManager);
        Assertions.notNull("log", log);
        Assertions.notNull("i18nHelper", i18nHelper);

        this.eventCont = eventCont;
        this.indexManager = indexManager;
        this.log = log;
        this.i18nHelper = i18nHelper;
        this.i18nBeanFactory = i18nBeanFactory;
    }

    public IndexCommandResult call()
    {
        // We use a null user here to get the Johnson message in the default language for this JIRA instance.
        final String johnsonMessage = i18nBeanFactory.getInstance((ApplicationUser) null).getText("admin.reindex.in.progress.johnson.summary");
        final Event appEvent = new Event(EventType.get("reindex"), johnsonMessage, EventLevel.get(EventLevel.WARNING));
        if (eventCont != null)
        {
            eventCont.addEvent(appEvent);
        }
        try
        {
            final Context context = Contexts.percentageReporter(indexManager, taskProgressSink, i18nHelper, log, appEvent);
            log.info("Re-indexing started");
            return doReindex(context, indexManager);
        }
        finally
        {
            if (eventCont != null)
            {
                eventCont.removeEvent(appEvent);
            }
            log.info("Re-indexing finished");
        }
    }

    public Logger getLog()
    {
        return log;
    }

    public I18nHelper getI18nHelper()
    {
        return i18nHelper;
    }

    public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
    {
        this.taskProgressSink = taskProgressSink;
    }

    public abstract IndexCommandResult doReindex(Context context, IndexLifecycleManager indexManager);
}
