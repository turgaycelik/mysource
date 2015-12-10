package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

/**
 * Allow Unassigned Issues by default for new instances.
 *
 * @since v6.1
 */
public class UpgradeTask_Build6124 extends AbstractUpgradeTask
{
    private final ApplicationProperties applicationProperties;

    public UpgradeTask_Build6124(final ApplicationProperties applicationProperties)
    {
        super(false);
        this.applicationProperties = applicationProperties;
    }


    @Override
    public String getBuildNumber()
    {
        return "6124";
    }

    @Override
    public String getShortDescription()
    {
        return "Allow unassigned issues by default but don't enforce this setting for existing instances";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        if (setupMode)
        {
            applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);
        }
    }
}
