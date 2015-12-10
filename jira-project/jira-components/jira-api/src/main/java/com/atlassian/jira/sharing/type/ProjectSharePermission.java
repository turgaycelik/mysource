/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.type;

import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.search.ProjectShareTypeSearchParameter;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * wrapper for ProjectShareType SharePermission with convenience accessors and assertions.
 * 
 * @since v3.13
 */
public class ProjectSharePermission
{
    private static final char ROLE_SEPARATOR = ':';

    private final Long projectId;
    private final Long roleId;

    public ProjectSharePermission(final SharePermission permission)
    {
        Assertions.notNull("permissions", permission);
        Assertions.equals("permission type", ShareType.Name.PROJECT, permission.getType());
        projectId = (permission.getParam1() == null) ? null : new Long(permission.getParam1());
        roleId = (permission.getParam2() == null) ? null : new Long(permission.getParam2());
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

    public ProjectShareTypeSearchParameter getSearchParameter()
    {
        return new ProjectShareTypeSearchParameter(getProjectId(), getRoleId());
    }

    String getIndexValue()
    {
        return indexValue(projectId, roleId);
    }

    /**
     * Used to create the value for indexing and querying for specific projects and roles.
     * 
     * @param projectId must not be null
     * @param roleId may be null
     * @return a String that represents
     */
    static String indexValue(final Long projectId, final Long roleId)
    {
        final StringBuilder buffer = new StringBuilder();
        if (projectId != null)
        {
            buffer.append(projectId);
        }
        if (roleId != null)
        {
            buffer.append(ROLE_SEPARATOR);
            buffer.append(roleId);
        }
        return buffer.toString();
    }

    /**
     * Used to create the value for matching a specific project's any role.
     * 
     * @param projectId must not be null
     * @return a String that represents
     */
    static String searchAllRolesValue(final Long projectId)
    {
        Assertions.notNull("projectId", projectId);
        return projectId.toString() + ROLE_SEPARATOR;
    }
}
