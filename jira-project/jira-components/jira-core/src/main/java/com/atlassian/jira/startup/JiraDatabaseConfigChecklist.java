package com.atlassian.jira.startup;

import com.atlassian.jira.appconsistency.db.Build178SchemaCheck;
import com.atlassian.jira.appconsistency.db.BuildVersionCheck;
import com.atlassian.jira.appconsistency.db.MinimumUpgradableVersionCheck;
import com.atlassian.jira.appconsistency.db.PostgresSchemaConfigCheck;
import com.atlassian.jira.appconsistency.db.PublicSchemaConfigCheck;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.database.ManagedDatasourceInfoSupplier;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Represents the set of sanity checks that must be done as soon as database config is known. This is a startup
 * type of check, but on setup, the database configuration is yet to be provided, so we have to wait until it is
 * in that case.
 *
 * @since v4.4
 */
public class JiraDatabaseConfigChecklist
{
    private static final Logger log = Logger.getLogger(JiraDatabaseConfigChecklist.class);
    private final JiraStartupLogger startupLogger = new JiraStartupLogger();
    private final StartupCheck[] checklist;
    private volatile boolean checksDone;
    private volatile boolean success;
    private StartupCheck failedStartupCheck = null;

    public JiraDatabaseConfigChecklist(final DatabaseConfigurationManager databaseConfigurationManager, final JiraProperties jiraSystemProperties)
    {
        this(new StartupCheck[] {
                new PostgresSchemaConfigCheck(new ManagedDatasourceInfoSupplier(databaseConfigurationManager), new ExternalLinkUtilImpl()),
                new PublicSchemaConfigCheck(new ManagedDatasourceInfoSupplier(databaseConfigurationManager)),
                new MinimumUpgradableVersionCheck(),
                new Build178SchemaCheck(),
                new BuildVersionCheck(jiraSystemProperties)
        });
    }

    JiraDatabaseConfigChecklist(final StartupCheck[] checklist)
    {
        this.checklist = checklist;
    }

    public boolean startupOK()
    {
        if (!checksDone)
        {
            log.debug("Checks not done, doing them now");
            success = doStartupChecks();
            checksDone = true;
        }
        return success;
    }

    private boolean doStartupChecks()
    {
        log.debug("Doing database config checklist");
        for (final StartupCheck startupCheck : checklist)
        {
            log.debug("Doing startup check " + startupCheck.getName());
            if (!startupCheck.isOk())
            {
                // Log the Checker's fault message
                startupLogger.printMessage(startupCheck.getFaultDescription(), Level.FATAL);
                failedStartupCheck = startupCheck;
                return false;
            }
        }
        // All checks passed
        return true;
    }

    /**
     * Returns the {@link StartupCheck} that failed, if any or null if none.
     * @return null or the failed {@link StartupCheck}.
     */
    public StartupCheck getFailedStartupCheck()
    {
        return failedStartupCheck;
    }
}
