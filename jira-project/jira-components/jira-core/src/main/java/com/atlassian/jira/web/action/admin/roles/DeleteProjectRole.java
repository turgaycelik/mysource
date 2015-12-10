package com.atlassian.jira.web.action.admin.roles;

import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class DeleteProjectRole extends ProjectRoleUsageAction
{
    public DeleteProjectRole(ProjectRoleService projectRoleService, NotificationSchemeManager notificationSchemeManager, PermissionSchemeManager permissionSchemeManager, ProjectFactory projectFactory, WorkflowManager workflowManager)
    {
        super(projectRoleService, notificationSchemeManager, permissionSchemeManager, projectFactory, workflowManager);
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        projectRoleService.deleteProjectRole(getRole(), this);

        if (hasAnyErrors())
            return ERROR;
        else
            return getRedirect("ViewProjectRoles.jspa");
    }
}
