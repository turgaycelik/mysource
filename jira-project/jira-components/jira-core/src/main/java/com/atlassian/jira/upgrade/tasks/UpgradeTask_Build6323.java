package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.google.common.collect.ImmutableMap;

/**
 * Remove any plugin state related to the {@code com.atlassian.jira.plugin.system.licenseroles plugin}.
 * Complexity O(2).
 */
public class UpgradeTask_Build6323 extends AbstractUpgradeTask
{
    public UpgradeTask_Build6323()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6323";
    }

    @Override
    public String getShortDescription()
    {
        return "Remove plugin state for the 'LicenseRoles' plugin.";
    }

    @Override
    public void doUpgrade(final boolean setupMode)
    {
        deletePluginState("com.atlassian.jira.plugin.system.licenseroles");
        deletePluginState("com.atlassian.jira.plugin.system.licenseroles:businessUser");
    }

    private void deletePluginState(String key)
    {
        getOfBizDelegator().removeByAnd("PluginState", ImmutableMap.of("key", key));
    }
}
