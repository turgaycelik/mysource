package com.atlassian.jira.event.listeners.reindex;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.admin.plugins.PluginReindexHelper;
import com.atlassian.jira.web.action.admin.plugins.PluginReindexHelperImpl;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;

/**
 * Adds a re-index notification whenever a plugin module requiring reindex is enabled.
 *
 * @since v4.3
 */
@EventComponent
public class ReindexMessageListener
{
    private final PluginReindexHelper pluginReindexHelper;
    private final JiraAuthenticationContext authenticationContext;
    private final ReindexMessageManager reindexMessageManager;
    private final PluginAccessor pluginAccessor;

    public ReindexMessageListener(JiraAuthenticationContext authenticationContext, PluginAccessor pluginAccessor,
            ReindexMessageManager reindexMessageManager)
    {
        this.pluginAccessor = pluginAccessor;
        this.reindexMessageManager = reindexMessageManager;
        this.pluginReindexHelper = new PluginReindexHelperImpl(this.pluginAccessor);
        this.authenticationContext = authenticationContext;
    }

    // WARN: this relies on the fact that PICO starts after the plugin system. If we ever fix that, a 'simple'
    // plugin system start might cause this listener to add a message.
    // To prevent this, ReindexMessageManager.clear() should be called after the plugin system is started.
    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    @ClusterSafe("Events are only processed locally")
    public synchronized void pluginModuleEnabled(final PluginModuleEnabledEvent pmEnabledEvent)
    {
        if (pluginReindexHelper.doesEnablingPluginModuleRequireMessage(pmEnabledEvent.getModule().getCompleteKey()))
        {
            reindexMessageManager.pushMessage(authenticationContext.getLoggedInUser(),"admin.notifications.task.plugins");
        }
    }
}
