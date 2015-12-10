package com.atlassian.jira.external.beans;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.security.roles.ProjectRoleActor;

/**
 * Represents a project role member from an external data.
 *
 * @since v3.13
 */
@PublicApi
public class ExternalProjectRoleActor
{
    private final String id;
    private final String projectId;
    private final String roleId;
    private final String roleType;
    private final String roleActor;

    public ExternalProjectRoleActor(final String id, final String projectId, final String roleId, final String roleType, final String roleActor)
    {
        this.id = id;
        this.projectId = projectId;
        this.roleId = roleId;
        this.roleType = roleType;
        this.roleActor = roleActor;
    }

    public String getId()
    {
        return id;
    }

    public String getProjectId()
    {
        return projectId;
    }

    public String getRoleId()
    {
        return roleId;
    }

    public String getRoleType()
    {
        return roleType;
    }

    public String getRoleActor()
    {
        return roleActor;
    }

    public boolean isUserActor()
    {
        return ProjectRoleActor.USER_ROLE_ACTOR_TYPE.equals(roleType);
    }

    public boolean isGroupActor()
    {
        return ProjectRoleActor.GROUP_ROLE_ACTOR_TYPE.equals(roleType);
    }

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

        final ExternalProjectRoleActor that = (ExternalProjectRoleActor) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null)
        {
            return false;
        }
        if (roleActor != null ? !roleActor.equals(that.roleActor) : that.roleActor != null)
        {
            return false;
        }
        if (roleId != null ? !roleId.equals(that.roleId) : that.roleId != null)
        {
            return false;
        }
        if (roleType != null ? !roleType.equals(that.roleType) : that.roleType != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
        result = 31 * result + (roleId != null ? roleId.hashCode() : 0);
        result = 31 * result + (roleType != null ? roleType.hashCode() : 0);
        result = 31 * result + (roleActor != null ? roleActor.hashCode() : 0);
        return result;
    }
}
