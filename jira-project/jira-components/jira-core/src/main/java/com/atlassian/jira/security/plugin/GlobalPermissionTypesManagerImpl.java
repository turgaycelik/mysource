package com.atlassian.jira.security.plugin;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.event.api.EventListener;
import com.atlassian.fugue.Option;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.plugin.permission.GlobalPermissionModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.util.concurrent.ResettableLazyReference;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;


@EventComponent
public class GlobalPermissionTypesManagerImpl implements Startable, GlobalPermissionTypesManager
{
    private final DefaultPluginModuleTracker<Void, GlobalPermissionModuleDescriptor> pluginModuleTracker;

    private final ResettableLazyReference<Map<String, GlobalPermissionType>> globalPermissions;

    public GlobalPermissionTypesManagerImpl(final PluginAccessor pluginAccessor, PluginEventManager pluginEventManager)
    {
        this.pluginModuleTracker = new DefaultPluginModuleTracker<Void, GlobalPermissionModuleDescriptor>(pluginAccessor, pluginEventManager, GlobalPermissionModuleDescriptor.class, new PluginModuleTracker.Customizer<Void, GlobalPermissionModuleDescriptor>()
        {
            @Override
            public void removed(final GlobalPermissionModuleDescriptor descriptor)
            {
                globalPermissions.reset();
            }

            @Override
            public GlobalPermissionModuleDescriptor adding(final GlobalPermissionModuleDescriptor descriptor)
            {
                globalPermissions.reset();
                return descriptor;
            }
        });

        this.globalPermissions = new ResettableLazyReference<Map<String, GlobalPermissionType>>()
        {
            @Override
            protected Map<String, GlobalPermissionType> create()
            {
                Map<String, GlobalPermissionType> permissions = Maps.newHashMap();
                for (final GlobalPermissionModuleDescriptor moduleDescriptor : pluginModuleTracker.getModuleDescriptors())
                {
                    GlobalPermissionType globalPermissionType = createGlobalPermission(moduleDescriptor);
                    permissions.put(moduleDescriptor.getKey(), globalPermissionType);
                }
                return permissions;
            }
        };
    }

    @EventListener
    @SuppressWarnings ("unused")
    public void onClearCache(final ClearCacheEvent event)
    {
        globalPermissions.reset();
    }

    @Override
    public void start() throws Exception
    {
        globalPermissions.reset();
    }

    @Override
    public Collection<GlobalPermissionType> getAll()
    {
        return globalPermissions.get().values();
    }

    @Override
    public Option<GlobalPermissionType> getGlobalPermission(@Nonnull String permissionKey)
    {
        return Option.option(globalPermissions.get().get(permissionKey));
    }

    @Override
    public Option<GlobalPermissionType> getGlobalPermission(@Nonnull final GlobalPermissionKey permissionKey)
    {
        return getGlobalPermission(permissionKey.getKey());
    }

    private GlobalPermissionType createGlobalPermission(GlobalPermissionModuleDescriptor descriptor)
    {
        return new GlobalPermissionType(descriptor.getKey(),
                descriptor.getI18nNameKey(),
                descriptor.getDescriptionI18nKey(),
                descriptor.isAnonymousAllowed()
        );
    }

    @VisibleForTesting
    DefaultPluginModuleTracker<Void, GlobalPermissionModuleDescriptor> getPluginModuleTracker()
    {
        return pluginModuleTracker;
    }
}
