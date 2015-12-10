package com.atlassian.jira.scheme.mapper;

import com.atlassian.jira.security.roles.ProjectRole;
import org.apache.commons.collections.Transformer;

/**
 * 
 */
public final class GroupToRoleMapping
{
    /**
     * Transforms from GroupToRoleMapping to it's group name.
     */
    public final static Transformer MAPPING_TO_GROUPNAME_TRANSFORMER = new Transformer() {
        public Object transform(Object object)
        {
            GroupToRoleMapping mapping = (GroupToRoleMapping) object;
            return mapping.getGroupName();
        }
    };

    private final ProjectRole projectRole;
    private final String groupName;

    public GroupToRoleMapping(ProjectRole projectRole, String groupName)
    {
        if (projectRole == null)
        {
            throw new NullPointerException("projectRole should not be null");
        }
        
        if (groupName == null)
        {
            throw new NullPointerException("groupName should not be null");
        }

        this.projectRole = projectRole;
        this.groupName = groupName;
    }

    public ProjectRole getProjectRole()
    {
        return projectRole;
    }

    public String getGroupName()
    {
        return groupName;
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

        final GroupToRoleMapping that = (GroupToRoleMapping) o;

        if (!groupName.equals(that.groupName))
        {
            return false;
        }

        if (!projectRole.equals(that.projectRole))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = projectRole.hashCode();
        result = 29 * result + groupName.hashCode();
        return result;
    }
}
