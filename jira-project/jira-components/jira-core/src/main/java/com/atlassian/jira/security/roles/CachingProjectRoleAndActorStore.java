package com.atlassian.jira.security.roles;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.map.CacheObject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A caching implementation of the {@link ProjectRoleAndActorStore} that delegates to another {@link ProjectRoleAndActorStore}.
 * <p>
 * This class maintains two separate unrelated caches, one for ProjectRoles and another for the actors associated with a
 * Project/ProjectRole combination. These use separate approaches to maintain correctness under concurrent usage.
 * <p>
 * The caching of the ProjectRoleActors maintains its correctness under concurrent updates/miss population by using
 * {@link ConcurrentMap#putIfAbsent(Object,Object)} to store the result of a retrieval operation from the database
 * (non-mutative), but {@link ConcurrentMap#put(Object,Object)} to store the result of an update.
 */
@EventComponent
public class CachingProjectRoleAndActorStore implements ProjectRoleAndActorStore
{
    private final ProjectRoleAndActorStore delegate;
    private final RoleActorFactory roleActorFactory;

    /** Caches all project roles, including lookup maps for by-ID or by-name */
    final CachedReference<AllProjectRoles> projectRoles;

    /** Caches default role actors for a given project role */
    final Cache<Long,CacheObject<CachedRoleActors>> defaultRoleActors;

    /** Caches project-specific role actors for a given project role */
    final Cache<ProjectRoleActorsKey,CacheObject<CachedRoleActors>> projectRoleActors;



    public CachingProjectRoleAndActorStore(final ProjectRoleAndActorStore delegate,
            final RoleActorFactory roleActorFactory, final CacheManager cacheManager)
    {
        this.delegate = delegate;
        this.roleActorFactory = roleActorFactory;

        this.projectRoles = cacheManager.getCachedReference(getClass(), "projectRoles",
                new AllProjectRolesLoader());

        this.defaultRoleActors = cacheManager.getCache(getClass().getName() + ".defaultRoleActors",
                new DefaultRoleActorsLoader());

        this.projectRoleActors = cacheManager.getCache(getClass().getName() + ".projectRoleActors",
                new ProjectRoleActorsLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }



    public Collection<ProjectRole> getAllProjectRoles()
    {
        return projectRoles.get().getAll();
    }

    public ProjectRole getProjectRole(final Long id)
    {
        return projectRoles.get().get(id);
    }

    public ProjectRole getProjectRoleByName(final String name)
    {
        return projectRoles.get().getByName(name);
    }

    public ProjectRole addProjectRole(final ProjectRole projectRole)
    {
        try
        {
            return delegate.addProjectRole(projectRole);
        }
        finally
        {
            projectRoles.reset();
        }
    }

    public void updateProjectRole(final ProjectRole projectRole)
    {
        try
        {
            delegate.updateProjectRole(projectRole);
        }
        finally
        {
            projectRoles.reset();
        }
    }

    public void deleteProjectRole(final ProjectRole projectRole)
    {
        final long projectRoleId = notNull("projectRole.getId()", notNull("projectRole", projectRole).getId());
        try
        {
            delegate.deleteProjectRole(projectRole);
        }
        finally
        {
            projectRoles.reset();
            defaultRoleActors.remove(projectRoleId);
            projectRoleActors.removeAll();
        }
    }

    public DefaultRoleActors getDefaultRoleActors(final Long projectRoleId)
    {
        return defaultRoleActors.get(projectRoleId).getValue();
    }

    public ProjectRoleActors getProjectRoleActors(final Long projectRoleId, final Long projectId)
    {
        if (projectId == null)
        {
            // Really the default role actors...
            return defaultRoleActors.get(notNull("projectRoleId", projectRoleId)).getValue();
        }
        return projectRoleActors.get(toKey(projectRoleId, projectId)).getValue();
    }

    public void updateProjectRoleActors(final ProjectRoleActors projectRoleActors)
    {
        if (projectRoleActors.getProjectId() == null)
        {
            // Really the default role actors...
            updateDefaultRoleActors(projectRoleActors);
            return;
        }
        delegate.updateProjectRoleActors(projectRoleActors);
        this.projectRoleActors.remove(toKey(projectRoleActors));
    }

    public void updateDefaultRoleActors(final DefaultRoleActors defaultRoleActors)
    {
        delegate.updateDefaultRoleActors(defaultRoleActors);
        this.defaultRoleActors.remove(defaultRoleActors.getProjectRoleId());
    }

    public void applyDefaultsRolesToProject(final Project project)
    {
        delegate.applyDefaultsRolesToProject(project);
    }

    @Override
    public void removeAllRoleActorsByKeyAndType(final String key, final String type)
    {
        delegate.removeAllRoleActorsByKeyAndType(key, type);
        // Nuke the whole cache since we don't know which projects/roles this will effect
        defaultRoleActors.removeAll();
        projectRoleActors.removeAll();
    }

    public void removeAllRoleActorsByProject(final Project project)
    {
        delegate.removeAllRoleActorsByProject(project);
        projectRoleActors.removeAll();
    }



    @Override
    public Collection<Long> getProjectIdsContainingRoleActorByKeyAndType(final String key, final String type)
    {
        return delegate.getProjectIdsContainingRoleActorByKeyAndType(key, type);
    }

    public List<Long> roleActorOfTypeExistsForProjects(final List<Long> projectsToLimitBy, final ProjectRole projectRole, final String projectRoleType, final String projectRoleParameter)
    {
        return delegate.roleActorOfTypeExistsForProjects(projectsToLimitBy, projectRole, projectRoleType, projectRoleParameter);
    }

    @Override
    public Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(final List<Long> projectsToLimitBy, final ProjectRole projectRole, final String projectRoleType, final String userKey)
    {
        return delegate.getProjectIdsForUserInGroupsBecauseOfRole(projectsToLimitBy, projectRole, projectRoleType, userKey);
    }



    @EventListener
    public void onClearCache(@SuppressWarnings("unused") final ClearCacheEvent event)
    {
        clearCaches();
    }

    public void clearCaches()
    {
        projectRoles.reset();
        defaultRoleActors.removeAll();
        projectRoleActors.removeAll();
    }



    static ProjectRoleActorsKey toKey(Long projectRoleId, Long projectId)
    {
        return new ProjectRoleActorsKey(projectRoleId, projectId);
    }

    static ProjectRoleActorsKey toKey(ProjectRoleActors projectRoleActors)
    {
        return toKey(projectRoleActors.getProjectRoleId(), projectRoleActors.getProjectId());
    }

    CacheObject<CachedRoleActors> toCacheObject(DefaultRoleActors defaultRoleActors)
    {
        if (defaultRoleActors == null)
        {
            return CacheObject.NULL();
        }
        final Set<RoleActor> optimizedRoleActors = roleActorFactory.optimizeRoleActorSet(defaultRoleActors.getRoleActors());
        return CacheObject.wrap(new CachedRoleActors(defaultRoleActors, optimizedRoleActors));
    }



    class DefaultRoleActorsLoader implements CacheLoader<Long,CacheObject<CachedRoleActors>>
    {
        @Override
        public CacheObject<CachedRoleActors> load(@Nonnull final Long projectRoleId)
        {
            return toCacheObject(delegate.getDefaultRoleActors(projectRoleId));
        }
    }

    class ProjectRoleActorsLoader implements CacheLoader<ProjectRoleActorsKey,CacheObject<CachedRoleActors>>
    {
        @Override
        public CacheObject<CachedRoleActors> load(@Nonnull final ProjectRoleActorsKey key)
        {
            return toCacheObject(delegate.getProjectRoleActors(key.getProjectRoleId(), key.getProjectId()));
        }
    }

    class AllProjectRolesLoader implements Supplier<AllProjectRoles>
    {
        public AllProjectRoles get()
        {
            return new AllProjectRoles(delegate.getAllProjectRoles());
        }
    }



    @Immutable
    static final class ProjectRoleActorsKey implements Serializable
    {
        final long projectRoleId;
        final long projectId;

        ProjectRoleActorsKey(Long projectRoleId, Long projectId)
        {
            this.projectRoleId = notNull("projectRoleId", projectRoleId);
            this.projectId = notNull("projectId", projectId);
        }

        public long getProjectRoleId()
        {
            return projectRoleId;
        }

        public long getProjectId()
        {
            return projectId;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final ProjectRoleActorsKey other = (ProjectRoleActorsKey)o;
            return projectRoleId == other.projectRoleId && projectId == other.projectId;
        }

        @Override
        public int hashCode()
        {
            final long value = projectId * 31 + projectRoleId;
            return (int)(value ^ (value >>> 32));
        }

        @Override
        public String toString()
        {
            return "ProjectRoleActorsKey[projectRoleId=" + projectRoleId + ",projectId=" + projectId + ']';
        }
    }



    static class AllProjectRoles
    {
        private final List<ProjectRole> projectRoles;
        private final Map<Long,ProjectRole> projectRolesById;
        private final Map<String,ProjectRole> projectRolesByName;

        AllProjectRoles(Collection<ProjectRole> projectRoles)
        {
            final ImmutableMap.Builder<Long,ProjectRole> byId = ImmutableMap.builder();
            final ImmutableMap.Builder<String,ProjectRole> byName = ImmutableMap.builder();
            for (ProjectRole projectRole : projectRoles)
            {
                byId.put(projectRole.getId(), projectRole);
                byName.put(projectRole.getName(), projectRole);
            }

            this.projectRoles = ImmutableList.copyOf(projectRoles);
            this.projectRolesById = byId.build();
            this.projectRolesByName = byName.build();
        }

        Collection<ProjectRole> getAll()
        {
            return projectRoles;
        }

        ProjectRole get(Long id)
        {
            return projectRolesById.get(id);
        }

        ProjectRole getByName(String name)
        {
            return projectRolesByName.get(name);
        }
    }



    /**
     * CachedProjectRoleActors contains an optimized contains(user) method.
     */
    static class CachedRoleActors implements ProjectRoleActors
    {
        private final DefaultRoleActors delegate;
        private final Set<RoleActor> optimizedProjectRoleSet;

        CachedRoleActors(final DefaultRoleActors delegate, final Set<RoleActor> optimizedProjectRoleSet)
        {
            this.delegate = delegate;
            this.optimizedProjectRoleSet = ImmutableSet.copyOf(optimizedProjectRoleSet);
        }

        /*
         * The optimized set of RoleActor instances is used.
         */
        public boolean contains(final ApplicationUser user)
        {
            for (final RoleActor o : optimizedProjectRoleSet)
            {
                if (o.contains(user))
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean contains(User user)
        {
            return contains(ApplicationUsers.from(user));
        }

        public Long getProjectId()
        {
            return (delegate instanceof ProjectRoleActors) ? ((ProjectRoleActors) delegate).getProjectId() : null;
        }

        public Set<User> getUsers()
        {
            return delegate.getUsers();
        }

        @Override
        public Set<ApplicationUser> getApplicationUsers()
        {
            return delegate.getApplicationUsers();
        }

        public Set<RoleActor> getRoleActors()
        {
            return delegate.getRoleActors();
        }

        public Long getProjectRoleId()
        {
            return delegate.getProjectRoleId();
        }

        public Set<RoleActor> getRoleActorsByType(final String type)
        {
            return delegate.getRoleActorsByType(type);
        }

        public DefaultRoleActors addRoleActors(final Collection<? extends RoleActor> roleActors)
        {
            return delegate.addRoleActors(roleActors);
        }

        public DefaultRoleActors addRoleActor(final RoleActor roleActor)
        {
            return delegate.addRoleActor(roleActor);
        }

        public DefaultRoleActors removeRoleActor(final RoleActor roleActor)
        {
            return delegate.removeRoleActor(roleActor);
        }

        public DefaultRoleActors removeRoleActors(final Collection<? extends RoleActor> roleActors)
        {
            return delegate.removeRoleActors(roleActors);
        }

        @Override
        public String toString()
        {
            return "CachedRoleActors[delegate=" + delegate + ",optimizedProjectRoleSet=" + optimizedProjectRoleSet + ']';
        }
    }
}
