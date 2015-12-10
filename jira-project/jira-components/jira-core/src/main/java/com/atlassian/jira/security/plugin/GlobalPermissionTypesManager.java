package com.atlassian.jira.security.plugin;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.atlassian.fugue.Option;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;

/**
 * Manages global permissions defined by plugins. System Global Permissions are also defined here
 */
public interface GlobalPermissionTypesManager
{
    /**
     * Returns all global permissions defined in this JIRA instance.
     * @return all global permissions defined by plugins including system permissions.
     */
    Collection<GlobalPermissionType> getAll();

    /**
     * Returns the details of the given Global Permission.
     *
     * @param permissionKey the String based permission key
     * @return the global permission object for the provided permissionKey.
     */
    Option<GlobalPermissionType> getGlobalPermission(@Nonnull String permissionKey);

    /**
     * Returns the details of the given Global Permission.
     *
     * @param permissionKey the GlobalPermissionKey representing this Global Permission.
     * @return the global permission object for the provided permissionKey.
     */
    Option<GlobalPermissionType> getGlobalPermission(@Nonnull GlobalPermissionKey permissionKey);
}
