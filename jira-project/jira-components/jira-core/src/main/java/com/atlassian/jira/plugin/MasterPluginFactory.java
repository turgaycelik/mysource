package com.atlassian.jira.plugin;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.factories.PluginFactory;
import com.atlassian.plugin.loaders.classloading.DeploymentUnit;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This plugin factory takes a list of plugin factories it will delegate to and a white list of allowable plugins.  It
 * then acts as a master factory over them all.  This allows us to have a bootstrap plugin factory and an everything
 * plugin factory.
 *
 * @since v4.4
 */
class MasterPluginFactory implements PluginFactory
{
    private static final Logger log = Logger.getLogger(MasterPluginFactory.class);
    private final List<PluginFactory> pluginFactories;
    private final List<Pattern> pluginWhitelist;
    private final JiraFailedPluginTracker jiraFailedPluginTracker;
    private final Map<String, PluginFactory> factoryDecisions;

    public MasterPluginFactory(final List<PluginFactory> pluginFactories, final List<Pattern> pluginWhitelist, final JiraFailedPluginTracker jiraFailedPluginTracker)
    {
        this.pluginFactories = pluginFactories;
        this.pluginWhitelist = pluginWhitelist;
        this.jiraFailedPluginTracker = jiraFailedPluginTracker;
        this.factoryDecisions = new HashMap<String, PluginFactory>();
    }

    @Override
    public String canCreate(PluginArtifact pluginArtifact) throws PluginParseException
    {
        log.debug("Seen plugin artifact '" + pluginArtifact.getName() + "'");
        if (!isInWhiteList(pluginArtifact))
        {
            return null;
        }
        String canCreate = null;
        for (PluginFactory pluginFactory : pluginFactories)
        {
            canCreate = pluginFactory.canCreate(pluginArtifact);
            if (canCreate != null)
            {
                factoryDecisions.put(pluginArtifact.getName(), pluginFactory);
                break;
            }
        }
        return canCreate;
    }

    private boolean isInWhiteList(PluginArtifact pluginArtifact)
    {
        for (Pattern allowedPattern : pluginWhitelist)
        {
            Matcher matcher = allowedPattern.matcher(pluginArtifact.getName());
            if (matcher.lookingAt())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Plugin create(PluginArtifact pluginArtifact, ModuleDescriptorFactory moduleDescriptorFactory)
            throws PluginParseException
    {
        final PluginFactory pluginFactory = Assertions.notNull("pluginFactory", factoryDecisions.get(pluginArtifact.getName()));
        Plugin plugin = pluginFactory.create(pluginArtifact, moduleDescriptorFactory);
        jiraFailedPluginTracker.trackLoadingPlugin(plugin, pluginArtifact);
        return plugin;
    }

    @Override
    public Plugin create(DeploymentUnit deploymentUnit, ModuleDescriptorFactory moduleDescriptorFactory)
            throws PluginParseException
    {
        throw new PluginParseException("Are we deprecated and hence not called or not??");
    }
}