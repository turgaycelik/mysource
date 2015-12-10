/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.project;


import com.atlassian.jira.action.component.ComponentUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.Permissions;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class ViewProject extends AbstractProjectAction
{
    Long pid;
    GenericValue project;
    private final ProjectFactory projectFactory;


    public ViewProject(ProjectFactory projectFactory)
    {
        this.projectFactory = projectFactory;
    }

    //todo - get rid of this constructor (possibly by moving functionality used by sub-classes to AbstractProjectAction)
    public ViewProject()
    {
        this(ComponentAccessor.getProjectFactory());
    }

    public String doDefault() throws Exception
    {
        final Project projectObject = getProjectObject();
        if (projectObject != null)
        {
            return getRedirect("/plugins/servlet/project-config/" + projectObject.getKey() + "/summary");
        }

        return getRedirect("/plugins/servlet/project-config/" + "UNKNOWN" + "/summary");

    }

    protected void doValidation()
    {
        // no errors as we are just redirecting
    }

    protected String doExecute() throws Exception
    {
        final Project projectObject = getProjectObject();
        if (projectObject != null)
        {
            return getRedirect("/plugins/servlet/project-config/" + projectObject.getKey() + "/summary");
        }

        return getRedirect("/plugins/servlet/project-config/" + "UNKNOWN" + "/summary");
    }

    public boolean hasProjectAdminPermission()
    {
        return hasAdminPermission() ||
                ComponentAccessor.getPermissionManager().hasPermission(Permissions.PROJECT_ADMIN, getProjectObject(), getLoggedInApplicationUser());
    }

    public boolean hasAdminPermission()
    {
        return ComponentAccessor.getPermissionManager().hasPermission(Permissions.ADMINISTER, getLoggedInApplicationUser());
    }

    public boolean hasAssociateRolesPermission() throws Exception
    {
        return hasAdminPermission() || (hasProjectAdminPermission());
    }

    public GenericValue getProject()
    {
        if (project == null)
        {
            project = getProjectManager().getProject(getPid());
        }
        return project;
    }

    public Project getProjectObject()
    {
        return projectFactory.getProject(getProject());
    }

    public boolean isDefaultAssigneeAssignable() throws GenericEntityException
    {
        Long assigneeType = getProject().getLong("assigneetype");
        if (assigneeType != null && ProjectAssigneeTypes.PROJECT_LEAD == assigneeType)
        {
            return ComponentUtils.isProjectLeadAssignable(getProject());
        }
        else
        {
            return true;
        }
    }

    public void setPid(Long pid)
    {
        this.pid = pid;
    }

    public Long getPid()
    {
        return pid;
    }

}
