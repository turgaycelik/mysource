package com.atlassian.jira.startup;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;

import com.atlassian.jira.util.dbc.Assertions;

/**
 * Implementation for PluginInfos.
 *
 * @since v5.0
 */
public class PluginInfosImpl implements PluginInfos
{
    private final String name;
    private final ImmutableList<PluginInfo> pluginInfos;

    public PluginInfosImpl(String name, Iterable<PluginInfo> pluginInfos)
    {
        this.name = Assertions.notNull(name);
        this.pluginInfos = ImmutableList.copyOf(pluginInfos);
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public Iterator<PluginInfo> iterator()
    {
        return pluginInfos.iterator();
    }

    @Override
    public int size()
    {
        return pluginInfos.size();
    }

    @Override
    public String toString()
    {
        return PluginInfosImpl.class.getSimpleName() + "[\"" + name() + "\"]";
    }

    @Override
    public String prettyPrint()
    {
        FormattedLogMsg logMsg = new FormattedLogMsg();
        logMsg.outputHeader(name());
        logMsg.outputProperty("Number", String.valueOf(size()), 1);
        for (final PluginInfo pluginInfo : pluginInfos)
        {
            logMsg.add("");
            logMsg.outputProperty(pluginInfo.getName(), pluginInfo.getKey(), 1);
            logMsg.outputProperty("Version", pluginInfo.getPluginInformation().getVersion(), 2);
            logMsg.outputProperty("Status", pluginInfo.isEnabled() ? "enabled" : "disabled", 2);
            if (pluginInfo.isUnloadable())
            {
                logMsg.outputProperty("Unloadable Reason", pluginInfo.getUnloadableReason(), 2);
            }
            logMsg.outputProperty("Vendor", pluginInfo.getPluginInformation().getVendorName(), 2);
            logMsg.outputProperty("Description", pluginInfo.getPluginInformation().getDescription(), 2);
        }

        return logMsg.toString();
    }
}
