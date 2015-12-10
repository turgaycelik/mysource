package com.atlassian.jira.my_home;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

import javax.annotation.Nonnull;

/**
 * The plugin module key is valid, if it is enabled.
 */
public class MyJiraHomeValidatorImpl implements MyJiraHomeValidator
{
    private final PluginAccessor pluginAccessor;

    public MyJiraHomeValidatorImpl(@Nonnull final PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public boolean isValid(@Nonnull String completePluginModuleKey)
    {
        try
        {
            return isPluginModuleEnabled(completePluginModuleKey) &&
                    isWebItemPluginModule(completePluginModuleKey);
        }
        catch (RuntimeException e)
        {
            return false;
        }
    }

    @Override
    public boolean isInvalid(@Nonnull final String completePluginModuleKey)
    {
        return !isValid(completePluginModuleKey);
    }

    private boolean isPluginModuleEnabled(@Nonnull final String completePluginModuleKey)
    {
        return pluginAccessor.isPluginModuleEnabled(completePluginModuleKey);
    }
    
    private boolean isWebItemPluginModule(@Nonnull final String completePluginModuleKey)
    {
        final ModuleDescriptor<?> pluginModule = pluginAccessor.getPluginModule(completePluginModuleKey);
        return pluginModule instanceof WebItemModuleDescriptor;
    }
}
