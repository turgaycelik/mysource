package com.atlassian.jira.startup;


import java.io.FileNotFoundException;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.database.DatabaseDriverRegisterer;
import com.atlassian.jira.config.database.InvalidDatabaseDriverException;
import com.atlassian.jira.config.webwork.WebworkConfigurator;
import com.atlassian.jira.config.database.DatabaseType;

import org.apache.log4j.Logger;

/**
 * The BootstrapContainerLauncher will bootstrap enough of JIRA during run level 0.  It has the ability to check the
 * state of the database connection and configure it if need be.  If the database is operational then it will move to
 * the next level and start JIRA completely.
 *
 * @see ComponentContainerLauncher
 */
public class BootstrapContainerLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(BootstrapContainerLauncher.class);

    public void start()
    {
        try
        {
            bootstrapJIRA();
        }
        catch (RuntimeException rte)
        {
            log.fatal("A RuntimeException occurred during BootstrapContainerLauncher servlet context initialisation - " + rte.getMessage() + ".", rte);
            throw rte;
        }
        catch (Error error)
        {
            log.fatal("An Error occurred during BootstrapContainerLauncher servlet context initialisation - " + error.getMessage() + ".", error);
            throw error;
        }
    }

    private void bootstrapJIRA()
    {
        try
        {
            //
            // builds enough of the PICO world to allow JIRA to be setup without a database
            ComponentManager.getInstance().bootstrapInitialise();
            if (JiraStartupChecklist.startupOK())
            {

                //
                // if the database is not setup then we need the plugins system.  However if it is already setup then we can skip this step
                // as save ourselves some time by skipping the bootstrap plugins system
                //
                DatabaseConfigurationManager dbcm = ComponentAccessor.getComponent(DatabaseConfigurationManager.class);
                if (!dbcm.isDatabaseSetup())
                {
                    bootstrapJIRAWhenDBIsNotSetup(dbcm);
                }
            }
        }
        catch (Exception ex)
        {
            log.fatal("A fatal error occurred during bootstrapping. JIRA has been locked.", ex);
            String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            JiraStartupChecklist.setFailedStartupCheck(new FailedStartupCheck("Component Manager", message));
        }
    }

    private void bootstrapJIRAWhenDBIsNotSetup(final DatabaseConfigurationManager dbcm)
    {
        // initialise the SetupContainer
        ComponentManager.getInstance().setupInitialise();
        // check to see if the driver is not setup
        try
        {
            dbcm.getDatabaseConfiguration();
        }
        catch (InvalidDatabaseDriverException e)
        {
            String errorMessage = getErrorMessageForDatabase(e);
            JiraStartupChecklist.setFailedStartupCheck(new FailedStartupCheck("Database Driver Check", errorMessage));
            return;
        }
        catch (Exception e)
        {
            //swallow everytthing except the driver exception
            if (e.getCause() instanceof  FileNotFoundException)
            {
                dbcm.createDbConfigFromEntityDefinition();
            }
        }
        ComponentManager.getInstance().start();

        // we need to have configuration setup for webwork here
        WebworkConfigurator.setupConfiguration();
    }

    public void stop()
    {
        // nothing to stop
    }

    private String getErrorMessageForDatabase(InvalidDatabaseDriverException e)
    {
        DatabaseDriverRegisterer registrar = DatabaseDriverRegisterer.forType(DatabaseType.forJdbcDriverClassName(e.driverClassName()));
        StringBuilder errorMessage = new StringBuilder("");
        if (registrar != null)
        {
            for (String s : registrar.getErrorMessage())
            {
                errorMessage.append(s);
                errorMessage.append("\n");
            }
        }
        else
        {
            errorMessage.append(e.getMessage());
        }
        return errorMessage.toString();
    }
}
