package com.atlassian.jira.security.roles.actor;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.security.roles.ProjectRoleActor;

/**
 * Base class for ProjectRoleActor implementations.
 */
@Internal
public abstract class AbstractRoleActor implements ProjectRoleActor
{
    private final Long id;
    private final Long projectRoleId;
    private final Long projectId;
    private final String parameter;

    public AbstractRoleActor(final Long id, final Long projectRoleId, final Long projectId, final String parameter)
    {
        this.id = id;
        this.projectRoleId = projectRoleId;
        this.projectId = projectId;
        this.parameter = parameter;
    }

    public Long getId()
    {
        return id;
    }

    public Long getProjectRoleId()
    {
        return projectRoleId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public String getParameter()
    {
        return parameter;
    }

    @Override
    public boolean isActive()
    {
        return true;
    }

    public String getDescriptor()
    {
        return getType() + ':' + getParameter();
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        AbstractRoleActor that = (AbstractRoleActor) o;

        if (parameter != null ? !parameter.equals(that.parameter) : that.parameter != null)
        {
            return false;
        }
        if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null)
        {
            return false;
        }
        if (projectRoleId != null ? !projectRoleId.equals(that.projectRoleId) : that.projectRoleId != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (projectRoleId != null ? projectRoleId.hashCode() : 0);
        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
        result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() +
                "[id=" + id +
                ",projectRoleId=" + projectRoleId +
                ",projectId=" + projectId +
                ",parameter=" + parameter +
                ']';
    }
}