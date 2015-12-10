package com.atlassian.jira.startup;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.webwork.WebworkConfigurator;

import org.apache.log4j.Logger;

/**
 * The ComponentContainerLauncher will launch PICO to creates its container of components.  The plugins system is not started, but the full list of PICO
 * components will be built.
 */
public class ComponentContainerLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(ComponentContainerLauncher.class);

    public void start()
    {
        /*
         See JRA-13850 - On app servers like Websphere 5.1, if the servlet context init fails, the
         app server will "silently" hide the root cause of the problem.

         In one example we had  a NoClassDefError but could not find it easily
         because WAS lost the exception in its 1.9 millions lines of Enterprise Java
         layers.

         So we need to explicitly log and re-throw the problem.
         */
        try
        {
            populateFullPicoContainer();
        }
        catch (RuntimeException rte)
        {
            log.fatal("A RuntimeException occurred during ComponentContainerLauncher servlet context initialisation - " + rte.getMessage() + ".", rte);
            throw rte;
        }
        catch (Error error)
        {
            log.fatal("An Error occurred during ComponentContainerLauncher servlet context initialisation - " + error.getMessage() + ".", error);
            throw error;
        }
    }

    private void populateFullPicoContainer()
    {
        if (JiraStartupChecklist.startupOK())
        {
            // Bootstrap all the required components
            try
            {
                ComponentManager componentManager = ComponentManager.getInstance();
                // if we have already been bootstrapped then shut it down first
                if (componentManager.getState().isContainerInitialised())
                {
                    componentManager.shutdown();
                }

                componentManager.initialise();
                // we need to have configuration setup for webwork here
                WebworkConfigurator.setupConfiguration();
            }
            catch (Exception ex)
            {
                log.fatal("A fatal error occurred during initialisation. JIRA has been locked.", ex);
                String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
                JiraStartupChecklist.setFailedStartupCheck(new FailedStartupCheck("Component Manager", message));
            }
        }
        else
        {
            log.fatal("Not initializing JIRA, the startup checklist failed and JIRA has been locked.");
        }
    }

    public void stop()
    {
        ComponentManager.getInstance().dispose();
    }
}
