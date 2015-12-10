/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.permission;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.permission.ProjectPermissionSchemeHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.permission.ProjectPermissionCategory.ATTACHMENTS;
import static com.atlassian.jira.permission.ProjectPermissionCategory.COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissionCategory.ISSUES;
import static com.atlassian.jira.permission.ProjectPermissionCategory.OTHER;
import static com.atlassian.jira.permission.ProjectPermissionCategory.PROJECTS;
import static com.atlassian.jira.permission.ProjectPermissionCategory.TIME_TRACKING;
import static com.atlassian.jira.permission.ProjectPermissionCategory.VOTERS_AND_WATCHERS;


/**
 * This class is used to display all permissions for a particular permission scheme.
 * It is used for the Edit Permissions page
 */
@WebSudoRequired
public class EditPermissions extends SchemeAwarePermissionAction
{
    private final PermissionManager permissionManager;
    private final ProjectPermissionSchemeHelper helper;
    private String usersGroupsRolesHeaderText;
    private List<Project> projects;

    public EditPermissions(final PermissionManager permissionManager, final ProjectPermissionSchemeHelper helper)
    {
        this.permissionManager = permissionManager;
        this.helper = helper;
    }

    public Collection<ProjectPermission> getProjectPermissions()
    {
        return permissionManager.getProjectPermissions(PROJECTS);
    }

    public Collection<ProjectPermission> getIssuePermissions()
    {
        return permissionManager.getProjectPermissions(ISSUES);
    }

    public Collection<ProjectPermission> getVotersAndWatchersPermissions()
    {
        return permissionManager.getProjectPermissions(VOTERS_AND_WATCHERS);
    }

    public Collection<ProjectPermission> getTimeTrackingPermissions()
    {
        return permissionManager.getProjectPermissions(TIME_TRACKING);
    }

    public Collection<ProjectPermission> getCommentsPermissions()
    {
        return permissionManager.getProjectPermissions(COMMENTS);
    }

    public Collection<ProjectPermission> getAttachmentsPermissions()
    {
        return permissionManager.getProjectPermissions(ATTACHMENTS);
    }

    public Collection<ProjectPermission> getOtherPermissions()
    {
        return permissionManager.getProjectPermissions(OTHER);
    }

    public String getI18nUsersGroupsRolesHeader()
    {
        if (usersGroupsRolesHeaderText == null)
        {
            usersGroupsRolesHeaderText = getText("admin.common.words.users.groups.roles");
        }
        return usersGroupsRolesHeaderText;
    }

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     * @param permissionKey The key of the permission
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     * @see PermissionSchemeManager
     */
    public List getPermissions(String permissionKey) throws GenericEntityException
    {
        return getSchemeManager().getEntities(getScheme(), permissionKey);
    }

    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getPermissionSchemeManager();
    }

    public List<Project> getUsedIn()
    {
        if (projects == null)
        {
            final Scheme permissionScheme = getSchemeObject();
            projects = helper.getSharedProjects(permissionScheme);
        }
        return projects;
    }

}
