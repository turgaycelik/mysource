package com.atlassian.jira.security;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@ClusterSafe("This is a thread local cache and only exists for the life of the request.")
class PermissionsCache
{
    private final Map<CacheKey, Collection<Project>> projectObjectsWithBrowsePermissionForUser = new HashMap<CacheKey, Collection<Project>>();
    private final Map<CacheKey, Collection<GenericValue>> projectsWithBrowsePermissionForUser = new HashMap<CacheKey, Collection<GenericValue>>();
    private final ProjectFactory projectFactory;

    private final Function<GenericValue, Project> gvToProjectTransformer = new Function<GenericValue, Project>()
    {
        public Project get(final GenericValue projectGv)
        {
            return projectFactory.getProject(projectGv);
        }
    };

    private static final Function<Project, GenericValue> projectToGVTransformer = new Function<Project, GenericValue>()
    {
        public GenericValue get(final Project project)
        {
            return project.getGenericValue();
        }
    };

    PermissionsCache(ProjectFactory projectFactory)
    {
        this.projectFactory = projectFactory;
    }

    Collection<GenericValue> getProjectsWithBrowsePermission(final User user)
    {
        return projectsWithBrowsePermissionForUser.get(new CacheKey(user));
    }

    void setProjectsWithBrowsePermission(final User user, final Collection<GenericValue> projectsWithBrowsePermission)
    {
        //JRA-16757: Make an immutable copy of the returned list and return it. This will give a runtime exception anyone that is trying
        //to modify the list inline.
        final CacheKey cacheKey = new CacheKey(user);
        projectsWithBrowsePermissionForUser.put(cacheKey, CollectionUtil.copyAsImmutableList(projectsWithBrowsePermission));
        projectObjectsWithBrowsePermissionForUser.put(cacheKey, CollectionUtil.transform(projectsWithBrowsePermission, gvToProjectTransformer));
    }

    Collection<Project> getProjectObjectsWithBrowsePermission(final User user)
    {
        return projectObjectsWithBrowsePermissionForUser.get(new CacheKey(user));
    }

    void setProjectObjectsWithBrowsePermission(final User user, final Collection<Project> projectsWithBrowsePermission)
    {
        //JRA-16757: Make an immutable copy of the returned list and return it. This will give a runtime exception anyone that is trying
        //to modify the list inline.
        final CacheKey cacheKey = new CacheKey(user);
        projectObjectsWithBrowsePermissionForUser.put(cacheKey, CollectionUtil.copyAsImmutableList(projectsWithBrowsePermission));
        projectsWithBrowsePermissionForUser.put(cacheKey, CollectionUtil.transform(projectsWithBrowsePermission, projectToGVTransformer));
    }

    private static final class CacheKey
    {
        private final String username;

        CacheKey(User user)
        {
            this.username = user != null ? user.getName() : null;
        }

        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null)
            {
                return false;
            }
            if (!getClass().equals(o.getClass()))
            {
                return false;
            }
            final CacheKey cacheKey = (CacheKey) o;
            return (username == null) ? (cacheKey.username == null) : username.equals(cacheKey.username);
        }

        public int hashCode()
        {
            return (username != null ? username.hashCode() : 0);
        }
    }
}
