package com.atlassian.jira.security;

import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.WorkflowPermissionFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.security.plugin.ProjectPermissionTypesManager;
import org.apache.log4j.Logger;

import java.util.Collection;

import static com.atlassian.jira.permission.ProjectPermissions.BROWSE_PROJECTS;

public class ThreadLocalCachingPermissionManager extends WorkflowBasedPermissionManager
{
    private static final Logger log = Logger.getLogger(ThreadLocalCachingPermissionManager.class);
    private final ProjectFactory projectFactory;

    public ThreadLocalCachingPermissionManager(final WorkflowPermissionFactory workflowPermissionFactory,
            final PermissionContextFactory permissionContextFactory, final ProjectFactory projectFactory,
            ProjectPermissionTypesManager projectPermissionTypesManager)
    {
        super(workflowPermissionFactory, permissionContextFactory, projectPermissionTypesManager);
        this.projectFactory = projectFactory;
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NM_WRONG_PACKAGE", justification="OSUser is deprecated and dying anyway. Plus the method in question is final so we can't override it.")
    public Collection<Project> getProjectObjects(ProjectPermissionKey permissionKey, final com.atlassian.crowd.embedded.api.User user)
    {
        if (BROWSE_PROJECTS.equals(permissionKey))
        {
            final PermissionsCache cache = getCache();
            final Collection<Project> cachedProjects = cache.getProjectObjectsWithBrowsePermission(user);
            if (cachedProjects != null)
            {
                return cachedProjects;
            }

            cache.setProjectObjectsWithBrowsePermission(user, super.getProjectObjects(permissionKey, user));
            return cache.getProjectObjectsWithBrowsePermission(user);
        }

        return super.getProjectObjects(permissionKey, user);
    }

    private PermissionsCache getCache()
    {
        PermissionsCache cache = (PermissionsCache) JiraAuthenticationContextImpl.getRequestCache().get(RequestCacheKeys.PERMISSIONS_CACHE);
        if (cache == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Creating new PermissionsCache");
            }
            cache = new PermissionsCache(projectFactory);
            JiraAuthenticationContextImpl.getRequestCache().put(RequestCacheKeys.PERMISSIONS_CACHE, cache);
        }

        return cache;
    }
}
