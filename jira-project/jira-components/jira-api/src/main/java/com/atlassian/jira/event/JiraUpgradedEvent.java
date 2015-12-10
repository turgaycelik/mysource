package com.atlassian.jira.event;

import com.atlassian.annotations.PublicApi;

/**
 * <p/>
 * Raised when JIRA finishes the upgrade process. Just before JIRA started event is raised.
 *
 * <p/>
 * This is only raised if the upgrade was successful.
 *
 * @since v5.0
 */
@PublicApi
public class JiraUpgradedEvent
{
    private final boolean setupUpgrade;

    public JiraUpgradedEvent(boolean setupUpgrade)
    {
        this.setupUpgrade = setupUpgrade;
    }

    /**
     * Whether the upgrade is a setup upgrade.
     *
     * @return <code>true</code>, if this was a setup upgrade.
     */
    public boolean isSetupUpgrade()
    {
        return setupUpgrade;
    }
}
