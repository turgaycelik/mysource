/**
 * Copyright 2008 Atlassian Pty Ltd 
 */
package com.atlassian.jira.sharing.type;

import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.search.GroupShareTypeSearchParameter;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * wrapper for GroupShareType SharePermission with convenience accessors and assertions.
 * 
 * @since v3.13
 */
public class GroupSharePermission
{
    private final SharePermission permission;

    public GroupSharePermission(final SharePermission permission)
    {
        Assertions.equals("permission type", ShareType.Name.GROUP, permission.getType());
        this.permission = permission;
    }

    public String getGroupName()
    {
        return permission.getParam1();
    }

    public GroupShareTypeSearchParameter getSearchParameter()
    {
        return new GroupShareTypeSearchParameter(getGroupName());
    }
}
