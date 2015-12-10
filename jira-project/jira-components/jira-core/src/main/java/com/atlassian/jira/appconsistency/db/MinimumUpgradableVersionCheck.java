package com.atlassian.jira.appconsistency.db;

import com.atlassian.jira.startup.StartupCheck;
import com.atlassian.jira.upgrade.util.UpgradeUtils;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import org.apache.log4j.Logger;

/**
 * This is a database check that verifies that the data is not too old to be upgraded by this version of JIRA.
 *
 * @since v4.0
 */
public class MinimumUpgradableVersionCheck implements StartupCheck
{
    private static final Logger log = Logger.getLogger(MinimumUpgradableVersionCheck.class);
    private static final String NAME = "JIRA Minimum Upgradable Version Check";
    private static final String INSTRUCTIONS_URL = ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.confluence.upgrade.guide.for.old.versions");

    private final BuildUtilsInfo buildUtilsInfo = new BuildUtilsInfoImpl();

    public String getName()
    {
        return NAME;
    }

    public boolean isOk()
    {
        if (databaseSetup())
        {
            log.debug("Performing version check");

            final int databaseBuildVersionNumber = getDbBuildNumber();

            if (databaseBuildVersionNumber > 0 && databaseBuildVersionNumber < getMinimumUpgradableBuildNumber())
            {
                log.debug(String.format("Your data is too old to be upgraded. Minimum version required: %d, your version: %d", getMinimumUpgradableBuildNumber(), databaseBuildVersionNumber));
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
        message.append("<p>Failed to start due to your data being too old to be able to be upgraded by this version of JIRA.</p>");
        message.append("<p>Database version is: ").append(getDbBuildNumber()).append("</p>");
        message.append("<p>Minimum version required is: ").append(getMinimumUpgradableVersionString()).append("</p>");
        message.append("<p>You are running: ").append(buildUtilsInfo.getBuildInformation()).append("</p>");
        message.append("<p>For information on how to upgrade your data, please see <a href=\"").append(INSTRUCTIONS_URL).append("\">our documentation</a>.</p>");
        return message.toString();
    }

    @Override
    public void stop()
    {
    }

    public String getFaultDescription()
    {
        StringBuilder message = new StringBuilder(512);
        message.append("Failed to start due to your data being too old to be able to be upgraded by this version of JIRA.\n");
        message.append("Database version is: ").append(getDbBuildNumber()).append("\n");
        message.append("Minimum version required is: ").append(getMinimumUpgradableVersionString()).append("\n");
        message.append("You are running: ").append(buildUtilsInfo.getBuildInformation()).append("\n");
        message.append("For information on how to upgrade your data, please see our documentation: ").append(INSTRUCTIONS_URL).append("\n");
        return message.toString();
    }

    private int getDbBuildNumber()
    {
        return UpgradeUtils.getJIRABuildVersionNumber();
    }

    private int getMinimumUpgradableBuildNumber()
    {
        return Integer.parseInt(buildUtilsInfo.getMinimumUpgradableBuildNumber());
    }

    private String getMinimumUpgradableVersionString()
    {
        return String.format("%s-#%d", buildUtilsInfo.getMinimumUpgradableVersion(), getMinimumUpgradableBuildNumber());
    }

    @Override
    public String toString()
    {
        return NAME;
    }
}
