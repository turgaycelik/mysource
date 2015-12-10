package com.atlassian.jira.event.permission;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;
import com.atlassian.jira.permission.GlobalPermissionType;

/**
 *
 * @since v6.2
 */
@ExperimentalApi
public class GlobalPermissionAddedEvent extends AbstractGlobalPermissionEvent
{
    @Internal
    public GlobalPermissionAddedEvent(final GlobalPermissionType globalPermissionType, final String group)
    {
        super(globalPermissionType, group);
    }
}
