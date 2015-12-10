/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.projectcategory;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;

@WebSudoRequired
public class DeleteProjectCategory extends ProjectActionSupport
{
    private boolean confirm = false;
    private Long id;

    private final CustomFieldManager customFieldManager;

    public DeleteProjectCategory(ProjectManager projectManager, PermissionManager permissionManager, CustomFieldManager customFieldManager)
    {
        super(projectManager, permissionManager);
        this.customFieldManager = customFieldManager;
    }

    protected void doValidation()
    {
        // Deletion must be confirmed
        if (!isConfirm())
        {
            addErrorMessage(getText("admin.errors.projectcategory.must.confirm.delete"));
        }

        // Must specify which project category to delete
        if (null == getId() || null == projectManager.getProjectCategoryObject(getId()))
        {
            addErrorMessage(getText("admin.errors.projectcategory.must.specify.category"));
        }
        else
        {
            // Confirm that there are no linked projects to this project category.
            final Collection projectsFromProjectCategory = getProjects();
            if (null != projectsFromProjectCategory && !projectsFromProjectCategory.isEmpty())
            {
                addErrorMessage(getText("admin.errors.projectcategory.currently.projects.linked"));
            }
        }
    }

    private Collection<Project> getProjects()
    {
        return projectManager.getProjectsFromProjectCategory(getProjectCategory());
    }

    private ProjectCategory getProjectCategory()
    {
        return projectManager.getProjectCategoryObject(getId());
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        customFieldManager.removeProjectCategoryAssociations(getProjectCategory());

        projectManager.removeProjectCategory(getId());

        return getRedirect("ViewProjectCategories!default.jspa");
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }
}
