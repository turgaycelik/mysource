package com.atlassian.jira.jelly.tag;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.ProjectAware;
import org.ofbiz.core.entity.GenericValue;

public abstract class ProjectAwareActionTagSupport extends UserAwareActionTagSupport implements ProjectAware
{
    private final String[] requiredContextVariables;
    private Long projectId = null;

    public ProjectAwareActionTagSupport()
    {
        String[] temp = new String[super.getRequiredContextVariables().length + 1];
        System.arraycopy(super.getRequiredContextVariables(), 0, temp, 0, super.getRequiredContextVariables().length);
        temp[temp.length - 1] = JellyTagConstants.PROJECT_ID;
        requiredContextVariables = temp;
    }

    public String[] getRequiredContextVariables()
    {
        return requiredContextVariables;
    }

    public boolean hasProject()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.PROJECT_ID);
    }

    public Long getProjectId()
    {
        return hasProject() ? (Long) getContext().getVariable(JellyTagConstants.PROJECT_ID) : null;
    }

    public GenericValue getProject()
    {
        return ManagerFactory.getProjectManager().getProject(getProjectId());
    }
}
