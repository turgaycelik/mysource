package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

/**
 * Upgrade Task to add property jira.projectkey.maxlength
 */
public class UpgradeTask_Build6006 extends AbstractUpgradeTask
{
    private static final String DEFAULT_NAME_LENGTH = "80";

    private final ApplicationProperties applicationProperties;

    public UpgradeTask_Build6006(ApplicationProperties applicationProperties)
    {
        super(false);
        this.applicationProperties = applicationProperties;
    }

    public String getBuildNumber()
    {
        return "6006";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        applicationProperties.setString(APKeys.JIRA_PROJECTNAME_MAX_LENGTH, DEFAULT_NAME_LENGTH);
    }

    public String getShortDescription()
    {
        return "Enabling configuration of the maximum project name length by configuration setting jira.projectname.maxlength. Default value: " + DEFAULT_NAME_LENGTH;
    }
}