package com.atlassian.jira.security;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Option;
import com.atlassian.jira.JiraManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.permission.ProjectPermissionCategory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * PermissionManager responsible for all project specific permissions.
 * <p>
 * See  <a href="http://www.atlassian.com/software/jira/docs/latest/permissions.html">JIRA Permissions</a>.
 * <p>
 * For all global Permissions it is recommended to use {@link GlobalPermissionManager}.
 */
@PublicApi
public interface PermissionManager extends JiraManager
{
    /**
     * @return all project permissions.
     * @since v6.3
     */
    Collection<ProjectPermission> getAllProjectPermissions();

    /**
     * @param category project permission category.
     * @return all project permissions of the specified category.
     * @since v6.3
     */
    Collection<ProjectPermission> getProjectPermissions(@Nonnull ProjectPermissionCategory category);

    /**
     * Returns a project permission matching the specified key.
     *
     * @param permissionKey A project permission key.
     * @return a project permission for the given permission key.
     *      {@link Option#none} if there is no permission with this key.
     * @since v6.3
     */
    Option<ProjectPermission> getProjectPermission(@Nonnull ProjectPermissionKey permissionKey);

    /**
     * Grants a permission to the system.
     *
     * @param permissionsId Permissions value. E.g. See {@link Permissions#ADMINISTER}
     * @param scheme        If null permission is global otherwise it is added to the scheme
     * @param parameter     Used for e.g. group name
     * @param securityType  e.g. GroupDropdown.DESC
     *
     * @throws CreateException if permission creation fails
     * @deprecated Use {@link com.atlassian.jira.permission.PermissionSchemeManager#createSchemeEntity(GenericValue, com.atlassian.jira.scheme.SchemeEntity)}
     *      to add project permissions to a permission scheme. Use {@link GlobalPermissionManager#addPermission(com.atlassian.jira.permission.GlobalPermissionType, String)}
     *      to add global permissions. Since v6.3.
     */
    @Deprecated
    public void addPermission(int permissionsId, GenericValue scheme, String parameter, String securityType)
            throws CreateException;

    /**
     * Checks to see if this user has the specified permission. It will check only global permissions as there are
     * no other permissions to check.
     *
     * @param permissionsId permission id
     * @param user          user, can be null - anonymous user
     * @return true if user is granted given permission, false otherwise
     * @deprecated Use {@link #hasPermission(int, ApplicationUser)} instead. Since v6.0.
     * @see com.atlassian.jira.security.GlobalPermissionManager#hasPermission(int, User)
     */
    @Deprecated
    public boolean hasPermission(int permissionsId, User user);

    /**
     * Checks to see if this user has the specified permission. It will check only global permissions as there are
     * no other permissions to check.
     *
     * @param permissionsId permission id
     * @param user          user, can be null - anonymous user
     * @return true if user is granted given permission, false otherwise
     * @deprecated Use {@link com.atlassian.jira.security.GlobalPermissionManager#hasPermission(com.atlassian.jira.permission.GlobalPermissionType, ApplicationUser)} instead. Since v6.3.
     * @see com.atlassian.jira.security.GlobalPermissionManager#hasPermission(int, User)
     *
     * @deprecated Use {@link GlobalPermissionManager#hasPermission(com.atlassian.jira.permission.GlobalPermissionKey, com.atlassian.jira.user.ApplicationUser)} instead. Since v6.2.5.
     */
    @Deprecated
    public boolean hasPermission(int permissionsId, ApplicationUser user);

    /**
     * Checks to see if this has permission to see the specified entity. Check Permissions scheme(s) if the entity
     * is project. Check Permissions scheme(s) and issue level security scheme(s) if the entity is an issue.
     *
     * @param permissionsId Not a global permission
     * @param entity        Not null.  Must be either an issue or project.
     * @param u             User object, possibly null if JIRA is accessed anonymously
     * @return True if there are sufficient rights to access the entity supplied
     * @throws IllegalArgumentException If the entity supplied is NOT an issue or project.
     * @deprecated <br/>Since v3.11: Use {@link #hasPermission(int, Issue, User)} for Issues or
     *          {@link #hasPermission(int, Project, User)} for Projects.
     *          <br/>Since v6.0: Use {@link #hasPermission(int, Issue, ApplicationUser)} for Issues or
     *          {@link #hasPermission(int, Project, ApplicationUser)} for Projects.
     *          <br/>Since v6.3: Use {@link #hasPermission(ProjectPermissionKey, Issue, ApplicationUser)} for Issues or
     *          {@link #hasPermission(ProjectPermissionKey, Project, ApplicationUser)} for Projects.
     */
    @Deprecated
    public boolean hasPermission(int permissionsId, GenericValue entity, User u);

