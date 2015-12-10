package com.atlassian.jira.plugin;

import com.atlassian.plugin.ModuleDescriptor;

import com.google.common.base.Predicate;

/**
 * Predicate which determines if the module is a system module.
 */
public class SystemModuleDescriptorPredicate implements Predicate<ModuleDescriptor<?>>
{
    @Override
    public boolean apply(final ModuleDescriptor<?> moduleDescriptor)
    {
        return moduleDescriptor.isSystemModule();
    }
}
