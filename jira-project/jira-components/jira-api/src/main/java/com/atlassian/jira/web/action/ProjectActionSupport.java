/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public class ProjectActionSupport extends JiraWebActionSupport
{
    private Collection<GenericValue> browseableProjects;
    private Collection<Project> browsableProjects;

    protected final ProjectManager projectManager;
    private final PermissionManager permissionManager;


    public ProjectActionSupport(ProjectManager projectManager, PermissionManager permissionManager)
    {
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
    }

    //todo - remove this constructor as subclasses become pico aware
    public ProjectActionSupport()
    {
        this(ComponentAccessor.getProjectManager(), ComponentAccessor.getPermissionManager());
    }

    /**
     * Returns the projects that the current user is allowed to Browse.
     * @return the projects that the current user is allowed to Browse.
     *
     * @deprecated Use {@link #getBrowsableProjects()} instead. Since v5.0.
     */
    public Collection<GenericValue> getBrowseableProjects()
    {
        if (browseableProjects == null)
        {
            browseableProjects = permissionManager.getProjects(Permissions.BROWSE, getLoggedInUser());
        }
        return browseableProjects;
    }

    /**
     * Returns the projects that the current user is allowed to Browse.
     * @return the projects that the current user is allowed to Browse.
     */
    public Collection<Project> getBrowsableProjects()
    {
        if (browsableProjects == null)
        {
            browsableProjects = permissionManager.getProjectObjects(Permissions.BROWSE, getLoggedInUser());
        }
        return browsableProjects;
    }

    public Long getSelectedProjectId()
    {
        final Project project = getSelectedProjectObject();
        return project == null ? null : project.getId();
    }

    public void setSelectedProject(GenericValue project)
    {
        if (project == null)
        {
            setSelectedProjectId(null);
        }
        else
        {
            setSelectedProjectId(project.getLong("id"));
        }
    }

    public void setSelectedProject(Project project)
    {
        if (project == null)
        {
            setSelectedProjectId(null);
        }
        else
        {
            setSelectedProjectId(project.getId());
        }
    }
}
