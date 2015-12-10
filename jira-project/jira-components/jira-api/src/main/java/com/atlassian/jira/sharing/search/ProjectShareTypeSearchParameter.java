package com.atlassian.jira.sharing.search;

import com.atlassian.jira.sharing.type.ShareType;

/**
 * Represents the search parameters when searching for Project ShareTypes. The object can be used to find all the shares in a particular project by
 * specifying a project and no role. The object can be used to find all shares for a particular project and particular role by passing both. It may
 * also used to find all shares involving a particular given role when the project is null.
 * 
 * @since v3.13
 */
public class ProjectShareTypeSearchParameter extends AbstractShareTypeSearchParameter
{
    private final Long projectId;
    private final Long roleId;

    public ProjectShareTypeSearchParameter()
    {
        this(null, null);
    }

    public ProjectShareTypeSearchParameter(final Long projectId)
    {
        this(projectId, null);
    }

    public ProjectShareTypeSearchParameter(final Long projectId, final Long roleId)
    {
        super(ShareType.Name.PROJECT);

        this.projectId = projectId;
        this.roleId = roleId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public boolean hasRole()
    {
        return roleId != null;
    }

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final ProjectShareTypeSearchParameter that = (ProjectShareTypeSearchParameter) o;

        if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null)
        {
            return false;
        }
        if (roleId != null ? !roleId.equals(that.roleId) : that.roleId != null)
        {
            return false;
        }

        return true;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        int result;
        result = (projectId != null ? projectId.hashCode() : 0);
        result = 31 * result + (roleId != null ? roleId.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON
}
