/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.projectcategory;

import com.atlassian.jira.entity.ProjectCategoryFactory;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

@WebSudoRequired
public class EditProjectCategory extends ProjectActionSupport
{
    private Long id = null;
    private String name = null;
    private String description = null;

    protected void doValidation()
    {
        // Valid name must have some content.
        if (!TextUtils.stringSet(getName()))
        {
            addError("name", getText("admin.errors.please.specify.a.name"));
        }

        // Confirm that the project category id actually maps to a project category.
        if (null == getId() || null == projectManager.getProjectCategoryObject(getId()))
        {
            addErrorMessage(getText("admin.errors.project.category.does.not.exist"));
        }
        else
        {
            // Validate that the name is not the name of another project category
            for (ProjectCategory projectCategory : projectManager.getAllProjectCategories())
            {
                //cannot have two categories with a different id and the same name
                if (!getId().equals(projectCategory.getId()) && TextUtils.noNull(getName()).equalsIgnoreCase(projectCategory.getName()))
                {
                    addError("name", getText("admin.errors.project.category.already.exists","'" + getName() + "'"));
                    break;
                }
            }
        }
    }

    /**
     * Populate name and description fields given a project category id.
     * @throws Exception
     */
    public String doDefault() throws Exception
    {
        if (null == getId() || null == projectManager.getProjectCategoryObject(getId()))
        {
            addErrorMessage(getText("admin.errors.project.category.does.not.exist"));
        }
        else
        {
            ProjectCategory projectCategory = projectManager.getProjectCategoryObject(getId());

            setName(projectCategory.getName());
            setDescription(projectCategory.getDescription());
        }

        return INPUT;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Build the new Project Category
        ProjectCategoryFactory.Builder builder = new ProjectCategoryFactory.Builder();
        builder.id(getId());
        builder.name(getName());
        builder.description(description);

        // save it
        projectManager.updateProjectCategory(builder.build());

        return getRedirect("ViewProjectCategories!default.jspa");
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
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
