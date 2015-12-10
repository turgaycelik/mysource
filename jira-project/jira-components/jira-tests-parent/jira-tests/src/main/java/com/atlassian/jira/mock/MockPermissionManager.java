/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.DefaultPermissionManager;
import com.atlassian.jira.security.JiraPermission;
import com.atlassian.jira.user.ApplicationUser;

import org.ofbiz.core.entity.GenericValue;

public class MockPermissionManager extends DefaultPermissionManager
{
    protected Collection<JiraPermission> permissions;

    public boolean isDefaultPermission()
    {
        return defaultPermission;
    }

    public void setDefaultPermission(boolean defaultPermission)
    {
        this.defaultPermission = defaultPermission;
    }

    private boolean defaultPermission;

    public MockPermissionManager()
    {
        super(null);
        permissions = new HashSet<JiraPermission>(4);
    }

    /**
     * Creates a PermissionManager implementation where, by default, all permissions are given or denied based on the
     * given value.
     * @param defaultPermission if true, everything is permitted, if false, everything is denied.
     */
    public MockPermissionManager(final boolean defaultPermission)
    {
        super(null);
        this.defaultPermission = defaultPermission;
    }

    @Override
    public void removeGroupPermissions(String group) throws RemoveException
    {
        final Iterator<JiraPermission> iterator = permissions.iterator();
        while (iterator.hasNext())
        {
            if (iterator.next().getGroup().equals(group))
            {
                iterator.remove();
            }
        }
    }

    @Override
    public boolean hasPermission(int permissionsId, Project project, User u, boolean issueCreation)
    {
        return defaultPermission;
    }

    @Override
    public boolean hasPermission(int permissionsId, GenericValue project, User user, boolean issueCreation)
    {
        return defaultPermission;
    }

    @Override
    public boolean hasPermission(int permissionsId, User u)
    {
        return defaultPermission;
    }

    @Override
    public boolean hasPermission(final int permissionsId, final ApplicationUser user)
    {
        return defaultPermission;
    }

    @Override
    public boolean hasPermission(final int permissionId, final GenericValue projectOrIssue, final User user)
    {
        return defaultPermission;
    }

    @Override
    public boolean hasPermission(final int permissionsId, final Issue issue, final User user)
    {
        return defaultPermission;
    }

    @Override
    public boolean hasPermission(final int permissionsId, final Issue issue, final ApplicationUser user)
    {
        return defaultPermission;
    }

    @Override
    public boolean hasPermission(final int permissionsId, final Project project, final User user)
    {
        return defaultPermission;
    }

    @Override
    public boolean hasPermission(final int permissionsId, final Project project, final ApplicationUser user)
    {
        return defaultPermission;
    }

    @Override
    public boolean hasPermission(final int permissionsId, final Project project, final ApplicationUser user, final boolean issueCreation)
    {
        return defaultPermission;
    }

    @Override
    public boolean hasProjects(final int permissionId, final User user)
    {
        return defaultPermission;
    }

    @Override
    public boolean hasProjects(final int permissionId, final ApplicationUser user)
    {
        return defaultPermission;
    }
}
