package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Permission checker for the {@link com.atlassian.jira.sharing.type.ProjectShareType}
 *
 * @since v3.13
 */
public class ProjectShareTypePermissionChecker implements ShareTypePermissionChecker
{
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final ProjectRoleManager projectRoleManager;

    public ProjectShareTypePermissionChecker(final ProjectManager projectManager, final PermissionManager permissionManager, final ProjectRoleManager projectRoleManager)
    {
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.projectRoleManager = projectRoleManager;
    }

    /**
     * If a role has been passed in, checks to see if the user is in that role for the given project.  If no role
     * has been given, it checks to see if the user has browse permission for the project.
     *
     * @param user The user to check permissions for.
     * @param permission the permission to check against.  Must have a project, role is optional.
     * @return true if the above criteria is true, else false.
     */
    public boolean hasPermission(final User user, final SharePermission permission)
    {
        Assertions.notNull("permission", permission);
        Assertions.equals(ProjectShareType.TYPE.toString(), ProjectShareType.TYPE, permission.getType());
        Assertions.notNull("permission.param1", permission.getParam1());

        final Long projectId = new Long(permission.getParam1());

        final Project project = projectManager.getProjectObj(projectId);

        if (project == null)
        {
            return false;
        }

        if (permission.getParam2() == null)
        {
            return permissionManager.hasPermission(Permissions.BROWSE, project, user);
        }
        else if (user != null)
        {

            final Long roleId = new Long(permission.getParam2());

            final ProjectRole role = projectRoleManager.getProjectRole(roleId);
            return ((role != null) && projectRoleManager.isUserInProjectRole(user, role, project));
        }
        else
        {
            return false;
        }
    }
}
