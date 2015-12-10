package com.atlassian.jira.util.resourcebundle;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.metadata.PluginMetadataManager;
import com.google.common.base.Supplier;

/**
 * @since v6.2.3
 */
public class InitialResourceBundleLoader extends DefaultResourceBundleLoader
{
    public InitialResourceBundleLoader(final ComponentLocator locator, final ApplicationProperties properties)
    {
        super(properties.getDefaultLocale(), true, new Supplier<ResourceLoaderInvocation>()
        {
            @Override
            public ResourceLoaderInvocation get()
            {
                final PluginAccessor component = locator.getComponent(PluginAccessor.class);
                final PluginMetadataManager pluginMetadataManager = locator.getComponent(PluginMetadataManager.class);
                return new PluginResourceLoaderInvocation(component, pluginMetadataManager);
            }
        });
    }
}
