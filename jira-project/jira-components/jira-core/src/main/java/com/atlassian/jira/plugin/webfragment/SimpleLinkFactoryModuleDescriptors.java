package com.atlassian.jira.plugin.webfragment;

import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;

/**
 * Gets the configured {@link SimpleLinkFactoryModuleDescriptor link factories}.
 * @deprecated since 6.3 - use {@link com.atlassian.plugin.web.api.DynamicWebInterfaceManager} directly.
 */
public interface SimpleLinkFactoryModuleDescriptors
{
    /**
     * Gets a snapshot of all the currently enabled SimpleLinkFactoryModuleDescriptors.
     *
     * @return An Iterable that contains a snapshot of all the currently enabled SimpleLinkFactoryModuleDescriptors.
     */
    Iterable<SimpleLinkFactoryModuleDescriptor> get();
}
