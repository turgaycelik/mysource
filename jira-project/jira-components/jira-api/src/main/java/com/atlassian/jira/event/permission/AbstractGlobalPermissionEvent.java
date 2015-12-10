package com.atlassian.jira.event.permission;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;
import com.atlassian.jira.permission.GlobalPermissionType;

/**
 * Base class for events published when Global Permissions are changed for a group
 *
 * @since v6.2
 */
@ExperimentalApi
public class AbstractGlobalPermissionEvent
{
    private final GlobalPermissionType globalPermissionType;
    private final String group;

    @Internal
    public AbstractGlobalPermissionEvent(final GlobalPermissionType globalPermissionType, final String group)
    {
        this.globalPermissionType = globalPermissionType;
        this.group = group;
    }

    public String getGroup()
    {
        return group;
    }

    public GlobalPermissionType getGlobalPermissionType()
    {
        return globalPermissionType;
    }
}
