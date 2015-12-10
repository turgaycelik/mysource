package com.atlassian.jira.plugin.util.orderings;

import com.atlassian.jira.plugin.util.ModuleDescriptors;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.metadata.PluginMetadataManager;
import com.atlassian.plugin.web.descriptors.WeightedDescriptor;
import com.atlassian.plugin.web.descriptors.WeightedDescriptorComparator;

import com.google.common.collect.Ordering;

/**
*
* @since v4.4
*/
public class DefaultModuleDescriptorOrderingsFactory implements ModuleDescriptors.Orderings
{
    private final PluginMetadataManager pluginMetadataManager;

    PluginMetadataManager getPluginMetadataManager()
    {
        return pluginMetadataManager;
    }

    public DefaultModuleDescriptorOrderingsFactory(final PluginMetadataManager pluginMetadataManager)
    {
        this.pluginMetadataManager = pluginMetadataManager;
    }

    @Override
    public Ordering<ModuleDescriptor> byOrigin()
    {
        return new ByOriginModuleDescriptorOrdering(getPluginMetadataManager());
    }

    @Override
    public Ordering<ModuleDescriptor> natural()
    {
        return new NaturalModuleDescriptorOrdering();
    }

    @Override
    public Ordering<WeightedDescriptor> weightedDescriptorComparator()
    {
        return Ordering.from(new WeightedDescriptorComparator());
    }
}