    /**
     * Checks to see if this user has permission to see the specified issue.
     * <p/>
     * Note that if the issue's generic value is null, it is assumed that the issue is currently being created, and so
     * the permission check call is deferred to the issue's project object, with the issueCreation flag set to true. See
     * JRA-14788 for more info.
     *
     * @param permissionsId Not a global permission
     * @param issue        The Issue (cannot be null)
     * @param user             User object, possibly null if JIRA is accessed anonymously
     * @return True if there are sufficient rights to access the entity supplied
     * @deprecated <br/>Since v6.0: Use {@link #hasPermission(int, Issue, ApplicationUser)} instead.
     *          <br/>Since v6.3: Use {@link #hasPermission(ProjectPermissionKey, Issue, ApplicationUser)} instead.
     */
    @Deprecated
    public boolean hasPermission(int permissionsId, Issue issue, User user);

    /**
     * Checks to see if this user has permission to see the specified issue.
     * <p/>
     * Note that if the issue's generic value is null, it is assumed that the issue is currently being created, and so
     * the permission check call is deferred to the issue's project object, with the issueCreation flag set to true. See
     * JRA-14788 for more info.
     *
     * @param permissionsId Not a global permission
     * @param issue        The Issue (cannot be null)
     * @param user             User object, possibly null if JIRA is accessed anonymously
     * @return True if there are sufficient rights to access the entity supplied
     * @deprecated Use {@link #hasPermission(ProjectPermissionKey, Issue, ApplicationUser)} instead. Since v6.3.
     */
    @Deprecated
    public boolean hasPermission(int permissionsId, Issue issue, ApplicationUser user);

    /**
     * Checks to see if this user has permission to see the specified issue.
     * <p/>
     * Note that if the issue's generic value is null, it is assumed that the issue is currently being created, and so
     * the permission check call is deferred to the issue's project object, with the issueCreation flag set to true. See
     * JRA-14788 for more info.
     *
     * @param permissionKey Not a global permission key
     * @param issue        The Issue (cannot be null)
     * @param user             User object, possibly null if JIRA is accessed anonymously
     * @return True if there are sufficient rights to access the entity supplied
     * @since v6.3
     */
    boolean hasPermission(@Nonnull ProjectPermissionKey permissionKey, @Nonnull Issue issue, @Nullable ApplicationUser user);

    /**
     * Checks whether the specified user has a specified permission within the context of a specified project.
     *
     * @param permissionsId A non-global permission, i.e. a permission that is granted via a project context
     * @param project       The project that is the context of the permission check.
     * @param user          The person to perform the permission check for
     * @return true if the user has the specified permission in the context of the supplied project
     * @deprecated <br/>Since v6.0: Use {@link #hasPermission(int, Project, ApplicationUser)} instead.
     *          <br/>Since v6.3: Use {@link #hasPermission(ProjectPermissionKey, Project, ApplicationUser)} instead.
     */
    @Deprecated
    public boolean hasPermission(int permissionsId, Project project, User user);

    /**
     * Checks whether the specified user has a specified permission within the context of a specified project.
     *
     * @param permissionsId A non-global permission, i.e. a permission that is granted via a project context
     * @param project       The project that is the context of the permission check.
     * @param user          The person to perform the permission check for
     * @return true if the user has the specified permission in the context of the supplied project
     * @deprecated Use {@link #hasPermission(ProjectPermissionKey, Project, ApplicationUser)} instead. Since v6.3.
     */
    @Deprecated
    public boolean hasPermission(int permissionsId, Project project, ApplicationUser user);

    /**
     * Checks whether the specified user has a specified permission within the context of a specified project.
     *
     * @param permissionKey A non-global permission, i.e. a permission that is granted via a project context
     * @param project       The project that is the context of the permission check.
     * @param user          The person to perform the permission check for
     * @return true if the user has the specified permission in the context of the supplied project
     * @since v6.3
     */
    boolean hasPermission(@Nonnull ProjectPermissionKey permissionKey, @Nonnull Project project, @Nullable ApplicationUser user);

    /**
     * Checks whether the specified user has a specified permission within the context of a specified project.
     *
     * @param permissionsId A non-global permission, i.e. a permission that is granted via a project context
     * @param project       The project that is the context of the permission check.
     * @param user          The person to perform the permission check for
     * @param issueCreation Whether this permission is being checked during issue creation
     * @return true if the user has the specified permission in the context of the supplied project
     * @deprecated <br/>Since v6.0: Use {@link #hasPermission(int, Project, ApplicationUser, boolean)} instead.
     *          <br/>Since v6.3: Use {@link #hasPermission(ProjectPermissionKey, Project, ApplicationUser, boolean)} instead.
     */
    @Deprecated
    public boolean hasPermission(int permissionsId, Project project, User user, boolean issueCreation);

