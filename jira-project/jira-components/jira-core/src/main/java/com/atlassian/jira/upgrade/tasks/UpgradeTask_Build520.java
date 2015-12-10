package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

/**
 * Upgrade Task to change jira.maximum.authentication.attempts.allowed to be move from blank (unlimited) to a specified
 * value
 */
public class UpgradeTask_Build520 extends AbstractUpgradeTask
{
    private static final String HOW_MANY = "3";

    private final ApplicationProperties applicationProperties;

    public UpgradeTask_Build520(ApplicationProperties applicationProperties)
    {
        super(false);
        this.applicationProperties = applicationProperties;
    }

    public String getBuildNumber()
    {
        return "520";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        applicationProperties.setString(APKeys.JIRA_MAXIMUM_AUTHENTICATION_ATTEMPTS_ALLOWED, HOW_MANY);
    }

    public String getShortDescription()
    {
        return "Enabling brute force protection of login attempts by setting jira.maximum.authentication.attempts.allowed to " + HOW_MANY;
    }
}