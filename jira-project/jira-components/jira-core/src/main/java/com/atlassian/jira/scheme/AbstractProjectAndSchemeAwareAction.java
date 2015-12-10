package com.atlassian.jira.scheme;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.action.admin.notification.ProjectAware;
import org.ofbiz.core.entity.GenericValue;

/**
 * @since v6.0
 */
public abstract class AbstractProjectAndSchemeAwareAction extends AbstractSchemeAwareAction implements ProjectAware
{
    private Long projectId;
    private Project project;

    @Override
    public Long getProjectId()
    {
        return projectId;
    }

    @Override
    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    @Override
    public GenericValue getProject()
    {
        if (getProjectObject() != null)
        {
            return getProjectObject().getGenericValue();
        }
        return null;
    }

    public Project getProjectObject()
    {
        if (project == null)
        {
            project = getProjectManager().getProjectObj(getProjectId());
        }
        return project;
    }
}
