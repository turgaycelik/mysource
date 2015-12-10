package com.atlassian.jira.mock.plugin;

import com.atlassian.plugin.Plugin;

/**
 * @since v6.2.3
 */
public class NullModuleDescriptor extends MockModuleDescriptor<Void>
{
    public NullModuleDescriptor(final Plugin plugin, final String key)
    {
        super(Void.class, plugin, key);
    }

    @Override
    public Void getModule()
    {
        return null;
    }
}
