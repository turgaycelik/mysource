package com.atlassian.jira.plugin;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.plugin.component.ComponentModuleDescriptor;
import com.atlassian.jira.plugin.ha.PluginMessageSender;
import com.atlassian.jira.startup.JiraStartupLogger;
import com.atlassian.jira.tenancy.JiraTenantAccessor;
import com.atlassian.jira.tenancy.PluginKeyPredicateLoader;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.event.NotificationException;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginUninstalledEvent;
import com.atlassian.plugin.event.events.PluginUpgradedEvent;
import com.atlassian.plugin.manager.DefaultPluginManager;
import com.atlassian.plugin.manager.PluginPersistentStateStore;
import com.atlassian.plugin.repositories.FilePluginInstaller;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;
import org.picocontainer.MutablePicoContainer;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JiraPluginManager extends DefaultPluginManager implements Startable, ClusterAwareJiraPluginController
{
    public static final String PLUGIN_LICENSE_REGISTRY = "plugin-license-registry-location";
    public static final String PLUGIN_LICENSE_TYPE_STORE = "plugin-license-store-location";

    private static final Logger log = Logger.getLogger(JiraPluginManager.class);
    private final PluginVersionStore pluginVersionStore;

    @ClusterSafe("This reference has no node-specific state")
    private final LazyReference<PluginMessageSender> pluginMessageSenderRef = new LazyReference<PluginMessageSender>()
    {
        @Override
        protected PluginMessageSender create() throws Exception
        {
            return ComponentAccessor.getComponent(PluginMessageSender.class);
        }
    };

    /**
     * This field is used to ensure that the listener is created by PICO before this class. <b>Do not remove</b> unless
     * you ensure that PICO knows to instantiate the listeners first via some out-of-band mechanism.
     */
    @SuppressWarnings("unused")
    private final JiraPluginSystemListener jiraPluginSystemListener;

    public JiraPluginManager(final PluginPersistentStateStore store, final PluginLoaderFactory pluginLoaderFactory,
            final ModuleDescriptorFactory moduleDescriptorFactory, final PluginVersionStore pluginVersionStore,
            final PluginEventManager pluginEventManager, final PluginPath pluginPath,
            final PluginKeyPredicateLoader pluginKeyPredicateLoader, final JiraPluginSystemListener jiraPluginSystemListener,
            final JiraFailedPluginTracker failedPluginTracker)
    {
        super(store, pluginLoaderFactory.getPluginLoaders(), moduleDescriptorFactory, pluginEventManager,
                failedPluginTracker, pluginKeyPredicateLoader.getPluginKeyPatternsPredicate());
        this.pluginVersionStore = pluginVersionStore;
        this.jiraPluginSystemListener = jiraPluginSystemListener;

        /**
         * this enables dynamic plugin installation while JIRA is running via the {@link com.atlassian.plugin.manager.DefaultPluginManager#installPlugin(com.atlassian.plugin.PluginArtifact)} method.
         * currently the only way this will be used is via the PDK which wont be enabled in production.  The reason for this is that
         * we're not sure yet what plugin types actually fully support dynamic installation.
         */
        setPluginInstaller(new FilePluginInstaller(pluginPath.getInstalledPluginsDirectory()));
    }

    public void start() throws PluginException
    {
        earlyStartup();
        // JDEV-29003 - this is a race at the moment, as it relies on events.  We are not going
        // to implement instant on in 6.3 timeframe so simply remove the tenanted check for now
        lateStartup();
    }

    @Override
    public void earlyStartup() throws PluginParseException, NotificationException
    {
        JiraStartupLogger.log().info("\n\n___ Starting the JIRA Plugin System _________________\n");
        super.earlyStartup();
    }

    @Override
    public void lateStartup() throws PluginParseException, NotificationException
    {
        super.lateStartup();
        storePluginVersions();
        JiraStartupLogger.log().info("\n\n___ Plugin System Started _________________\n");
    }

    @Override
    public void enablePlugins(String... keys)
    {
        super.enablePlugins(keys);
        for (String key : keys)
        {
            processEnabledPlugin(key);
            pluginMessageSenderRef.get().sendPluginEnabledMessage(key);
        }
    }

    @Override
    public void enablePluginsLocalOnly(String... keys)
    {
        super.enablePlugins(keys);
        for (String key : keys)
        {
            processEnabledPlugin(key);
        }
    }

    @Override
    public String installPlugin(final PluginArtifact pluginArtifact) throws PluginParseException
    {
        final String key =  super.installPlugin(pluginArtifact);
        pluginMessageSenderRef.get().sendPluginInstalledMessage(key);
        return key;

    }

    @Override
    public String installPluginLocalOnly(final PluginArtifact pluginArtifact) throws PluginParseException
    {
        final String key =  super.installPlugin(pluginArtifact);
        return key;

    }

    @Override
    public Set<String> installPlugins(final PluginArtifact... pluginArtifacts) throws PluginParseException
    {
        final Set<String> keys = super.installPlugins(pluginArtifacts);
        for (String key : keys)
        {
            pluginMessageSenderRef.get().sendPluginInstalledMessage(key);
        }
        return keys;
    }

    @Override
    public Set<String> installPluginsLocalOnly(final PluginArtifact... pluginArtifacts) throws PluginParseException
    {
        return super.installPlugins(pluginArtifacts);
    }

    @PluginEventListener
    public void onPluginEnabledEvent(final PluginEnabledEvent enabledEvent)
    {
        final Plugin plugin = enabledEvent.getPlugin();
        log.debug(String.format("Plugin with key '%s' has been enabled.", plugin.getKey()));
        // Ensure this plugin has an up-to-date PluginVersion
        pluginVersionStore.save(new PluginVersionImpl(plugin.getKey(), plugin.getName(), plugin.getPluginInformation().getVersion(), new Date()));
    }

    @PluginEventListener
    public void onPluginUninstalledEvent(final PluginUninstalledEvent uninstalledEvent)
    {
        final Plugin plugin = uninstalledEvent.getPlugin();
        log.debug(String.format("Plugin with key '%s' has been uninstalled.", plugin.getKey()));
        pluginVersionStore.deleteByKey(plugin.getKey());
    }

    @PluginEventListener
    public void onPluginDisabledEvent(PluginDisabledEvent disabledEvent)
    {
        final Plugin plugin = disabledEvent.getPlugin();
        log.debug(String.format("Plugin with key '%s' has been disabled.", plugin.getKey()));
    }

    @PluginEventListener
    public void onPluginUpgradedEvent(PluginUpgradedEvent upgradedEvent)
    {
        pluginMessageSenderRef.get().sendPluginUpgradedMessage(upgradedEvent.getPlugin().getKey());
    }

    private void processEnabledPlugin(String key)
    {
        //register components
        final Plugin plugin = getPlugin(key);
        if (plugin != null) {
            final Collection<ModuleDescriptor<?>> funNewComponents = getPlugin(key).getModuleDescriptors();
            //getModuleDescriptorsByModuleClass(ComponentModuleDescriptor.class);

            if (!funNewComponents.isEmpty())
            {
                for (final ModuleDescriptor<?> moduleDescriptor : funNewComponents)
                {
                    if (ComponentModuleDescriptor.class.isInstance(moduleDescriptor) && isPluginModuleEnabled(moduleDescriptor.getCompleteKey()))
                    {
                        ComponentModuleDescriptor componentModuleDescriptor = (ComponentModuleDescriptor) moduleDescriptor;
                        componentModuleDescriptor.registerComponents(ComponentManager.getInstance().getMutablePicoContainer());
                    }
                }
            }
        } else {
            log.error("Could not enable plugin with key '" + key + "'");
        }
    }

    @Override
    public void uninstall(final Plugin plugin) throws PluginException
    {
        super.uninstall(plugin);
        pluginMessageSenderRef.get().sendPluginUninstalledMessage(plugin.getKey());
    }

    @Override
    public void uninstallLocalOnly(final Plugin plugin) throws PluginException
    {
        super.uninstall(plugin);
    }

    @Override
    public void disablePlugin(final String key)
    {
        disablePluginLocalOnly(key);
        pluginMessageSenderRef.get().sendPluginDisabledMessage(key);
    }

    @Override
    public void disablePluginLocalOnly(final String key)
    {
        unregisterComponents(key);
        super.disablePlugin(key);
        if (!ComponentManager.getInstance().getState().isPluginSystemStarted())
        {
            log.warn("Disable Plugin with key '" + key + "' called before Plugin System Started.", new RuntimeException());
        }
    }

    @Override
    public void disablePluginWithoutPersisting(final String key)
    {
        unregisterComponents(key);
        super.disablePluginWithoutPersisting(key);
        if (ComponentManager.getInstance().getState().isPluginSystemStarted())
        {
            if (log.isDebugEnabled())
            {
                log.debug("Disabling plugin with key  '" + key + "' without persisting. This should occur if this "
                        + "plugin depends on a plugin that is currently being upgraded");
            }
        }
    }

    private void unregisterComponents(final String key)
    {
        final Plugin plugin = getPlugin(key);
        if (plugin == null)
        {
            log.warn(String.format("Attempted to unregister components for non existing plugin '%s'", key));
            return;
        }
        final Collection<ModuleDescriptor<?>> funNewComponents = plugin.getModuleDescriptors();
        //getModuleDescriptorsByModuleClass(ComponentModuleDescriptor.class);

        if (!funNewComponents.isEmpty())
        {
            for (final ModuleDescriptor<?> moduleDescriptor : funNewComponents)
            {
                if (ComponentModuleDescriptor.class.isInstance(moduleDescriptor) && isPluginModuleEnabled(moduleDescriptor.getCompleteKey()))
                {
                    ComponentModuleDescriptor componentModuleDescriptor = (ComponentModuleDescriptor) moduleDescriptor;
                    componentModuleDescriptor.unregisterComponents(ComponentManager.getInstance().getMutablePicoContainer());
                }
            }
        }
    }

    @Override
    public void disablePluginModule(final String completeKey)
    {
        disablePluginModuleLocalOnly(completeKey);
        pluginMessageSenderRef.get().sendPluginModuleDisabledMessage(completeKey);
    }

    @Override
    public void disablePluginModuleLocalOnly(final String completeKey)
    {
        final ModuleDescriptor<?> moduleDescriptor = getEnabledPluginModule(completeKey);
        if (moduleDescriptor instanceof ComponentModuleDescriptor)
        {
            ((ComponentModuleDescriptor) moduleDescriptor).unregisterComponents((MutablePicoContainer) ComponentManager.getInstance().getContainer());
        }

        super.disablePluginModule(completeKey);
    }

    @Override
    public void enablePluginModule(final String completeKey)
    {
        enablePluginModuleLocalOnly(completeKey);
        pluginMessageSenderRef.get().sendPluginModuleEnabledMessage(completeKey);
    }

    @Override
    public void enablePluginModuleLocalOnly(final String completeKey)
    {
        super.enablePluginModule(completeKey);
        final ModuleDescriptor<?> moduleDescriptor = getEnabledPluginModule(completeKey);
        if (moduleDescriptor instanceof ComponentModuleDescriptor)
        {
            ((ComponentModuleDescriptor) moduleDescriptor).registerComponents((MutablePicoContainer) ComponentManager.getInstance().getContainer());
        }
    }

    void storePluginVersions()
    {
        // Get the currently stored plugin versions by their key
        final Map<String, PluginVersion> pluginVersionsByKey = getPluginVersionsByKey();
        for (final Plugin plugin : getPlugins())
        {
            storePluginVersion(plugin, pluginVersionsByKey.get(plugin.getKey()));
            // now that we handled the plugin version remove it from the known versions
            pluginVersionsByKey.remove(plugin.getKey());
        }

        // Now take all the remaining keys in the existing versions map and delete them
        deletePluginVersions(pluginVersionsByKey.values());
    }

    void storePluginVersion(final Plugin plugin, final PluginVersion pluginVersion)
    {
        final String pluginKey = plugin.getKey();
        final PluginInformation pluginInformation = plugin.getPluginInformation();
        String version = "unknown";
        if (pluginInformation != null)
        {
            version = pluginInformation.getVersion();
        }

        if (pluginVersion == null)
        {
            if (log.isInfoEnabled())
            {
                log.info("Plugin with key '" + pluginKey + "' and name '" + plugin.getName() + "' and version '" + version + "' is being added to the system.");
            }
            pluginVersionStore.create(new PluginVersionImpl(pluginKey, plugin.getName(), version, new Date()));
        }
        else
        {
            // If the version is at all different then update the record
            if (!pluginVersion.getVersion().equals(version))
            {
                if (log.isInfoEnabled())
                {
                    log.info("Plugin with key '" + pluginKey + "' and name '" + plugin.getName() + "' and version '" + pluginVersion.getVersion() + "' is being updated to version '" + version + "'.");
                }
                pluginVersionStore.update(new PluginVersionImpl(pluginVersion.getId(), pluginVersion.getKey(), plugin.getName(), version, new Date()));
            }
        }
    }

    void deletePluginVersions(final Collection<PluginVersion> pluginVersionsToDelete)
    {
        for (final PluginVersion pluginVersion : pluginVersionsToDelete)
        {
            if (log.isInfoEnabled())
            {
                log.info("Plugin with key '" + pluginVersion.getKey() + "' and name '" + pluginVersion.getName() + "' and version '" + pluginVersion.getVersion() + "' is being removed from the system.");
            }
            pluginVersionStore.delete(pluginVersion.getId());
        }
    }

    Map<String, PluginVersion> getPluginVersionsByKey()
    {
        final Map<String, PluginVersion> versionsByKey = new HashMap<String, PluginVersion>();
        final List<PluginVersion> pluginVersions = pluginVersionStore.getAll();
        for (final PluginVersion pluginVersion : pluginVersions)
        {
            versionsByKey.put(pluginVersion.getKey(), pluginVersion);
        }
        return versionsByKey;
    }

}
