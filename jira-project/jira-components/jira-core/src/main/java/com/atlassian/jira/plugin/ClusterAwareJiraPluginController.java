package com.atlassian.jira.plugin;

import java.util.Set;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.PluginParseException;

/**
 * Use this interface for managing plugins on ONLY the local node.
 * Oprerations performed through this interface will not be relayed across the cluster.
 *
 * @since v6.3
 */
public interface ClusterAwareJiraPluginController extends PluginController
{
    void enablePluginsLocalOnly(String... keys);

    String installPluginLocalOnly(PluginArtifact pluginArtifact) throws PluginParseException;

    Set<String> installPluginsLocalOnly(PluginArtifact... pluginArtifacts) throws PluginParseException;

    void uninstallLocalOnly(Plugin plugin) throws PluginException;

    void disablePluginLocalOnly(String key);

    void disablePluginModuleLocalOnly(String completeKey);

    void enablePluginModuleLocalOnly(String completeKey);
}
