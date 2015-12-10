package com.atlassian.jira.appconsistency.db;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.startup.StartupCheck;
import com.atlassian.jira.upgrade.util.UpgradeUtils;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This is a database check that verifies that user is not running old version of JIRA on the data in the database
 * created by more recent version of JIRA. This check can be turned off by setting "-Djira.ignore.buildversion=true".
 */
public class BuildVersionCheck implements StartupCheck
{
    private static final Logger log = Logger.getLogger(BuildVersionCheck.class);
    private static final String NAME = "JIRA Build Version Check";
    private static final String JIRA_IGNORE_BUILD_VERSION = "jira.ignore.buildversion";

    private final BuildUtilsInfo buildUtilsInfo = new BuildUtilsInfoImpl();
    private final JiraProperties jiraSystemProperties;

    public BuildVersionCheck(final JiraProperties jiraSystemProperties)
    {
        this.jiraSystemProperties = jiraSystemProperties;
    }

    public String getName()
    {
        return NAME;
    }

    public boolean isOk()
    {
        // Look for the system property, if true do not perform build version check
        if (jiraSystemProperties.getBoolean(JIRA_IGNORE_BUILD_VERSION))
        {
            log.warn("Not performing Jira Build Version check since " + JIRA_IGNORE_BUILD_VERSION + " is set to 'true'");
        }
        else if (databaseSetup())
        {
            log.debug("Performing version check");

            final int applicationBuildVersionNumber = getAppBuildNumber();
            final int databaseBuildVersionNumber = getDbBuildNumber();

            if (databaseBuildVersionNumber > applicationBuildVersionNumber)
            {
                log.debug("There is a data consistency error: Database build number is: " + databaseBuildVersionNumber
                        + ", Application build number is: " + applicationBuildVersionNumber);
                // We have an error so JIRA should not be started!
                return false;
            }
        }
        return true;
    }

    private boolean databaseSetup()
    {
        // check to see if the project table exists
        return UpgradeUtils.tableExists("project");
    }

    public String getHTMLFaultDescription()
    {
        StringBuilder message = new StringBuilder(512);
        message.append("<p>Failed to start JIRA due to a build number inconsistency.</p>");
        message.append("<p>The data present in your database is newer than the version of JIRA you are trying to startup.</p>");
        message.append("<p>Database version is: ").append(getDbBuildNumber()).append("</p>");
        message.append("<p>JIRA app version is: ").append(getAppBuildNumber()).append("</p>");
        message.append("<p>Please use the correct version of JIRA. You are running: ").append(buildUtilsInfo.getBuildInformation()).append("</p>");
        return message.toString();
    }

    @Override
    public void stop()
    {
    }

    public String getFaultDescription()
    {
        StringBuilder message = new StringBuilder(512);
        message.append("\n\n");
        message.append(StringUtils.repeat("*", 100)).append("\n");
        message.append("Failed to start JIRA due to a build number inconsistency.\n");
        message.append("The data present in your database is newer than the version of JIRA you are trying to startup.\n");
        message.append("Database version is: ").append(getDbBuildNumber()).append("\n");
        message.append("JIRA app version is: ").append(getAppBuildNumber()).append("\n");
        message.append("Please use the correct version of JIRA. You are running: ").append(buildUtilsInfo.getBuildInformation()).append("\n");
        message.append(StringUtils.repeat("*", 100)).append("\n");
        return message.toString();
    }

    private int getDbBuildNumber()
    {
        return buildUtilsInfo.getDatabaseBuildNumber();
    }

    private int getAppBuildNumber()
    {
        return buildUtilsInfo.getApplicationBuildNumber();
    }

    @Override
    public String toString()
    {
        return NAME;
    }
}
