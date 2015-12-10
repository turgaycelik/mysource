package com.atlassian.jira.dev.reference.plugin.rpc;

import com.atlassian.plugin.PluginAccessor;

/**
 * Implementation of the reference XML-RPC service.
 *
 * @since v4.4
 */
public class ReferenceXmlRpcServiceImpl implements ReferenceXmlRpcService
{
    private final PluginAccessor pluginAccessor;

    public ReferenceXmlRpcServiceImpl(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    public String getVersion()
    {
        return pluginAccessor.getPlugin("com.atlassian.jira.dev.reference-plugin").getPluginInformation().getVersion();
    }
}
