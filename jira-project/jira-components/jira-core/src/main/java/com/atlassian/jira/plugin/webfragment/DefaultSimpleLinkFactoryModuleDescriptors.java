package com.atlassian.jira.plugin.webfragment;

import com.atlassian.jira.plugin.util.PluginModuleTrackerFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.plugin.tracker.PluginModuleTracker;

public class DefaultSimpleLinkFactoryModuleDescriptors implements SimpleLinkFactoryModuleDescriptors
{
    private final PluginModuleTracker<SimpleLinkFactory, SimpleLinkFactoryModuleDescriptor> simpleLinkFactoriesTracker;

    public DefaultSimpleLinkFactoryModuleDescriptors(final PluginModuleTrackerFactory pluginModuleTrackerFactory)
    {
        this.simpleLinkFactoriesTracker = pluginModuleTrackerFactory.create(SimpleLinkFactoryModuleDescriptor.class);
    }

    public Iterable<SimpleLinkFactoryModuleDescriptor> get()
    {
        return simpleLinkFactoriesTracker.getModuleDescriptors();
    }
}
