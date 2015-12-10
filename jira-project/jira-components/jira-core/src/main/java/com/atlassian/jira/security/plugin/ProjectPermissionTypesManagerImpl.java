package com.atlassian.jira.security.plugin;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.atlassian.event.api.EventListener;
import com.atlassian.fugue.Option;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.permission.ProjectPermissionCategory;
import com.atlassian.jira.plugin.permission.ProjectPermissionModuleDescriptor;
import com.atlassian.jira.plugin.util.PluginModuleTrackerFactory;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.util.concurrent.ResettableLazyReference;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;

import static com.atlassian.fugue.Option.option;
import static com.atlassian.plugin.tracker.PluginModuleTracker.Customizer;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.sort;

/**
 * @since v6.3
 */
@EventComponent
public class ProjectPermissionTypesManagerImpl implements ProjectPermissionTypesManager, Startable
{
    private final ResettableLazyReference<Map<ProjectPermissionKey, ProjectPermission>> permissions;

    public ProjectPermissionTypesManagerImpl(PluginModuleTrackerFactory trackerFactory)
    {
        final PluginModuleTracker<ProjectPermission, ProjectPermissionModuleDescriptor> pluginModuleTracker = trackerFactory.
                create(ProjectPermissionModuleDescriptor.class, new ProjectPermissionModuleTrackerCustomizer());

        permissions = new ResettableLazyReference<Map<ProjectPermissionKey, ProjectPermission>>()
        {
            @Override
            protected Map<ProjectPermissionKey, ProjectPermission> create()
            {
                List<ProjectPermission> permissions = newArrayList(pluginModuleTracker.getModules());
                sort(permissions, new ProjectPermissionComparator());

                Map<ProjectPermissionKey, ProjectPermission> keysToPermissions = newLinkedHashMap();
                for (ProjectPermission permission : permissions)
                {
                    keysToPermissions.put(new ProjectPermissionKey(permission.getKey()), permission);
                }
                return ImmutableMap.copyOf(keysToPermissions);
            }
        };
    }

    private class ProjectPermissionModuleTrackerCustomizer implements Customizer<ProjectPermission, ProjectPermissionModuleDescriptor>
    {
        @Override
        public ProjectPermissionModuleDescriptor adding(ProjectPermissionModuleDescriptor descriptor)
        {
            reset();
            return descriptor;
        }

        @Override
        public void removed(ProjectPermissionModuleDescriptor descriptor)
        {
            reset();
        }
    }

    private static class ProjectPermissionComparator implements Comparator<ProjectPermission>
    {
        @Override
        public int compare(ProjectPermission projectPermission1, ProjectPermission projectPermission2)
        {
            return projectPermission1.getKey().compareTo(projectPermission2.getKey());
        }
    }

    @EventListener
    @SuppressWarnings("unused")
    public void onClearCache(ClearCacheEvent event)
    {
        reset();
    }

    @Override
    public void start()
    {
        reset();
    }

    private void reset()
    {
        if (permissions != null)
        {
            permissions.reset();
        }
    }

    @Override
    public Collection<ProjectPermission> all()
    {
        return permissions.get().values();
    }

    @Override
    public Collection<ProjectPermission> withCategory(final ProjectPermissionCategory category)
    {
        return filter(all(), new Predicate<ProjectPermission>()
        {
            @Override
            public boolean apply(ProjectPermission permission)
            {
                return category.equals(permission.getCategory());
            }
        });
    }

    @Override
    public Option<ProjectPermission> withKey(ProjectPermissionKey permissionKey)
    {
        return option(permissions.get().get(permissionKey));
    }

    @Override
    public boolean exists(ProjectPermissionKey permissionKey)
    {
        return permissions.get().containsKey(permissionKey);
    }
}
