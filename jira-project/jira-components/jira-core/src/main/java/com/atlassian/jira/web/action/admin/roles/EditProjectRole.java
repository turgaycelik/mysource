package com.atlassian.jira.web.action.admin.roles;

import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

@WebSudoRequired
public class EditProjectRole extends AbstractProjectRole
{
    private String name;
    private String description;

    public EditProjectRole(ProjectRoleService projectRoleService)
    {
        super(projectRoleService);
    }

    public String doDefault() throws Exception
    {
        ProjectRole roleType = getRole();
        name = roleType.getName();
        description = roleType.getDescription();

        return super.doDefault();
    }

    protected void doValidation()
    {
        if (getRole() == null)
        {
            addErrorMessage(getText("admin.errors.specified.role.does.not.exist"));
        }

        if (!TextUtils.stringSet(name))
        {
            addError("name", getText("admin.errors.must.specify.name"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        ProjectRole oldRole = getRole();
        ProjectRole role = new ProjectRoleImpl(oldRole.getId(), name, description);
        projectRoleService.updateProjectRole(role, this);

        if (getHasErrorMessages())
        {
            return ERROR;
        }
        else
        {
            return getRedirect("ViewProjectRoles.jspa");
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}