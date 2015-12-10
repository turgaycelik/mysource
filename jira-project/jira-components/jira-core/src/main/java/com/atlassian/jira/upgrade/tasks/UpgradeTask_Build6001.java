package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

/**
 * Upgrade Task to add property jira.projectkey.maxlength
 */
public class UpgradeTask_Build6001 extends AbstractUpgradeTask
{
    private static final String HOW_MANY = "10";

    private final ApplicationProperties applicationProperties;

    public UpgradeTask_Build6001(ApplicationProperties applicationProperties)
    {
        super(false);
        this.applicationProperties = applicationProperties;
    }

    public String getBuildNumber()
    {
        return "6001";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        applicationProperties.setString(APKeys.JIRA_PROJECTKEY_MAX_LENGTH, HOW_MANY);
    }

    public String getShortDescription()
    {
        return "Enabling configuration of the maximum project key length by configuration setting jira.projectkey.maxlength. Default value: " + HOW_MANY;
    }
}