package com.atlassian.jira.plugin.util;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Set;

/**
 * This is a glorified list of plugin keys that code can use to track what plugins are involved in it's caches
 * <p/>
 * On plugin events it can then ask if the event related to one of the tracked plugins
 * <p/>
 * This uses a {@link java.util.concurrent.CopyOnWriteArraySet} under the covers to ensure that the list is as safe as
 * possible.  The assumption is that the reads and writes will be of low volume and the total number of plugins tracked
 * will be smallish.  In other words its anticipated that it will just work!
 *
 * @since v6.2.3
 */
public interface PluginsTracker
{
    /**
     * Tracks a plugin as being involved
     *
     * @param plugin the plugin in play
     */
    void trackInvolvedPlugin(Plugin plugin);

    /**
     * Tracks a plugin as being involved via it's {@link com.atlassian.plugin.ModuleDescriptor}
     *
     * @param moduleDescriptor the ModuleDescriptor of the plugin in play
     */
    void trackInvolvedPlugin(ModuleDescriptor moduleDescriptor);

    /**
     * Returns true if the plugin is being tracked
     *
     * @param plugin the plugin in play
     * @return true if the underlying plugin is being tracked
     */
    boolean isPluginInvolved(Plugin plugin);

    /**
     * Returns true if the plugin that this ModuleDescriptor belongs to is being tracked
     *
     * @param moduleDescriptor the ModuleDescriptor of the plugin in play
     * @return true if the underlying plugin is being tracked
     */
    boolean isPluginInvolved(ModuleDescriptor moduleDescriptor);

    /**
     * Returns true if the plugin pointed to by the moduleDescriptor contains in it a 1 or modules with the target
     * module descriptor class.
     *
     * @param moduleDescriptor the module descriptor in play (typically from a plugin event)
     * @param targetModuleClass the target capabilities you want to test
     * @return true if the underlying plugin has the designed module descriptor class
     */
    boolean isPluginWithModuleDescriptor(ModuleDescriptor moduleDescriptor, Class<? extends ModuleDescriptor> targetModuleClass);

    /**
     * Returns true if the plugin pointed to by the moduleDescriptor contains in it a 1 or module descriptors with the
     * target module descriptor class.
     *
     * @param plugin the plugin play (typically from a plugin event)
     * @param targetModuleClass the target capabilities you want to test
     * @return true if the underlying plugin has the designed module descriptor class
     */
    boolean isPluginWithModuleDescriptor(Plugin plugin, Class<? extends ModuleDescriptor> targetModuleClass);

    /**
     * Returns true if the plugin contains resources of the specified type, for example "i18n" resource types
     *
     * @param plugin the plugin play (typically from a plugin event)
     * @param pluginResourceType the descriptive name of the resource type (for example "i18n")
     * @return true if the plugin cvontains resources of the specified type
     */
    boolean isPluginWithResourceType(Plugin plugin, String pluginResourceType);

    /**
     * Returns true if the underlying plugin contains resources of the specified type, for example "i18n" resource
     * types
     *
     * @param moduleDescriptor the module descriptor of the plugin play (typically from a plugin event)
     * @param pluginResourceType the descriptive name of the resource type (for example "i18n")
     * @return true if the plugin cvontains resources of the specified type
     */
    boolean isPluginWithResourceType(ModuleDescriptor moduleDescriptor, String pluginResourceType);

    /**
     * @return a copy of the underlying tracked plugin keys
     */
    Set<PluginInfo> getInvolvedPluginKeys();

    /**
     * Clear the underlying set of tracked plugins
     */
    void clear();

    /**
     * Return a hash that represents all the plugins in this tracker. This hash should change if the list
     * of plugins being tracked changes.
     *
     * It is used to help generate a cache busting WebResource URL prefix. That is, if this hash changes then it is
     * likely that the URLs to all of JIRA's WebResources will change which will force all browsers to request all
     * resources again.
     *
     * @return Return a hash that represents all the plugins in this tracker.
     */
    String getStateHashCode();

    /**
     * A simple class that contains plugin key and pluginVersion.
     */
    public static class PluginInfo implements Comparable<PluginInfo>
    {
        private final String pluginKey;
        private final String pluginVersion;

        public PluginInfo(String pluginKey, String pluginVersion)
        {
            this.pluginKey = Assertions.notNull("pluginKey", pluginKey);
            this.pluginVersion = Assertions.notNull("pluginVersion", pluginVersion);
        }

        public String getPluginKey()
        {
            return pluginKey;
        }

        public String getPluginVersion()
        {
            return pluginVersion;
        }

        @Override
        public int compareTo(PluginInfo that)
        {
            int rc = pluginKey.compareTo(that.pluginKey);
            if (rc == 0)
            {
                rc = pluginVersion.compareTo(that.pluginVersion);
            }
            return rc;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            PluginInfo that = (PluginInfo) o;

            return pluginKey.equals(that.pluginKey) && pluginVersion.equals(that.pluginVersion);

        }

        @Override
        public int hashCode()
        {
            int result = pluginKey.hashCode();
            result = 31 * result + pluginVersion.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
