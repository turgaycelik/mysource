package com.atlassian.jira.plugin.util;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;

/**
 * <p>Creates instances of a {@link PluginModuleTracker} using the {@link DefaultPluginModuleTracker} implementation.</p>
 *
 * <p>Instances of PluginModuleTracker keep a cached set of module descriptors that is updated when plugins and plugin
 * modules are enabled or disabled in the plugins system.</p>
 *
 * <p><em>IMPORTANT: </em> The set of descriptors returned by the plugin module trackers is a live list that is updated
 * as plugins and plugin modules are enabled or disabled, it is not necessary to further cache the results obtained from
 * a plugin module tracker.</p>
 *
 * @since v4.4
 * @see PluginModuleTracker
 */
public class PluginModuleTrackerFactory
{
    private PluginAccessor pluginAccessor;
    private PluginEventManager pluginEventManager;

    public PluginModuleTrackerFactory(final PluginAccessor pluginAccessor, final PluginEventManager pluginEventManager)
    {
        this.pluginAccessor = pluginAccessor;
        this.pluginEventManager = pluginEventManager;
    }

    /**
     * Creates an instance of a plugin module tracker for the specified module descriptor class.
     *
     * @param moduleDescriptorClass The module descriptor class to track.
     * @param <M> The module described by D.
     * @param <D> The class module descriptor to be tracked.
     * @return An instance of a plugin module tracker for the specified module descriptor class.
     */
    public <M, D extends ModuleDescriptor<M>> PluginModuleTracker<M, D> create(final Class<D> moduleDescriptorClass)
    {
        return DefaultPluginModuleTracker.create(pluginAccessor, pluginEventManager, moduleDescriptorClass);
    }

    /**
     * <p>Creates an instance of a plugin module tracker for the specified module descriptor class.</p>
     *
     * <p>It takes in a {@link com.atlassian.plugin.tracker.PluginModuleTracker.Customizer customizer} that enables the
     * calling code to perform additional operations when a plugin module is added or removed from the module tracker.</p>
     *
     * @param moduleDescriptorClass The module descriptor class to track.
     * @param pluginModuleTrackerCustomizer The customizer to use when module descriptors are added or removed from the
     * module tracker.
     * @param <M> The module described by D.
     * @param <D> The module descriptor to be tracked.
     * @return An instance of a plugin module tracker for the specified module descriptor class.
     * @see com.atlassian.plugin.tracker.PluginModuleTracker.Customizer
     */
    @SuppressWarnings ( { "unchecked" })
    public <M, D extends ModuleDescriptor<M>> PluginModuleTracker<M, D> create(final Class<D> moduleDescriptorClass,
            final PluginModuleTracker.Customizer<M, D> pluginModuleTrackerCustomizer)
    {
        return new DefaultPluginModuleTracker(pluginAccessor, pluginEventManager, moduleDescriptorClass,
                pluginModuleTrackerCustomizer);
    }
}
