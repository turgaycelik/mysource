package com.atlassian.jira.avatar.types.project;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.types.AvatarAccessPolicy;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;

public class ProjectAvatarAccessPolicy implements AvatarAccessPolicy
{
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final ProjectService projectService;

    public ProjectAvatarAccessPolicy(final ProjectManager projectManager, final PermissionManager permissionManager, final ProjectService projectService)
    {
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.projectService = projectService;
    }

    @Override
    public boolean userCanViewAvatar(final ApplicationUser user, final Avatar avatar)
    {
        if (!hasCorrectType(avatar))
        {
            return false;
        }
        if (avatar.isSystemAvatar())
        {
            return true;
        }

        final long owningProjectId = Long.parseLong(avatar.getOwner());
        final Project project = projectManager.getProjectObj(owningProjectId);

        boolean hasPermission = null != project;
        hasPermission = hasPermission && hasUserPermissionToProject(user, project);

        return hasPermission;
    }

    @Override
    public boolean userCanCreateAvatarFor(final ApplicationUser user, final String owningObjectId)
    {
        final long owningProjectId;
        try
        {
            owningProjectId = Long.parseLong(owningObjectId);
        }
        catch (Exception x)
        {
            throw new IllegalArgumentException("ownerId", x);
        }
        final ProjectService.GetProjectResult getProjectResult = projectService.getProjectByIdForAction(user, owningProjectId, ProjectAction.EDIT_PROJECT_CONFIG);

        return getProjectResult.isValid();
    }

    private boolean hasCorrectType(final Avatar avatar)
    {
        return Avatar.Type.PROJECT == avatar.getAvatarType();
    }

    private boolean hasUserPermissionToProject(ApplicationUser remoteUser, Project project)
    {
        final boolean isAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, remoteUser);
        final boolean isProjectAdmin = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, remoteUser);
        final boolean hasBrowseProject = permissionManager.hasPermission(Permissions.BROWSE, project, remoteUser);

        return hasBrowseProject || isProjectAdmin || isAdmin;
    }
}
