package com.atlassian.jira.scheduler;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.startup.JiraLauncher;
import com.atlassian.jira.startup.JiraStartupChecklist;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.core.LifecycleAwareSchedulerService;

import org.apache.log4j.Logger;

import static com.atlassian.jira.component.ComponentAccessor.getApplicationProperties;
import static com.atlassian.jira.component.ComponentAccessor.getComponent;

/**
 * Launches the JIRA scheduler.
 */
public class JiraSchedulerLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(JiraSchedulerLauncher.class);

    public void start()
    {
        try
        {
            proceedIfAllClear();
        }
        catch (RuntimeException rte)
        {
            log.fatal("A RuntimeException occurred during JiraSchedulerLauncher servlet context initialisation - " + rte.getMessage() + ".", rte);
            throw rte;
        }
        catch (Error error)
        {
            log.fatal("An Error occurred during JiraSchedulerLauncher servlet context initialisation - " + error.getMessage() + ".", error);
            throw error;
        }
    }

    protected void proceedIfAllClear()
    {
        if (checkAllClear())
        {
            log.info("Starting the JIRA Scheduler....");
            try
            {
                getComponent(LifecycleAwareSchedulerService.class).start();
                log.info("JIRA Scheduler started.");
            }
            catch (SchedulerServiceException e)
            {
                log.error("Error starting scheduler", e);
            }
        }
    }

    protected boolean checkAllClear()
    {
        boolean ok = false;
        if (!JiraStartupChecklist.startupOK())
        {
            log.info("JIRA Scheduler not started: JIRA startup checklist failed.");
        }
        else if (!thereAreNoJohnsonEvents())
        {
            log.info("JIRA Scheduler not started: Johnson events detected.");
        }
        else if (!canCreateScheduler())
        {
            log.info("JIRA Scheduler not started: JIRA not setup yet.");
        }
        else
        {
            ok = true;
        }
        return ok;
    }

    private boolean thereAreNoJohnsonEvents()
    {
        JohnsonEventContainer cont = JohnsonEventContainer.get(ServletContextProvider.getServletContext());
        return cont.getEvents().isEmpty();

    }

    /**
     * This ends up being called back by super.contextInitialized(servletContextEvent);
     *
     * @return true if the JIRA is setup
     */
    protected boolean canCreateScheduler()
    {
        return getApplicationProperties().getString(APKeys.JIRA_SETUP) != null;
    }

    public void stop()
    {
        LifecycleAwareSchedulerService schedulerService = getComponent(LifecycleAwareSchedulerService.class);
        // it can be null if we stop during bootstrapping
        if (schedulerService != null)
        {
            schedulerService.shutdown();
        }
    }
}
