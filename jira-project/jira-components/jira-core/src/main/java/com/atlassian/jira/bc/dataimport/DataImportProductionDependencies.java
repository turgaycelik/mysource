package com.atlassian.jira.bc.dataimport;

import java.util.Collection;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.mail.settings.MailSettings;
import com.atlassian.jira.upgrade.ConsistencyCheckImpl;
import com.atlassian.jira.upgrade.ConsistencyChecker;
import com.atlassian.jira.upgrade.UpgradeManager;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.manager.PluginPersistentState;
import com.atlassian.plugin.manager.PluginPersistentStateStore;
import com.atlassian.sal.api.upgrade.PluginUpgradeManager;
import com.atlassian.scheduler.core.LifecycleAwareSchedulerService;

import org.apache.log4j.Logger;

/**
 * Provides a number of dependencies used during dataimport.  These *have* to come directly from the componentmanager
 * since they may get swapped out during import.  They should never be injected in this class.
 *
 * @since v4.4
 */
public class DataImportProductionDependencies
{
    private static final Logger log = Logger.getLogger(DataImportProductionDependencies.class);

    //this should not get anything injected.  The wholepoint of this class is to make sure it gets the
    //latest component from the ComponentManager since components may change during import.

    IndexLifecycleManager getIndexLifecycleManager()
    {
        return ComponentAccessor.getComponentOfType(IndexLifecycleManager.class);
    }

    ConsistencyChecker getConsistencyChecker()
    {
        return new ConsistencyCheckImpl();
    }

    UpgradeManager getUpgradeManager()
    {
        return ComponentAccessor.getComponentOfType(UpgradeManager.class);
    }

    LifecycleAwareSchedulerService getSchedulerService()
    {
        return ComponentAccessor.getComponent(LifecycleAwareSchedulerService.class);
    }

    PluginEventManager getPluginEventManager()
    {
        return ComponentAccessor.getPluginEventManager();
    }

    PluginUpgradeManager getPluginUpgradeManager()
    {
        return ComponentAccessor.getOSGiComponentInstanceOfType(PluginUpgradeManager.class);
    }

    MailSettings getMailSettings()
    {
        return ComponentAccessor.getComponent(MailSettings.class);
    }

    void globalRefresh(boolean quickImport)
    {
        if (quickImport)
        {
            log.warn("QuickImport is on, doing a fast refresh.");
            final PluginPersistentStateStore pluginPersistentStateStore = ComponentAccessor.getComponentOfType(PluginPersistentStateStore.class);
            // We need to keep track of which plugins were disabled in the old instance of JIRA, before we clear the cache
            final PluginPersistentState oldState = pluginPersistentStateStore.load();
            // Lets be tricky and clear the ApplicationProperties first since so much depends on those
            ComponentAccessor.getApplicationProperties().refresh();
            // Clear the state out of JIRA
            ComponentAccessor.getComponentOfType(EventPublisher.class).publish(ClearCacheEvent.INSTANCE);

            // We need to call enable/disable on the plugins which states have changed because of the newly imported data
            syncPluginStateWithNewData(pluginPersistentStateStore, oldState);
        }
        else
        {
            ManagerFactory.globalRefresh();
        }
    }

    private void syncPluginStateWithNewData(final PluginPersistentStateStore pluginPersistentStateStore, final PluginPersistentState oldState)
    {
        // Get the new state of the plugins
        final PluginPersistentState state = pluginPersistentStateStore.load();
        final PluginAccessor pluginAccessor = ComponentAccessor.getPluginAccessor();
        final PluginController pluginController = ComponentAccessor.getPluginController();

        // Run through all plugins
        final Collection<Plugin> plugins = pluginAccessor.getPlugins();
        for (Plugin plugin : plugins)
        {
            // If the plugin state has changed let the plugins system know
            final boolean enabledInOldSystem = oldState.isEnabled(plugin);
            final boolean enabledInNewSystem = state.isEnabled(plugin);
            if (!enabledInOldSystem && enabledInNewSystem)
            {
                pluginController.enablePlugins(plugin.getKey());
            }
            else if (enabledInOldSystem && !enabledInNewSystem)
            {
                pluginController.disablePlugin(plugin.getKey());
            }

            // Run through all the modules and let the plugins system know if the state has changed
            final Collection<ModuleDescriptor<?>> moduleDescriptors = plugin.getModuleDescriptors();
            for (ModuleDescriptor<?> moduleDescriptor : moduleDescriptors)
            {
                final boolean moduleEnabledInOldSystem = oldState.isEnabled(moduleDescriptor);
                final boolean moduleEnabledInNewSystem = state.isEnabled(moduleDescriptor);
                if (!moduleEnabledInOldSystem && moduleEnabledInNewSystem)
                {
                    pluginController.enablePluginModule(moduleDescriptor.getCompleteKey());
                }
                else if (moduleEnabledInOldSystem && !moduleEnabledInNewSystem)
                {
                    pluginController.disablePluginModule(moduleDescriptor.getCompleteKey());
                }
            }
        }
    }

    void refreshSequencer()
    {
        ComponentAccessor.getOfBizDelegator().refreshSequencer();
    }
}
