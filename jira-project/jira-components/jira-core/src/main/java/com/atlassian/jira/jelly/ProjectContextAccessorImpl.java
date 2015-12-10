/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Tag;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class ProjectContextAccessorImpl implements ProjectContextAccessor, ProjectAware
{
    private static final Logger log = Logger.getLogger(ProjectContextAccessorImpl.class);
    private boolean hasProjectId = false;
    private Long projectId = null;
    private final Tag tag;

    public ProjectContextAccessorImpl(Tag tag)
    {
        this.tag = tag;
    }

    public void setProject(Long projectId)
    {
        setPreviousProject();
        resetProjectContext();
        setProjectContext(projectId);
    }

    public void setProject(String projectKey)
    {
        setPreviousProject();
        resetProjectContext();
        setProjectContext(projectKey);
    }

    public void setProject(GenericValue project)
    {
        setPreviousProject();
        resetProjectContext();
        setProjectContext(project);
    }

    public void loadPreviousProject()
    {
        if (hasProjectId)
        {
            resetProjectContext();
            setProjectContext(projectId);
            hasProjectId = false;
            projectId = null;
        }
    }

    private void setPreviousProject()
    {
        // Store the old project
        if (hasProject())
        {
            hasProjectId = true;
            this.projectId = getProjectId();
        }
    }

    private void resetProjectContext()
    {
        // Reset the current context
        getContext().removeVariable(JellyTagConstants.PROJECT_ID);
        getContext().removeVariable(JellyTagConstants.PROJECT_KEY);
    }

    private void setProjectContext(Long projectId)
    {
        final GenericValue project = ComponentAccessor.getProjectManager().getProject(projectId);
        setProjectContext(project);
    }

    private void setProjectContext(String projectKey)
    {
        final GenericValue project = ComponentAccessor.getProjectManager().getProjectByKey(projectKey);
        setProjectContext(project);
    }

    private void setProjectContext(GenericValue project)
    {
        // Retrieve the new project
        if (project != null)
        {
            getContext().setVariable(JellyTagConstants.PROJECT_ID, project.getLong("id"));
            getContext().setVariable(JellyTagConstants.PROJECT_KEY, project.getString("key"));
        }
    }

    public JellyContext getContext()
    {
        return tag.getContext();
    }

    public boolean hasProject()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.PROJECT_ID);
    }

    public Long getProjectId()
    {
        if (hasProject())
            return (Long) getContext().getVariable(JellyTagConstants.PROJECT_ID);
        else
            return null;
    }

    public GenericValue getProject()
    {
        return ManagerFactory.getProjectManager().getProject(getProjectId());
    }
}
