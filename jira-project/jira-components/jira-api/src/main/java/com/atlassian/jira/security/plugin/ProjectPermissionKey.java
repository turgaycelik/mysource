package com.atlassian.jira.security.plugin;

import javax.annotation.Nonnull;

import com.atlassian.jira.security.Permissions;

import static com.atlassian.jira.permission.ProjectPermissions.systemProjectPermissionKeyByShortName;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Key of the permission which applies to projects.
 */
public final class ProjectPermissionKey
{
    private final String permissionKey;

    /**
     * @deprecated Use {@link #ProjectPermissionKey(String)}.
     */
    @Deprecated
    public ProjectPermissionKey(final int permissionId)
    {
        this(resolve(permissionId));
    }

    public ProjectPermissionKey(@Nonnull final String permissionKey)
    {
        this.permissionKey = checkNotNull(permissionKey);
    }

    public String permissionKey()
    {
        return permissionKey;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final ProjectPermissionKey that = (ProjectPermissionKey) o;

        return permissionKey.equals(that.permissionKey);
    }

    @Override
    public int hashCode()
    {
        return permissionKey != null ? permissionKey.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return permissionKey;
    }

    private static String resolve(final Integer permissionId)
    {
        String name = Permissions.getShortName(permissionId);
        ProjectPermissionKey key = systemProjectPermissionKeyByShortName(name);
        if (key == null)
        {
            throw new IllegalArgumentException("No permission for id : " + permissionId + " in system");
        }
        return key.permissionKey();
    }
}
