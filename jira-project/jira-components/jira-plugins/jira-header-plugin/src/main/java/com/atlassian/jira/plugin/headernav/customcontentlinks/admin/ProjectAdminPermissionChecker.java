package com.atlassian.jira.plugin.headernav.customcontentlinks.admin;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;

public class ProjectAdminPermissionChecker
{
    private PermissionManager permissionManager;
    private ProjectManager projectManager;
    private UserManager userManager;

    public ProjectAdminPermissionChecker(PermissionManager permissionManager, ProjectManager projectManager, UserManager userManager) {
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.userManager = userManager;
    }

    public boolean canAdminister(String projectKey, String userName) {
        Project jiraProject = projectManager.getProjectObjByKey(projectKey);
        if (jiraProject != null) {
            User user = userManager.getUser(userName);
            if (user != null) {
                return permissionManager.hasPermission(Permissions.PROJECT_ADMIN, jiraProject, user);
            }
        }
        return false;
    }
}
