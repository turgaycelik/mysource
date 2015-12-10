/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jelly.IssueAware;
import com.atlassian.jira.jelly.ProjectContextAccessor;
import com.atlassian.jira.jelly.ProjectContextAccessorImpl;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public abstract class IssueAwareActionTagSupport extends ProjectAwareActionTagSupport implements IssueAware, ProjectContextAccessor
{
    private static final Logger log = Logger.getLogger(IssueAwareActionTagSupport.class);
    private final ProjectContextAccessor projectContextAccessor;
    private final String[] requiredContextVariables;

    public IssueAwareActionTagSupport()
    {
        projectContextAccessor = new ProjectContextAccessorImpl(this);

        String[] temp = new String[super.getRequiredContextVariables().length + 1];
        System.arraycopy(super.getRequiredContextVariables(), 0, temp, 0, super.getRequiredContextVariables().length);
        temp[temp.length - 1] = JellyTagConstants.ISSUE_ID;
        requiredContextVariables = temp;
    }

    protected void preContextValidation()
    {
        // If there is no project then retrieve the issue if there is one and set the project from it
        if (!hasProject())
        {
            if (hasIssue())
            {
                setProject(getIssue().getLong("project"));
            }
        }
    }

    protected void endTagExecution(XMLOutput output)
    {
        loadPreviousProject();
    }

    public String[] getRequiredContextVariables()
    {
        return requiredContextVariables;
    }

    public boolean hasIssue()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.ISSUE_ID);
    }

    public Long getIssueId()
    {
        if (hasIssue())
            return (Long) getContext().getVariable(JellyTagConstants.ISSUE_ID);
        else
            return null;
    }

    public GenericValue getIssue()
    {
        return ComponentAccessor.getIssueManager().getIssue(getIssueId());
    }

    public void setProject(Long projectId)
    {
        projectContextAccessor.setProject(projectId);
    }

    public void setProject(String projectKey)
    {
        projectContextAccessor.setProject(projectKey);
    }

    public void setProject(GenericValue project)
    {
        projectContextAccessor.setProject(project);
    }

    public void loadPreviousProject()
    {
        projectContextAccessor.loadPreviousProject();
    }
}
