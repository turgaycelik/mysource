package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

/**
 * Only show the admin gadget "Getting Started" task list on new instances.
 */
public class UpgradeTask_Build6005 extends AbstractUpgradeTask
{
    private final ApplicationProperties applicationProperties;

    public UpgradeTask_Build6005(ApplicationProperties applicationProperties)
    {
        super(false);
        this.applicationProperties = applicationProperties;
    }

    public String getBuildNumber()
    {
        return "6005";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        applicationProperties.setOption(APKeys.JIRA_ADMIN_GADGET_TASK_LIST_ENABLED, setupMode);
    }

    public String getShortDescription()
    {
        return "Only show the admin gadget 'Getting Started' task list on new instances";
    }
}
