package com.atlassian.jira.plugin;

/**
 * ModuleDescriptors that can be ordered using {@see ModuleDescriptorComparator}, and use the 'order' element inside their
 * config file to indicate ordering.
 */
public interface OrderableModuleDescriptor
{
    public int getOrder();
}
