package com.atlassian.jira.plugin.userformat.descriptors;

import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;

/**
 *
 * @since v4.4
 */
public class DefaultUserFormatTypes implements UserFormatTypes
{
    private final UserFormatModuleDescriptors userFormatModuleDescriptors;

    public DefaultUserFormatTypes(final UserFormatModuleDescriptors userFormatModuleDescriptors)
    {
        this.userFormatModuleDescriptors = userFormatModuleDescriptors;
    }

    @Override
    public Iterable<String> get()
    {
        return ImmutableSet.copyOf
                (
                        Iterables.transform
                                (
                                        userFormatModuleDescriptors.get(),
                                        new Function<UserFormatModuleDescriptor, String>()
                                        {
                                            @Override
                                            public String apply(@Nullable UserFormatModuleDescriptor aUserFormatModuleDescriptor)
                                            {
                                                return aUserFormatModuleDescriptor.getType();
                                            }
                                        }
                                )
                );

    }
}
