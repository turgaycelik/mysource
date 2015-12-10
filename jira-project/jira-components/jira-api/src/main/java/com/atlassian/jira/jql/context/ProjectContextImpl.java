package com.atlassian.jira.jql.context;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.0
 */
public class ProjectContextImpl implements ProjectContext
{
    private final Long projectId;

    public ProjectContextImpl(final Long projectId)
    {
        this.projectId = notNull("projectId", projectId);
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public boolean isAll()
    {
        return false;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ProjectContextImpl that = (ProjectContextImpl) o;

        if (!projectId.equals(that.projectId))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return projectId.hashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append("projectId", projectId).
                toString();
    }
}
