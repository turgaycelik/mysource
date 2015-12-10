package com.atlassian.jira.web.action.setup;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.dataimport.DataImportService;
import com.atlassian.jira.bc.dataimport.DefaultDataImportService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.RequestCacheKeys;
import com.atlassian.jira.task.CompositeProgressSink;
import com.atlassian.jira.task.JohnsonEventProgressSink;
import com.atlassian.jira.task.LoggingProgressSink;
import com.atlassian.jira.task.ProvidesTaskProgress;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.seraph.spi.rememberme.RememberMeTokenDao;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;
import java.util.concurrent.Callable;

/**
 * A helper class to run a data import asynchronously via the {@link com.atlassian.jira.task.ImportTaskManager}.
 *
 * @since v4.4
 */
public class DataImportAsyncCommand  implements Callable<DataImportService.ImportResult>, ProvidesTaskProgress
{
    private final JohnsonEventContainer eventCont;
    private final DataImportService dataImportService;
    private final User loggedInUser;
    private final DataImportService.ImportValidationResult validationResult;
    private final Event johnsonEvent;
    private final VelocityRequestContext velocityRequestContext;
    private final HttpSession session;
    private TaskProgressSink taskProgressSink;

    public DataImportAsyncCommand(final JohnsonEventContainer eventCont, final DataImportService dataImportService,
            final User loggedInUser, DataImportService.ImportValidationResult validationResult, Event johnsonEvent,
            VelocityRequestContext velocityRequestContext, HttpSession session)
    {
        this.eventCont = eventCont;
        this.dataImportService = dataImportService;
        this.loggedInUser = loggedInUser;
        this.validationResult = validationResult;
        this.johnsonEvent =  johnsonEvent;
        this.velocityRequestContext = velocityRequestContext;
        this.session = session;
    }

    @Override
    public DataImportService.ImportResult call() throws Exception
    {
        if (eventCont != null)
        {
            eventCont.addEvent(johnsonEvent);
        }
        //setup the velocity request context with the context from the calling thread! The plugins system needs this in
        //order to initialise gadgets correctly.
        JiraAuthenticationContextImpl.getRequestCache().put(RequestCacheKeys.VELOCITY_REQUEST_CONTEXT, velocityRequestContext);

        try
        {
            final DataImportService.ImportResult result = dataImportService.doImport(loggedInUser, validationResult, taskProgressSink);
            if(result.isValid())
            {
                //destroy the user's session so they get logged out on a successful import!
                if(session != null)
                {
                    session.invalidate();
                }
                ComponentAccessor.getComponentOfType(RememberMeTokenDao.class).removeAll();
            }
            return result;
        }
        finally
        {
            if (eventCont != null)
            {
                eventCont.removeEvent(johnsonEvent);
            }
            //finally reset this thread's context.
            JiraAuthenticationContextImpl.getRequestCache().remove(RequestCacheKeys.VELOCITY_REQUEST_CONTEXT);
        }
    }

    @Override
    public void setTaskProgressSink(TaskProgressSink taskProgressSink)
    {
        this.taskProgressSink = new CompositeProgressSink(taskProgressSink, new JohnsonEventProgressSink(johnsonEvent),
                new LoggingProgressSink(Logger.getLogger(DefaultDataImportService.class), "Importing data is {0}% complete...", 1));
    }

}
