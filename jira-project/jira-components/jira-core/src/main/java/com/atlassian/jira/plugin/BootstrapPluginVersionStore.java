package com.atlassian.jira.plugin;

import java.util.Collections;
import java.util.List;

/**
 * Bootstrap NOP implementation of the {@link com.atlassian.jira.plugin.PluginVersionStore}.
 *
 * @since v4.4
 */
public class BootstrapPluginVersionStore implements PluginVersionStore
{
    public BootstrapPluginVersionStore()
    {
    }

    public PluginVersion create(final PluginVersion pluginVersion)
    {
        return pluginVersion;
    }

    public PluginVersion update(final PluginVersion pluginVersion)
    {
        return pluginVersion;
    }

    public boolean delete(final Long pluginVersionId)
    {
        return true;
    }

    public PluginVersion getById(final Long pluginVersionId)
    {
        return null;
    }

    public List<PluginVersion> getAll()
    {
        return Collections.emptyList();
    }

    @Override
    public void deleteByKey(final String pluginKey)
    {
        // No-op
    }

    @Override
    public long save(final PluginVersion pluginVersion)
    {
        return 0L;
    }
}
