package com.atlassian.jira.web.action.admin.roles;

import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

@WebSudoRequired
public class ViewProjectRoles extends AbstractRoleActors
{
    private String name;
    private String description;

    public ViewProjectRoles(ProjectRoleService projectRoleService, PluginAccessor pluginAccessor)
    {
        super(projectRoleService, pluginAccessor);
    }

    @RequiresXsrfCheck
    public String doAddRole() throws Exception
    {
        // validate
        validateName();

        if (invalidInput())
        {
            return ERROR;
        }

        projectRoleService.createProjectRole(new ProjectRoleImpl(name, description), this);
        if(hasAnyErrors())
        {
            return ERROR;
        }

        return getRedirect("ViewProjectRoles.jspa");
    }

    protected void validateName()
    {
        if (!TextUtils.stringSet(name))
        {
            addError("name", getText("admin.errors.must.specify.a.name.for.the.to.be.added", "role"));
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
