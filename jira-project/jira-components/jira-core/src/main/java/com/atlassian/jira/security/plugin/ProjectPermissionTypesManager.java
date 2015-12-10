package com.atlassian.jira.security.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.permission.ProjectPermissionCategory;

import java.util.Collection;

/**
 * Manages project permissions defined by plugins (including system project permissions).
 *
 * @since v6.3
 */
public interface ProjectPermissionTypesManager
{
    /**
     * @return all project permissions.
     */
    Collection<ProjectPermission> all();

    /**
     * @param category project permission category.
     * @return all project permissions of the specified category.
     */
    Collection<ProjectPermission> withCategory(ProjectPermissionCategory category);

    /**
     * Returns a project permission matching the specified key.
     *
     * @param permissionKey A project permission key.
     * @return a project permission for the given permission key.
     *      {@link Option#none} if there is no permission with this key.
     */
    Option<ProjectPermission> withKey(ProjectPermissionKey permissionKey);

    /**
     * Returns a boolean value indicating whether a project permission
     * with the given key exists.
     *
     * @param permissionKey A project permission key.
     * @return true if the permission with the given key exists, otherwise false.
     */
    boolean exists(ProjectPermissionKey permissionKey);
}
