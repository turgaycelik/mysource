/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Option;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.permission.ProjectPermissionCategory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.security.plugin.ProjectPermissionTypesManager;
import com.atlassian.jira.user.ApplicationUser;

import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.component.ComponentAccessor.getComponent;
import static com.atlassian.jira.component.ComponentAccessor.getProjectManager;

/**
 * The purpose of this class is to provide a temporary access-all-areas pass
 * and is a (partial) implementation of PermissionManager (subverting the
 * stored permissions). Operations that attempt to specify a change to stored
 * permissions like adding or removing permissions and the getAllGroups() method
 * throw an UnsupportedOperationException.
 */
public class SubvertedPermissionManager implements PermissionManager
{
    @Override
    public Collection<ProjectPermission> getAllProjectPermissions()
    {
        return getComponent(ProjectPermissionTypesManager.class).all();
    }

    @Override
    public Collection<ProjectPermission> getProjectPermissions(ProjectPermissionCategory category)
    {
        return getComponent(ProjectPermissionTypesManager.class).withCategory(category);
    }

    @Override
    public Option<ProjectPermission> getProjectPermission(@Nonnull ProjectPermissionKey permissionKey)
    {
        return getComponent(ProjectPermissionTypesManager.class).withKey(permissionKey);
    }

    /**
     * Not implemented.
     */
    public void addPermission(int permissionsId, GenericValue scheme, String group, String securityType)
    {
        throw new UnsupportedOperationException("addPermission() not implemented in " + this.getClass().getName());
    }

    public Collection<Project> getProjectObjects(final int permissionId, final User user)
    {
        return getAllProjects();
    }

    @Override
    public Collection<Project> getProjects(int permissionId, ApplicationUser user)
    {
        return getAllProjects();
    }

    @Override
    public Collection<Project> getProjects(ProjectPermissionKey permissionKey, ApplicationUser user)
    {
        return getAllProjects();
    }

    private Collection<Project> getAllProjects()
    {
        return getProjectManager().getProjectObjects();
    }

    /**
     * Returns all the projects in the given category, or if category is null,
     * all projects in no category.
     * @param permissionId ignored.
     * @param user ignored.
     * @param category the category for which to get projects.
     * @return the projects.
     */
    public Collection<GenericValue> getProjects(int permissionId, User user, GenericValue category)
    {
        if (category == null)
        {
            return getProjectManager().getProjectsWithNoCategory();
        }
        else
        {
            return getProjectManager().getProjectsFromProjectCategory(category);
        }
    }

    public Collection<Project> getProjects(int permissionId, User user, ProjectCategory category)
    {
        return getProjectsWithCategory(category);
    }

    @Override
    public Collection<Project> getProjects(int permissionId, ApplicationUser user, ProjectCategory projectCategory)
    {
        return getProjectsWithCategory(projectCategory);
    }

    @Override
    public Collection<Project> getProjects(ProjectPermissionKey permissionKey, ApplicationUser user, ProjectCategory projectCategory)
    {
        return getProjectsWithCategory(projectCategory);
    }

    private Collection<Project> getProjectsWithCategory(ProjectCategory category)
    {
        if (category == null)
        {
            return getProjectManager().getProjectObjectsWithNoCategory();
        }
        else
        {
            return getProjectManager().getProjectsFromProjectCategory(category);
        }
    }

    /**
     * Returns true if there are any projects at all.
     * @param permissionId ignored.
     * @param user ignored.
     * @return true if there are any projects.
     */
    public boolean hasProjects(int permissionId, User user)
    {
        return hasProjects();
    }

    @Override
    public boolean hasProjects(int permissionId, ApplicationUser user)
    {
        return hasProjects();
    }

    @Override
    public boolean hasProjects(ProjectPermissionKey permissionKey, ApplicationUser user)
    {
        return hasProjects();
    }

    private boolean hasProjects()
    {
        return !getAllProjects().isEmpty();
    }

    /**
     * Not implemented.
     */
    public void removeGroupPermissions(String group)
    {
        throw new UnsupportedOperationException("removeGroupPermissions() not implemented in " + this.getClass().getName());
    }

    /**
     * Not implemented.
     */
    public void removeUserPermissions(String username)
    {
        throw new UnsupportedOperationException("removeUserPermissions() not implemented in " + this.getClass().getName());
    }

    /**
     * Not implemented.
     */
    @Override
    public void removeUserPermissions(final ApplicationUser user) throws RemoveException
    {
        throw new UnsupportedOperationException("removeUserPermissions() not implemented in " + this.getClass().getName());
    }

    /**
     * Not implemented.
     */
    public Collection<Group> getAllGroups(int permType, Project project)
    {
        throw new UnsupportedOperationException("getAllGroups() not implemented in " + this.getClass().getName());
    }

    /**
     * Always returns true.
     *
     * @param permissionType ignored
     * @param u              ignored
     * @return               true
     */
    public boolean hasPermission(int permissionType, User u)
    {
        return true;
    }

    @Override
    public boolean hasPermission(int permissionsId, ApplicationUser user)
    {
        return true;
    }

    /**
     * Always returns true.
     *
     * @param permissionsId ignored
     * @param entity        ignored
     * @param u             ignored
     * @return              true
     */
    public boolean hasPermission(int permissionsId, GenericValue entity, User u)
    {
        return true;
    }

    /**
     * Always returns true.
     *
     * @param permissionsId ignored
     * @param issue         ignored
     * @param u             ignored
     * @return              true
     */
    public boolean hasPermission(int permissionsId, Issue issue, User u)
    {
        return true;
    }

    @Override
    public boolean hasPermission(int permissionsId, Issue issue, ApplicationUser user)
    {
        return true;
    }

    @Override
    public boolean hasPermission(ProjectPermissionKey permissionKey, Issue issue, ApplicationUser user)
    {
        return true;
    }

    /**
     * Always return true.
     *
     * @param permissionsId ignored
     * @param project       ignored
     * @param user          ignored
     * @return              true
     */
    public boolean hasPermission(int permissionsId, Project project, User user)
    {
        return true;
    }

    @Override
    public boolean hasPermission(int permissionsId, Project project, ApplicationUser user)
    {
        return true;
    }

    @Override
    public boolean hasPermission(ProjectPermissionKey permissionKey, Project project, ApplicationUser user)
    {
        return true;
    }

    /**
     * Always return true.
     *
     * @param permissionsId ignored
     * @param project       ignored
     * @param user          ignored
     * @param issueCreation ignored
     * @return              true
     */
    public boolean hasPermission(int permissionsId, Project project, User user, boolean issueCreation)
    {
        return true;
    }

    @Override
    public boolean hasPermission(int permissionsId, Project project, ApplicationUser user, boolean issueCreation)
    {
        return true;
    }

    @Override
    public boolean hasPermission(ProjectPermissionKey permissionKey, Project project, ApplicationUser user, boolean issueCreation)
    {
        return true;
    }

    /**
     * Always return true.
     *
     * @param permissionsId ignored
     * @param project       ignored
     * @param u             ignored
     * @param issueCreation ignored
     * @return              true
     */
    public boolean hasPermission(int permissionsId, GenericValue project, User u, boolean issueCreation)
    {
        return true;
    }

    public final Collection<GenericValue> getProjects(final int permissionId, final User user)
    {
        return getProjectManager().getProjects();
    }
}