    /**
     * Checks whether the specified user has a specified permission within the context of a specified project.
     *
     * @param permissionsId A non-global permission, i.e. a permission that is granted via a project context
     * @param project       The project that is the context of the permission check.
     * @param user          The person to perform the permission check for
     * @param issueCreation Whether this permission is being checked during issue creation
     * @return true if the user has the specified permission in the context of the supplied project
     * @deprecated Use {@link #hasPermission(ProjectPermissionKey, Project, ApplicationUser, boolean)} instead. Since v6.3.
     */
    @Deprecated
    public boolean hasPermission(int permissionsId, Project project, ApplicationUser user, boolean issueCreation);

    /**
     * Checks whether the specified user has a specified permission within the context of a specified project.
     *
     * @param permissionKey A non-global permission, i.e. a permission that is granted via a project context
     * @param project       The project that is the context of the permission check.
     * @param user          The person to perform the permission check for
     * @param issueCreation Whether this permission is being checked during issue creation
     * @return true if the user has the specified permission in the context of the supplied project
     * @since v6.3
     */
    boolean hasPermission(@Nonnull ProjectPermissionKey permissionKey, @Nonnull Project project, @Nullable ApplicationUser user, boolean issueCreation);

    /**
     * Does the same as {@link #hasPermission(int,org.ofbiz.core.entity.GenericValue,User)} except
     * the entity is a project {@link GenericValue}.
     *
     * @param permissionsId Not a global permission
     * @param project       Not null.
     * @param user             User object, possibly null if JIRA is accessed anonymously
     * @param issueCreation Whether this permission is being checked during issue creation
     * @return True if there are sufficient rights to access the entity supplied
     * @deprecated <br/>Since v3.11: Use {@link #hasPermission(int, Issue, User)} for Issues or
     *             {@link #hasPermission(int, Project, User, boolean)} for Projects.
     *             <br/>Since v6.0: Use {@link #hasPermission(int, Issue, ApplicationUser)} for
     *             Issues or {@link #hasPermission(int, Project, ApplicationUser, boolean)} for
     *             Projects.
     *             <br/>Since v6.3: Use {@link #hasPermission(ProjectPermissionKey, Issue, ApplicationUser)} for
     *             Issues or {@link #hasPermission(ProjectPermissionKey, Project, ApplicationUser, boolean)} for
     *             Projects.
     */
    @Deprecated
    public boolean hasPermission(int permissionsId, GenericValue project, User user, boolean issueCreation);

    /**
     * Remove all permissions that have used this group
     *
     * @param group The name of the group that needs to be removed, must NOT be null and must be a real group
     * @throws RemoveException if permission removal fails
     */
    public void removeGroupPermissions(String group) throws RemoveException;

    /**
     * Remove all permissions that have used this username
     *
     * @param username username of the user whose permissions are to be removed
     * @throws RemoveException if permission removal fails
     * @deprecated Use {@link #removeUserPermissions(ApplicationUser)} instead. Since v6.0.
     */
    @Deprecated
    public void removeUserPermissions(String username) throws RemoveException;

    /**
     * Remove all permissions that have been assigned to this user
     *
     * @param user the user whose permissions are to be removed
     * @throws RemoveException
     * @since v6.0
     */
    public void removeUserPermissions(ApplicationUser user) throws RemoveException;

    /////////////// Project Permission Methods //////////////////////////////////////////

    /**
     * Can this user see at least one project with this permission
     *
     * @param permissionId must NOT be a global permission
     * @param user         user being checked
     * @return true the given user can see at least one project with the given permission, false otherwise
     * @deprecated Use {@link #hasProjects(ProjectPermissionKey, ApplicationUser)} instead. Since v6.3.
     */
    @Deprecated
    public boolean hasProjects(int permissionId, User user);

    /**
     * Can this user see at least one project with this permission
     *
     * @param permissionId must NOT be a global permission
     * @param user         user being checked
     * @return true the given user can see at least one project with the given permission, false otherwise
     * @deprecated Use {@link #hasProjects(ProjectPermissionKey, ApplicationUser)} instead. Since v6.3.
     */
    @Deprecated
    public boolean hasProjects(int permissionId, ApplicationUser user);

