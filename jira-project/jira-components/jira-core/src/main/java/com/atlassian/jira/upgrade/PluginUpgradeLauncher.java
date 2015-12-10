package com.atlassian.jira.upgrade;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.extension.JiraStartedEvent;
import com.atlassian.jira.startup.JiraLauncher;
import com.atlassian.jira.startup.JiraStartupChecklist;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.event.PluginEventManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Issues a JiraStartedEvent to launch the Jira Plugin upgrade task
 *
 * @since v4.4
 */
public class PluginUpgradeLauncher implements JiraLauncher
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginUpgradeLauncher.class);

    @Override
    public void start()
    {
        if (!JiraStartupChecklist.startupOK())
        {
            LOGGER.debug("Skipping, JIRA is locked.");
            return;
        }
        // Fire a JiraStarted Event to the plugin framework.
        // This allows SAL to give a better LifecycleAware onStart() time, and the JIRA SAL implementation can fire start() on Startable OSGi components.
        // See http://jira.atlassian.com/browse/JRA-23876
        if(JiraUtils.isSetup())
        {
            getPluginEventManager().broadcast(new JiraStartedEvent());
        }
    }

    @Override
    public void stop()
    {
        // do nothing
    }

    public PluginEventManager getPluginEventManager()
    {
        return ComponentAccessor.getComponentOfType(PluginEventManager.class);
    }


}
