/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.project;

import com.atlassian.jira.entity.ProjectCategoryFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;

@WebSudoRequired
public class SelectProjectCategory extends JiraWebActionSupport
{
    private Long pid;
    private Long pcid;

    private final ProjectManager projectManager;

    public SelectProjectCategory(final ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    @Override
    public String doDefault() throws Exception
    {
        final ProjectCategory projectCategory = projectManager.getProjectCategoryForProject(getProject());

        if (null != projectCategory)
        {
            setPcid(projectCategory.getId());
        }
        else
        {
            setPcid(-1L);
        }

        return super.doDefault();
    }

    @Override
    protected void doValidation()
    {
        // Must have a valid project
        if (null == getProject())
        {
            addErrorMessage(getText("admin.errors.project.specify.project"));
        }

        // Either a valid project category, or null
        if (!new Long(-1).equals(getPcid()) && null == getProjectCategory())
        {
            addError("pcid", getText("admin.errors.project.specify.project.category"));
        }
    }

    /**
     * Given a project, remove all project category links, then create one if supplied a project category.
     */
    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        projectManager.setProjectCategory(getProject(), getProjectCategory());

        return returnCompleteWithInlineRedirect("/plugins/servlet/project-config/" + getProject().getKey() + "/summary");
    }

    public Collection<ProjectCategory> getProjectCategories() throws GenericEntityException
    {
        final Collection<ProjectCategory> projectCategoriesToDisplay = newArrayList(createEmptyProjectCategory());
        projectCategoriesToDisplay.addAll(projectManager.getAllProjectCategories());
        return projectCategoriesToDisplay;
    }

    private ProjectCategory createEmptyProjectCategory()
    {
        return new ProjectCategoryFactory.Builder().id(-1L).name("None").build();
    }

    public Project getProject()
    {
        return projectManager.getProjectObj(getPid());
    }

    private ProjectCategory getProjectCategory()
    {
        if (null == getPcid() || getPcid().equals(new Long(-1)))
        { return null; }

        return projectManager.getProjectCategoryObject(getPcid());
    }

    public Long getPid()
    {
        return pid;
    }

    public void setPid(Long pid)
    {
        this.pid = pid;
    }

    public Long getPcid()
    {
        return pcid;
    }

    public void setPcid(final Long pcid)
    {
        this.pcid = pcid;
    }
}
