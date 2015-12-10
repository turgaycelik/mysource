package com.atlassian.jira.web.action.admin.roles;

import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Abstract class for the Delete and editing of Project Roles
 */
public abstract class AbstractProjectRole extends JiraWebActionSupport
{
    private String id;
    protected ProjectRoleService projectRoleService;

    protected AbstractProjectRole(ProjectRoleService projectRoleService)
    {
        this.projectRoleService = projectRoleService;
    }

    public ProjectRole getRole()
    {
        return projectRoleService.getProjectRole(new Long(id), this);
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
