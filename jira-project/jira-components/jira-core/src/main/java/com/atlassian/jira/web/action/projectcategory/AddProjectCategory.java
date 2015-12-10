/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.projectcategory;

import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

import java.util.Collection;

@WebSudoRequired
public class AddProjectCategory extends ProjectActionSupport
{
    private final ProjectManager projectManager;

    public AddProjectCategory(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    protected void doValidation()
    {
        if (!TextUtils.stringSet(name))
        {
            addError("name", getText("admin.errors.please.specify.a.name"));
            return;
        }

        //loop through all existing project categories and check that the name is unique
        Collection<ProjectCategory> projectCategories = projectManager.getAllProjectCategories();
        if (projectCategories != null)
        {
            for (ProjectCategory projectCategory : projectCategories)
            {
                if (name.equalsIgnoreCase(projectCategory.getName()))
                {
                    addError("name", getText("admin.errors.project.category.already.exists", "'" + name + "'"));
                    break;
                }
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            projectManager.createProjectCategory(getName(), getDescription());
        }
        catch (Exception e)
        {
            addErrorMessage(getText("admin.errors.projectcategory.could.not.create", e));
            return ERROR;
        }

        return getRedirect("ViewProjectCategories!default.jspa");
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

    public String name;
    public String description;
}
