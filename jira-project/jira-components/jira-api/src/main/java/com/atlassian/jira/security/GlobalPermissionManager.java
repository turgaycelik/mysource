package com.atlassian.jira.security;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Option;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Use this manager to add/remove or check global permissions.
 * <p>
 * The system global permissions are:
 * <ul>
 * <li>{@link com.atlassian.jira.permission.GlobalPermissionKey#SYSTEM_ADMIN}</li>
 * <li>{@link com.atlassian.jira.permission.GlobalPermissionKey#ADMINISTER}</li>
 * <li>{@link com.atlassian.jira.permission.GlobalPermissionKey#USE}</li>
 * <li>{@link com.atlassian.jira.permission.GlobalPermissionKey#USER_PICKER}</li>
 * <li>{@link com.atlassian.jira.permission.GlobalPermissionKey#CREATE_SHARED_OBJECTS}</li>
 * <li>{@link com.atlassian.jira.permission.GlobalPermissionKey#MANAGE_GROUP_FILTER_SUBSCRIPTIONS}</li>
 * <li>{@link com.atlassian.jira.permission.GlobalPermissionKey#BULK_CHANGE}</li>
 * </ul>
 * Use {@link #getAllGlobalPermissions()} in order to get the collection of all global permissions, which includes
 * plugin global permissions.
 *
 * For project specific permissions use {@link PermissionManager}.
 */
public interface GlobalPermissionManager
{
    /**
     * @return the collection with all global permissions. This includes all the systems global permissions and all
     * the plugin global permissions.
     */
    @ExperimentalApi
    Collection<GlobalPermissionType> getAllGlobalPermissions();

    /**
     * @param permissionId id of the permission.
     * @return a global permission (system and pluggable) for the given permission id.
     * @deprecated use {@link com.atlassian.jira.security.GlobalPermissionManager#getGlobalPermission(GlobalPermissionKey)}
     * to get global permission by key. Eventually, we want to stop referring to Global Permission's by ID.
     */
    @Internal
    Option<GlobalPermissionType> getGlobalPermission(int permissionId);

    /**
     * Returns the global permission details for the given permission key.
     *
     * @param permissionKey the global permission key
     * @return the global permission details for the given permission key.
     *
     * @since 6.2.5
     */
    @ExperimentalApi
    Option<GlobalPermissionType> getGlobalPermission(@Nonnull GlobalPermissionKey permissionKey);

    /**
     * Returns a global permission matching the specified key.
     *
     * @param permissionKey the key of the permission declared by global permission module.
     * @return a global permission for the given permission key.
     */
    @ExperimentalApi
    Option<GlobalPermissionType> getGlobalPermission(@Nonnull String permissionKey);

    /**
     * Grants a user group a global permission.
     *
     * @param permissionType the global permission id.
     * @param group          the name of the group. Null means "anyone" group.
     *                       The JIRA use, admin and sysadmin permission cannot be granted to anyone.
     *
     * @return true if the permission was added.
     * @deprecated Use {@link #addPermission(com.atlassian.jira.permission.GlobalPermissionType, String)} instead.
     */
    boolean addPermission(int permissionType, String group);

    /**
     * Grants a user group a global permission.
     *
     * @param globalPermissionType global permission, must not be null.
     * @param group the name of the group. Null means "anyone" group.
     * The JIRA use, admin and sysadmin permission cannot be granted to anyone.
     *
     * @return true if permission was added.
     */
    @ExperimentalApi
    boolean addPermission(GlobalPermissionType globalPermissionType, String group);

    /**
     * Retrieve a list of user groups which have been granted a specified permission.
     * The returned {@link JiraPermission} contains a reference to the user group.
     *
     * {@link JiraPermission#getScheme()} is always NULL, because Global permission are not configured using schemes.
     * {@link JiraPermission#getType()} will always return "group", because global permissions can only be granted to groups.
     *
     * @param permissionType The key of pluggable global permission. Must be a global permission.
     * @return Collection of {@link JiraPermission#getPermType}, must never return null.
     * @deprecated Use {@link #getPermissions(com.atlassian.jira.permission.GlobalPermissionKey)} instead.
     */
    Collection<JiraPermission> getPermissions(int permissionType);

    /**
     * Retrieve a list of user groups which have been granted a specified permission.
     * The returned {@link JiraPermission} contains a reference to the user group.
     *
     * {@link JiraPermission#getScheme()} is always NULL, because Global permission are not configured using schemes.
     * {@link JiraPermission#getType()} will always return "group", because global permissions can only be granted to groups.
     *
     * @param globalPermissionType global permission, must not be null.
     * @return Collection of {@link JiraPermission#getPermType}, must never return null.
     *
     * @deprecated Use {@link #getPermissions(com.atlassian.jira.permission.GlobalPermissionKey)} instead. Since v6.2.5.
     */
    @Internal
    Collection<GlobalPermissionEntry> getPermissions(GlobalPermissionType globalPermissionType);

    /**
     * Retrieve a list of user groups which have been granted the specified permission.
     * <p>
     * The returned {@link GlobalPermissionEntry} contains a reference to the user group.
     * </p>
     *
     * @param globalPermissionKey global permission, must not be null.
     * @return Collection of {@link GlobalPermissionEntry}, never null.
     *
     * @since 6.2.5
     */
    @ExperimentalApi
    @Nonnull
    Collection<GlobalPermissionEntry> getPermissions(@Nonnull GlobalPermissionKey globalPermissionKey);

    /**
     * Revokes a global permission for a user group
     *
     * @param permissionType  the global permission.
     * @param group           the group name. NULL means the anyone group.
     *
     * @return true if the permission was revoked, false if not (e.g. the group does not have this permission)
     * @deprecated Use {@link #removePermission(com.atlassian.jira.permission.GlobalPermissionType, String)} instead.
      */
    boolean removePermission(int permissionType, String group);

    /**
     * Revokes a global permission for a user group
     *
     * @param globalPermissionType global permission, must not be null.
     * @param group          the group name. NULL means the anyone group.
     *
     * @return true if the permission was revoked, false if not (e.g. the group does not have this permission)
     */
    @ExperimentalApi
    boolean removePermission(GlobalPermissionType globalPermissionType, String group);

    /**
     * Revoke all global permissions for a user group.
     *
     * @param group cannot NOT be null and the group must exist.
     *
     * @return true, if this group does not have any global permissions
     */
    boolean removePermissions(String group);

    /**
     * Check if a global permission is granted for an anonymous user.
     * <p/>
     * If the permission is {@link Permissions#ADMINISTER} and the lookup is false then the same
     * query will be executed for the {@link Permissions#SYSTEM_ADMIN} permission type, since
     * it is implied that having a {@link Permissions#SYSTEM_ADMIN} permission grants
     * {@link Permissions#ADMINISTER} rights.
     * <p/>
     * Note: Use {@link #hasPermission(int, User)} method is you have the user object,
     * i.e. user is not anonymous.
     * <p/>
     * <b>If you are using this method directly, consider using
     * {@link com.atlassian.jira.security.PermissionManager#hasPermission(int, User)}
     * instead as it handles logged in and anonymous users as well.</b>
     *
     * @param permissionType must be global permission
     * @return true the anonymous user has the permission of given type, false otherwise
     * @see #hasPermission(int, User)
     * @deprecated Use {@link #hasPermission(com.atlassian.jira.permission.GlobalPermissionKey, com.atlassian.jira.user.ApplicationUser)} instead.
     */
    boolean hasPermission(int permissionType);

    /**
     * Left in here temporarily in case it is being used by SD 2.0
     *
     * @deprecated Use {@link #hasPermission(com.atlassian.jira.permission.GlobalPermissionKey, com.atlassian.jira.user.ApplicationUser)} instead. Since v6.2.5.
     */
    @Internal
    boolean hasPermission(@Nonnull GlobalPermissionType globalPermissionType);

    /**
     * Check if a global permission for one of the users groups exists.
     * <p/>
     * If the permission type is {@link Permissions#ADMINISTER} and the lookup is false then the same
     * query will be executed for the {@link Permissions#SYSTEM_ADMIN} permission type, since
     * it is implied that having a {@link Permissions#SYSTEM_ADMIN} permission grants
     * {@link Permissions#ADMINISTER} rights.
     * <p/>
     * <b>Note:</b> Use {@link #hasPermission(int)} method is you do not have the user object, i.e. user is anonymous.
     * <p/>
     * <b>If you are using this method directly, consider using
     * {@link com.atlassian.jira.security.PermissionManager#hasPermission(int, User)}
     * instead as it handles logged in and anonymous users as well.</b>
     *
     * @param permissionType must be a global permission
     * @param user           must not be null
     * @return true if the given user has the permission of given type, otherwise false
     * @see #hasPermission(int)
     * @see com.atlassian.jira.security.PermissionManager#hasPermission(int, User)
     * @deprecated Use {@link #hasPermission(com.atlassian.jira.permission.GlobalPermissionKey, com.atlassian.jira.user.ApplicationUser)} instead.
     */
    boolean hasPermission(int permissionType, User user);

    /**
     * Check if a global permission for one of the users groups exists.
     * <p/>
     * If the permission type is {@link Permissions#ADMINISTER} and the lookup is false then the same
     * query will be executed for the {@link Permissions#SYSTEM_ADMIN} permission type, since
     * it is implied that having a {@link Permissions#SYSTEM_ADMIN} permission grants
     * {@link Permissions#ADMINISTER} rights.
     * <p/>
     * <b>Note:</b> Use {@link #hasPermission(int)} method is you do not have the user object, i.e. user is anonymous.
     * <p/>
     * <b>If you are using this method directly, consider using
     * {@link com.atlassian.jira.security.PermissionManager#hasPermission(int, User)}
     * instead as it handles logged in and anonymous users as well.</b>
     *
     * @param permissionType must be a global permission
     * @param user           must not be null
     * @return true if the given user has the permission of given type, otherwise false
     * @see #hasPermission(int)
     * @see com.atlassian.jira.security.PermissionManager#hasPermission(int, User)
     * @deprecated Use {@link #hasPermission(com.atlassian.jira.permission.GlobalPermissionKey, com.atlassian.jira.user.ApplicationUser)} instead.
     */
    boolean hasPermission(int permissionType, ApplicationUser user);

    /**
     * Left in here temporarily in case it is being used by SD 2.0
     *
     * @deprecated Use {@link #hasPermission(com.atlassian.jira.permission.GlobalPermissionKey, com.atlassian.jira.user.ApplicationUser)} instead. Since v6.2.5.
     */
    @Internal
    boolean hasPermission(@Nonnull GlobalPermissionType globalPermissionType, @Nullable ApplicationUser user);

    /**
     * Check if the given user has the given Global Permission.
     * <p/>
     * If the permission type is {@link Permissions#ADMINISTER} and the lookup is false then the same
     * query will be executed for the {@link Permissions#SYSTEM_ADMIN} permission type, since
     * it is implied that having a {@link Permissions#SYSTEM_ADMIN} permission grants
     * {@link Permissions#ADMINISTER} rights.
     *
     * @param globalPermissionKey global permission, must not be null.
     * @param user The user - can be null indicating "anonymous"
     * @return true if the given user has the permission of given type, otherwise false.
     *
     * @since 6.2.5
     */
    @ExperimentalApi
    boolean hasPermission(@Nonnull GlobalPermissionKey globalPermissionKey, @Nullable ApplicationUser user);

    /**
     * Retrieve all the groups with this permission. Only groups directly associated with the permission will be
     * returned.
     *
     * @param permissionId must be a global permission
     * @return a Collection of {@link Group}'s, will never be null.
     * @deprecated Use {@link #getGroupsWithPermission(com.atlassian.jira.permission.GlobalPermissionKey)}
     */
    Collection<Group> getGroupsWithPermission(int permissionId);

    /**
     * Retrieve all the groups with this permission. Only groups directly associated with the permission will be
     * returned.
     *
     * @param globalPermissionType global permission, must not be null.
     * @return a Collection of {@link Group}'s, will never be null.
     *
     * @deprecated Use {@link #getGroupsWithPermission(com.atlassian.jira.permission.GlobalPermissionKey)} instead. Since v6.2.5.
     */
    @Internal
    Collection<Group> getGroupsWithPermission(@Nonnull GlobalPermissionType globalPermissionType);

    /**
     * Retrieve all the groups with the given permission.
     * <p>
     *   Only groups directly associated with the permission will be returned.
     * </p>
     *
     * @param permissionKey global permission, must not be null.
     * @return a Collection of {@link Group}'s, will never be null.
     *
     * @since 6.2.5
     */
    @ExperimentalApi
    @Nonnull
    Collection<Group> getGroupsWithPermission(@Nonnull GlobalPermissionKey permissionKey);

    /**
     * Retrieve all the group names with this permission. Only group names directly associated with the permission will
     * be returned.
     *
     * @param permissionId must be a global permission
     * @return a Collection of String, group names, will never be null.
     * @deprecated Use {@link #getGroupNamesWithPermission(com.atlassian.jira.permission.GlobalPermissionKey)}
     */
    Collection<String> getGroupNames(int permissionId);

    /**
     * Retrieve all the group names with this permission. Only group names directly associated with the permission will
     * be returned.
     *
     * @param globalPermissionType global permission, must not be null.
     * @return a Collection of String, group names, will never be null.
     *
     * @deprecated Use {@link #getGroupNamesWithPermission(com.atlassian.jira.permission.GlobalPermissionKey)} instead. Since v6.2.5.
     */
    @Internal
    Collection<String> getGroupNames(@Nonnull GlobalPermissionType globalPermissionType);

    /**
     * Retrieve all the group names with this permission. Only group names directly associated with the permission will
     * be returned.
     *
     * @param permissionKey global permission, must not be null.
     * @return a Collection of String, group names, will never be null.
     *
     * @since 6.2.5
     */
    @ExperimentalApi
    @Nonnull
    Collection<String> getGroupNamesWithPermission(@Nonnull GlobalPermissionKey permissionKey);

    /**
     * @param permissionId id of the permission to check.
     * @return true if provided id is the id of a global permission.
     *
     * @deprecated Use {@link com.atlassian.jira.permission.GlobalPermissionKey} instead of {@code int}. Since v6.2.5
     */
    @ExperimentalApi
    boolean isGlobalPermission(int permissionId);

    @Internal
    void clearCache();
}
