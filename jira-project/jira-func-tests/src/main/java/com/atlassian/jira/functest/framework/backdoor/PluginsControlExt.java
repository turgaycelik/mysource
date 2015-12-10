package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.atlassian.jira.testkit.client.PluginsControl;

/**
 * Extended PluginsControl.
 *
 * @since v6.0
 */
public class PluginsControlExt extends PluginsControl
{
    private static final String ISSUE_NAV_PLUGIN_KEY = "com.atlassian.jira.jira-issue-nav-plugin";

    public PluginsControlExt(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * Refrain from using this. As of JIRA 6.0, jira-issue-nav-plugin is an integral part of JIRA.
     *
     * The behaviour of disabling the plugin is undefined and Atlassian does not support such configuration.
     *
     * @deprecated
     */
    public PluginsControlExt enableIssueNavPlugin()
    {
        enablePlugin(ISSUE_NAV_PLUGIN_KEY);
        return this;
    }

    /**
     * Refrain from using this. As of JIRA 6.0, jira-issue-nav-plugin is an integral part of JIRA.
     *
     * The behaviour of disabling the plugin is undefined and Atlassian does not support such configuration.
     *
     * @deprecated
     */
    public PluginsControlExt disableIssueNavPlugin()
    {
        disablePlugin(ISSUE_NAV_PLUGIN_KEY);
        return this;
    }
}
