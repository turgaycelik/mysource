package com.atlassian.jira.plugin.util.orderings;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.metadata.PluginMetadataManager;
import com.google.common.collect.Ordering;

/**
 * <p>Orders module descriptors according to the &quot;origin&quot; of the plugin they come from. The origin of a
 * plugin can be one of &quot;user installed&quot; or &quot;system&quot; as determined by the
 * {@link PluginMetadataManager plugin metadata manager}.</p>
 *
 * <p>Module descriptors coming from user plugins are considered to be <em>&quot;greater than&quot;</em> the ones coming
 * from system plugins.</p>
 *
 * @since v4.4
 * @see PluginMetadataManager
 */
public class ByOriginModuleDescriptorOrdering extends Ordering<ModuleDescriptor>
{
    private final PluginMetadataManager pluginMetadataManager;

    ByOriginModuleDescriptorOrdering(PluginMetadataManager pluginMetadataManager)
    {
        this.pluginMetadataManager = pluginMetadataManager;
    }

    @Override
    public int compare(final ModuleDescriptor o1, final ModuleDescriptor o2)
    {
        if (isSystemProvided(o1.getPlugin()))
        {
            if (isSystemProvided(o2.getPlugin()))
            {
                return 0;
            }
            return -1;
        }
        if (isSystemProvided(o2.getPlugin()))
        {
            return 1;
        }
        return 0;
    }

    private boolean isSystemProvided(final Plugin plugin)
    {
        return !pluginMetadataManager.isUserInstalled(plugin);
    }
}
