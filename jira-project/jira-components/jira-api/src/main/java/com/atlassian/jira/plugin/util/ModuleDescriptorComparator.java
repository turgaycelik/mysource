package com.atlassian.jira.plugin.util;

import com.atlassian.jira.plugin.OrderableModuleDescriptor;

import java.util.Comparator;

/**
 * Compares Module Descriptors that implement {@see OrderableModuleDescriptor}
 */
public class ModuleDescriptorComparator implements Comparator<OrderableModuleDescriptor>
{
    public static final ModuleDescriptorComparator COMPARATOR = new ModuleDescriptorComparator();

    public int compare(final OrderableModuleDescriptor descriptor1, final OrderableModuleDescriptor descriptor2)
    {
        final int order1 = descriptor1.getOrder();
        final int order2 = descriptor2.getOrder();

        if (order1 == order2)
        {
            return 0;
        }
        else if (order1 < order2)
        {
            return -1;
        }

        return 1;
    }
}
