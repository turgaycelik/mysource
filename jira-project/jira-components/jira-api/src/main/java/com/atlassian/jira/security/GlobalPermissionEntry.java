package com.atlassian.jira.security;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;
import com.atlassian.jira.permission.GlobalPermissionKey;

import org.ofbiz.core.entity.GenericValue;

/**
 * This file represents a entry for a global permission.
 */
@ExperimentalApi
public final class GlobalPermissionEntry
{
    private final String permissionKey;

    private final String group;

    public GlobalPermissionEntry(GenericValue permission)
    {
        this.group = permission.getString("group_id");
        this.permissionKey = permission.getString("permission");
    }

    public GlobalPermissionEntry(final String permissionKey, final String group)
    {
        this.permissionKey = permissionKey;
        this.group = group;
    }

    public GlobalPermissionEntry(final GlobalPermissionKey permissionKey, final String group)
    {
        this.permissionKey = permissionKey.getKey();
        this.group = group;
    }

    // For anonymous permission checking
    public GlobalPermissionEntry(final String permissionKey)
    {
        this.permissionKey = permissionKey;
        this.group = null;
    }

    public String getPermissionKey()
    {
        return permissionKey;
    }

    /**
     * @deprecated Use {@link #getPermissionKey()} instead. Since v6.2.5.
     */
    @Internal
    public String getGlobalPermissionType()
    {
        return permissionKey;
    }

    public String getGroup()
    {
        return group;
    }

    @SuppressWarnings ("RedundantIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof GlobalPermissionEntry))
        {
            return false;
        }

        final GlobalPermissionEntry jiraPermission = (GlobalPermissionEntry) o;

        if (group != null ? !group.equals(jiraPermission.group) : jiraPermission.group != null)
        {
            return false;
        }
        if (permissionKey != null ? !permissionKey.equals(jiraPermission.permissionKey) : jiraPermission.permissionKey != null)
        {
            return false;
        }
        return true;
    }

    ///CLOVER:OFF
    @Override
    public int hashCode()
    {
        int result;
        result = group != null ? group.hashCode() : 0;
        result = 71 * result + (permissionKey != null ? permissionKey.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "Global Permission: [group=" + group + "][permissionKey=" + permissionKey + "]";
    }
}