package com.atlassian.jira.startup;

import com.atlassian.plugin.Plugin;

import java.util.Comparator;

public class PluginComparator implements Comparator<Plugin>
{
    private static final Comparator<Plugin> nameComparator = Plugin.NAME_COMPARATOR;

    public int compare(final Plugin plugin1, final Plugin plugin2)
    {
        int result = nameComparator.compare(plugin1, plugin2);
        if (result != 0)
        {
            return result;
        }
        result = plugin1.getKey().compareTo(plugin2.getKey());
        if (result != 0)
        {
            return result;
        }
        result = plugin1.getPluginsVersion() - plugin2.getPluginsVersion();
        if (result != 0)
        {
            return result;
        }
        return 0;
    }
}
