package com.atlassian.jira.plugin.ha;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Represents a plugin operation that has been performed
 *
 * @since v6.1
 */
public enum PluginEventType
{
    // Constructor takes a list of exclusions for the predicate, so if a passive node receives a plugin uninstalled ebvent
    // we suppress the inevitable plugin_module_disabled and plugin_disabled events
    PLUGIN_MODULE_ENABLED, PLUGIN_MODULE_DISABLED, PLUGIN_INSTALLED,
    PLUGIN_DISABLED(PluginEventType.PLUGIN_MODULE_DISABLED),
    PLUGIN_ENABLED(PluginEventType.PLUGIN_MODULE_ENABLED),
    PLUGIN_UNINSTALLED(PluginEventType.PLUGIN_MODULE_DISABLED, PluginEventType.PLUGIN_DISABLED),
    PLUGIN_UPGRADED(PluginEventType.PLUGIN_MODULE_DISABLED, PluginEventType.PLUGIN_DISABLED, PluginEventType.PLUGIN_MODULE_ENABLED, PluginEventType.PLUGIN_ENABLED);

    private final List<PluginEventType> excludedTypes = Lists.newArrayList();

    PluginEventType(final PluginEventType...excludedTypes)
    {
        for (PluginEventType excludedType : excludedTypes)
        {
            this.excludedTypes.add(excludedType);
        };
    }

    public boolean hasExclusions()
    {
        return excludedTypes.size() > 0;
    }

    public List<PluginEventType> getExclusions()
    {
        return excludedTypes;
    }
}
