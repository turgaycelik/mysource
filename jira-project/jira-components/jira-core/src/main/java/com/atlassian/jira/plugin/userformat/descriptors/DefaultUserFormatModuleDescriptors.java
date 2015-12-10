package com.atlassian.jira.plugin.userformat.descriptors;

import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptors;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;

/**
 * @since v4.4
 */
public class DefaultUserFormatModuleDescriptors implements UserFormatModuleDescriptors
{
    private final PluginAccessor pluginAccessor;
    private final ModuleDescriptors.Orderings moduleDescriptorOrderings;

    public DefaultUserFormatModuleDescriptors(final PluginAccessor pluginAccessor, final ModuleDescriptors.Orderings moduleDescriptorOrderings)
    {
        this.pluginAccessor = pluginAccessor;
        this.moduleDescriptorOrderings = moduleDescriptorOrderings;
    }

    @Override
    public Iterable<UserFormatModuleDescriptor> forType(final String type)
    {
        return Iterables.filter(get(), new Predicate<UserFormatModuleDescriptor>()
        {
            @Override
            public boolean apply(@Nullable UserFormatModuleDescriptor aUserFormatModuleDescriptor)
            {
                return aUserFormatModuleDescriptor.getType().equals(type);
            }
        });
    }

    @Override
    public UserFormatModuleDescriptor withKey(final String completeKey)
    {
        final ModuleDescriptor<?> enabledPluginModule = pluginAccessor.getEnabledPluginModule(completeKey);

        if (enabledPluginModule instanceof UserFormatModuleDescriptor)
        {
            return (UserFormatModuleDescriptor) enabledPluginModule;
        }
        else
        {
            return null;
        }
    }

    @Override
    public Iterable<UserFormatModuleDescriptor> get()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(UserFormatModuleDescriptor.class);
    }

    @Override
    public UserFormatModuleDescriptor defaultFor(final String type)
    {
        Iterable<UserFormatModuleDescriptor> descriptorsForType = forType(type);

        if (!Iterables.isEmpty(descriptorsForType))
        {
            return moduleDescriptorOrderings.byOrigin().compound(moduleDescriptorOrderings.natural()).
                    min(descriptorsForType);
        }
        else
        {
            return null;
        }
    }
}