    /**
     * Can this user see at least one project with this permission
     *
     * @param permissionKey must NOT be a global permission
     * @param user         user being checked
     * @return true the given user can see at least one project with the given permission, false otherwise
     * @since v6.3
     */
    boolean hasProjects(@Nonnull ProjectPermissionKey permissionKey, @Nullable ApplicationUser user);

    /**
     * Retrieve a list of projects this user has the permission for
     *
     * @param permissionId must NOT be a global permission
     * @param user         user
     * @return a collection of {@link GenericValue} objects
     * @deprecated Please use {@link #getProjectObjects(int, com.atlassian.crowd.embedded.api.User)}. Since v4.3
     */
    @Deprecated
    public Collection<GenericValue> getProjects(int permissionId, User user);

    /**
     * Retrieve a list of project objects this user has the permission for
     *
     * @param permissionId must NOT be a global permission
     * @param user user
     * @return a collection of {@link Project} objects
     * @deprecated <br/>Since v6.0: Use {@link #getProjects(int, ApplicationUser)} instead.
     *          <br/>Since v6.3: Use {@link #getProjects(ProjectPermissionKey, ApplicationUser)} instead.
     */
    @Deprecated
    public Collection<Project> getProjectObjects(int permissionId, User user);

    /**
     * Retrieve a list of project objects this user has the permission for
     *
     * @param permissionId must NOT be a global permission
     * @param user user
     * @return a collection of {@link Project} objects
     * @deprecated Use {@link #getProjects(ProjectPermissionKey, ApplicationUser)} instead. Since v6.3.
     */
    @Deprecated
    public Collection<Project> getProjects(int permissionId, ApplicationUser user);

    /**
     * Retrieve a list of project objects this user has the permission for
     *
     * @param permissionKey must NOT be a global permission
     * @param user user
     * @return a collection of {@link Project} objects
     * @since v6.3
     */
    Collection<Project> getProjects(@Nonnull ProjectPermissionKey permissionKey, @Nullable ApplicationUser user);

    /**
     * Retrieve a list of projects associated with the specified category, that this user has the permissions for
     *
     * @param permissionId permission id
     * @param user         user
     * @param category     GenericValue representing category
     * @return a collection of {@link GenericValue} objects
     *
     * @deprecated <br/>Since v5.0: Use {@link #getProjects(int, User, ProjectCategory)} instead.
     *      <br/>Since v6.0: Use {@link #getProjects(int, ApplicationUser, ProjectCategory)} instead.
     *      <br/>Since v6.3: Use {@link #getProjects(ProjectPermissionKey, ApplicationUser, ProjectCategory)} instead.
     */
    @Deprecated
    public Collection<GenericValue> getProjects(int permissionId, User user, GenericValue category);

    /**
     * Returns the list of projects associated with the specified category, that this user has the permissions for.
     *
     * @param permissionId permission id
     * @param user         user
     * @param projectCategory     the ProjectCategory
     * @return the list of projects associated with the specified category, that this user has the permissions for.
     * @deprecated <br/>Since v6.0: Use {@link #getProjects(int, ApplicationUser, ProjectCategory)} instead.
     *          <br/>Since v6.3: Use {@link #getProjects(ProjectPermissionKey, ApplicationUser, ProjectCategory)} instead.
     */
    @Deprecated
    public Collection<Project> getProjects(int permissionId, User user, ProjectCategory projectCategory);

    /**
     * Returns the list of projects associated with the specified category, that this user has the permissions for.
     *
     * @param permissionId permission id
     * @param user         user
     * @param projectCategory     the ProjectCategory
     * @return the list of projects associated with the specified category, that this user has the permissions for.
     * @deprecated Use {@link #getProjects(ProjectPermissionKey, ApplicationUser, ProjectCategory)} instead. Since v6.3.
     */
    @Deprecated
    public Collection<Project> getProjects(int permissionId, ApplicationUser user, ProjectCategory projectCategory);

    /**
     * Returns the list of projects associated with the specified category, that this user has the permissions for.
     *
     * @param permissionKey permission key
     * @param user         user
     * @param projectCategory     the ProjectCategory
     * @return the list of projects associated with the specified category, that this user has the permissions for.
     * @since v6.3
     */
    Collection<Project> getProjects(@Nonnull ProjectPermissionKey permissionKey, @Nullable ApplicationUser user, @Nonnull ProjectCategory projectCategory);

    /////////////// Group Permission Methods //////////////////////////////////////////

    /**
     * Retrieve all groups that are used in the permission globally and in the project.
     *
     * @param permissionId permission id
     * @param project      project from which to retrieve groups
     * @return a collection of Groups
     */
    public Collection<Group> getAllGroups(int permissionId, Project project);
}
