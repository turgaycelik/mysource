package com.atlassian.jira.startup;

import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;

import static com.atlassian.johnson.event.EventLevel.ERROR;

/**
 * Launcher for the {@link JiraDatabaseConfigChecklist}.
 *
 * @since v4.4
 */
public class DatabaseChecklistLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(DatabaseChecklistLauncher.class);
    private final DatabaseConfigurationManager dbcm;
    private final ServletContext servletContext;
    private final JiraProperties jiraSystemProperties;

    public DatabaseChecklistLauncher(final DatabaseConfigurationManager dbcm, final ServletContext servletContext,
            final JiraProperties jiraSystemProperties)
    {
        this.dbcm = dbcm;
        this.servletContext = servletContext;
        this.jiraSystemProperties = jiraSystemProperties;
    }

    @Override
    public void start()
    {
        final JiraDatabaseConfigChecklist jiraPostDatabaseChecklist = new JiraDatabaseConfigChecklist(dbcm, jiraSystemProperties);
        if (jiraPostDatabaseChecklist.startupOK())
        {
            log.info("JIRA database startup checks completed successfully.");
        }
        else
        {
            final StartupCheck failedCheck = jiraPostDatabaseChecklist.getFailedStartupCheck();
            final String desc = failedCheck.getFaultDescription();
            log.fatal(failedCheck.getName() + " failed: " + desc);
            log.fatal("Database startup check failed.");
            final EventType eventType = EventType.get("database");
            final Event event = new Event(eventType, failedCheck.getHTMLFaultDescription(), EventLevel.get(ERROR));
            JohnsonEventContainer.get(servletContext).addEvent(event);
        }
    }

    @Override
    public void stop()
    {
    }
}
