package com.atlassian.jira.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.permission.ProjectPermissionCategory;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.security.plugin.ProjectPermissionOverrideModuleDescriptor;
import com.atlassian.jira.security.plugin.ProjectPermissionTypesManager;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.security.type.SingleUser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.ozymandias.SafePluginPointAccess;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.permission.LegacyProjectPermissionKeyMapping.getKey;
import static com.atlassian.jira.permission.ProjectPermissions.BROWSE_PROJECTS;
import static com.atlassian.jira.security.plugin.ProjectPermissionOverride.Decision;
import static com.atlassian.jira.security.plugin.ProjectPermissionOverride.Decision.DENY;
import static com.atlassian.jira.user.ApplicationUsers.from;
import static com.atlassian.jira.user.ApplicationUsers.toDirectoryUser;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Collections.emptyList;

/**
 * An abstract PermissionManager that implements a lot of the common functionality to all PermissionManagers.
 *
 */
public class DefaultPermissionManager implements PermissionManager
{
    private static final Logger log = Logger.getLogger(DefaultPermissionManager.class);

    private ProjectPermissionTypesManager projectPermissionTypesManager;

    public DefaultPermissionManager(ProjectPermissionTypesManager projectPermissionTypesManager)
    {
        this.projectPermissionTypesManager = projectPermissionTypesManager;
    }

    @Override
    public Collection<ProjectPermission> getAllProjectPermissions()
    {
        return projectPermissionTypesManager.all();
    }

    @Override
    public Collection<ProjectPermission> getProjectPermissions(ProjectPermissionCategory category)
    {
        return projectPermissionTypesManager.withCategory(category);
    }

    @Override
    public Option<ProjectPermission> getProjectPermission(@Nonnull ProjectPermissionKey permissionKey)
    {
        return projectPermissionTypesManager.withKey(permissionKey);
    }

    /**
     * Adds a permission to the system
     *
     * @param permissionsId Permissions value
     * @param scheme If null permission is global otherwise it is added to the scheme
     * @param parameter Used for e.g. group name
     * @param securityType e.g. GroupDropdown.DESC
     */
    public void addPermission(final int permissionsId, final GenericValue scheme, final String parameter, final String securityType)
            throws CreateException
    {
        if (isGlobalPermission(permissionsId) && (scheme != null))
        {
            throw new IllegalArgumentException("Can not create a global permissions in a scheme");
        }
        if (scheme == null)
        {
            ComponentAccessor.getGlobalPermissionManager().addPermission(permissionsId, parameter);
        }
        else
        {
            SchemeEntity schemeEntity = new SchemeEntity(securityType, parameter, permissionsId);
            try
            {
                ComponentAccessor.getPermissionSchemeManager().createSchemeEntity(scheme, schemeEntity);
            }
            catch (final GenericEntityException e)
            {
                throw new CreateException(e);
            }
        }
    }

    /**
     * Checks to see if this user has the specified permission<br/>
     * It will check only global permissions as there are no other permissions to check<br/>
     *
     * @param permissionsId permission id
     * @param user user
     * @return true if user is granted given permission, false otherwise
     */
    public boolean hasPermission(final int permissionsId, final User user)
    {
        return hasPermission(permissionsId, from(user));
    }

    public boolean hasPermission(final int permissionsId, final ApplicationUser user)
    {
        if (!isGlobalPermission(permissionsId))
        {
            throw new IllegalArgumentException("Expected global permission, got " + permissionsId);
        }

        if (user == null)
        {
            return ComponentAccessor.getGlobalPermissionManager().hasPermission(permissionsId);
        }
        else
        {
            return user.isActive() && ComponentAccessor.getGlobalPermissionManager().hasPermission(permissionsId, user);
        }
    }

    /**
     * Checks to see if this has permission to see the specified entity<br>
     * Check Permissions scheme(s) if the entity is project<br>
     * Check Permissions scheme(s) and issue level security scheme(s) if the entity is an issue<br>
     *
     * @param permissionId, not a global permission
     * @param projectOrIssue not null must be Project or Issue
     * @param user User object, possibly null if JIRA is accessed anonymously
     */
    public boolean hasPermission(final int permissionId, final GenericValue projectOrIssue, final User user)
    {
        final ProjectPermissionKey permissionKey = getNonGlobalKey(permissionId);
        return hasPermission(permissionKey, projectOrIssue, user);
    }

    protected boolean hasPermission(final ProjectPermissionKey permissionKey, final GenericValue projectOrIssue, final User user)
    {
        return projectPermissionTypesManager.exists(permissionKey) && getProjectOrIssue(projectOrIssue).fold(new Function<Project, Boolean>()
        {
            @Override
            public Boolean apply(final Project project)
            {
                final boolean corePermissionCheckResult = doProjectPermissionCheck(permissionKey, project, user, false);
                return withPermissionOverriding(corePermissionCheckResult, permissionKey, project, from(user));
            }
        }, new Function<Issue, Boolean>()
        {
            @Override
            public Boolean apply(final Issue issue)
            {
                final boolean corePermissionCheckResult = doIssuePermissionCheck(permissionKey, issue, user, false);
                return withPermissionOverriding(corePermissionCheckResult, permissionKey, issue.getProjectObject(), from(user));
            }
        });
    }

    public boolean hasPermission(final int permissionsId, final Issue issue, final User user)
    {
        ProjectPermissionKey permissionKey = getNonGlobalKey(permissionsId);
        return hasPermission(permissionKey, issue, user);
    }

    protected boolean hasPermission(final ProjectPermissionKey permissionKey, final Issue issue, final User user)
    {
        return withPermissionOverriding(doIssuePermissionCheck(permissionKey, issue, user), permissionKey, issue.getProjectObject(), from(user));
    }

    public boolean hasPermission(int permissionsId, Issue issue, ApplicationUser user)
    {
        ProjectPermissionKey permissionKey = getNonGlobalKey(permissionsId);
        return hasPermission(permissionKey, issue, user);
    }

    public boolean hasPermission(ProjectPermissionKey permissionKey, Issue issue, ApplicationUser user)
    {
        return withPermissionOverriding(doIssuePermissionCheck(permissionKey, issue, toDirectoryUser(user)), permissionKey, issue.getProjectObject(), user);
    }

    public boolean hasPermission(final int permissionsId, final Project project, final User user)
    {
        ProjectPermissionKey permissionKey = getNonGlobalKey(permissionsId);
        return hasPermission(permissionKey, project, user);
    }

    protected boolean hasPermission(ProjectPermissionKey permissionKey, final Project project, final User user)
    {
        return withPermissionOverriding(doProjectPermissionCheck(permissionKey, project, user, false), permissionKey, project, from(user));
    }

    public boolean hasPermission(int permissionsId, Project project, ApplicationUser user)
    {
        ProjectPermissionKey permissionKey = getNonGlobalKey(permissionsId);
        return hasPermission(permissionKey, project, user);
    }

    public boolean hasPermission(ProjectPermissionKey permissionsKey, Project project, ApplicationUser user)
    {
        return withPermissionOverriding(doProjectPermissionCheck(permissionsKey, project, toDirectoryUser(user), false), permissionsKey, project, user);
    }

    public boolean hasPermission(final int permissionsId, final Project project, final User user, final boolean issueCreation)
    {
        ProjectPermissionKey permissionKey = getNonGlobalKey(permissionsId);
        return hasPermission(permissionKey, project, user, issueCreation);
    }

    protected boolean hasPermission(final ProjectPermissionKey permissionKey, final Project project, final User user, final boolean issueCreation)
    {
        return withPermissionOverriding(doProjectPermissionCheck(permissionKey, project, user, issueCreation), permissionKey, project, from(user));
    }

    public boolean hasPermission(int permissionsId, Project project, ApplicationUser user, boolean issueCreation)
    {
        ProjectPermissionKey permissionKey = getNonGlobalKey(permissionsId);
        return hasPermission(permissionKey, project, user, issueCreation);
    }

    public boolean hasPermission(ProjectPermissionKey permissionsKey, Project project, ApplicationUser user, boolean issueCreation)
    {
        return withPermissionOverriding(doProjectPermissionCheck(permissionsKey, project, toDirectoryUser(user), issueCreation), permissionsKey, project, user);
    }

    public boolean hasPermission(final int permissionId, final GenericValue entity, final User user, final boolean issueCreation)
    {
        final ProjectPermissionKey permissionKey = getNonGlobalKey(permissionId);
        return hasPermission(permissionKey, entity, user, issueCreation);
    }

    protected boolean hasPermission(final ProjectPermissionKey permissionKey, final GenericValue entity, final User user, final boolean issueCreation)
    {
        return projectPermissionTypesManager.exists(permissionKey) && getProjectOrIssue(entity).fold(new Function<Project, Boolean>()
        {
            @Override
            public Boolean apply(final Project project)
            {
                boolean corePermissionCheckResult = doProjectPermissionCheck(permissionKey, project, user, issueCreation);
                return withPermissionOverriding(corePermissionCheckResult, permissionKey, project, from(user));
            }
        }, new Function<Issue, Boolean>()
        {
            @Override
            public Boolean apply(final Issue issue)
            {
                boolean corePermissionCheckResult = doIssuePermissionCheck(permissionKey, issue, user, issueCreation);
                return withPermissionOverriding(corePermissionCheckResult, permissionKey, issue.getProjectObject(), from(user));
            }
        });
    }

    private boolean doIssuePermissionCheck(ProjectPermissionKey permissionKey, final Issue issue, final User user)
    {
        // JRA-14788: if generic value of issue object is null, need to defer permission check to project object.
        if (issue.getId() != null)
        {
            return doIssuePermissionCheck(permissionKey, issue, user, false);
        }
        else
        {
            return doProjectPermissionCheck(permissionKey, issue.getProjectObject(), user, true);
        }
    }

    private boolean doIssuePermissionCheck(ProjectPermissionKey permissionKey, final Issue issue, final User user, final boolean issueCreation)
    {
        // Check that the user can actually see the project this issue is in
        if (!doProjectPermissionCheck(permissionKey, issue.getProjectObject(), user, false))
        {
            return false;
        }

        // Check the project permissions apply to this issue
        if (!doEntityPermissionCheck(permissionKey, issue.getGenericValue(), user, issueCreation))
        {
            return false;
        }

        // When checking Issue Visibility (BROWSE_PROJECT permission), also check the Security Level
        // JRA-40124 Don't check Security Level for other permissions - this is unnecessary and can cause false negatives
        if (ProjectPermissions.BROWSE_PROJECTS.equals(permissionKey))
        {
            return hasIssueSecurityLevelPermission(user, issue, issueCreation);
        }
        return true;
    }

    private boolean hasIssueSecurityLevelPermission(final User user, final Issue issue, final boolean issueCreation)
    {
        // If there is issue level security check that otherwise the must be able to see the issue
        if (user == null)
        {
            return ComponentAccessor.getComponent(IssueSecuritySchemeManager.class).hasSchemeAuthority(issue.getSecurityLevelId(), issue.getGenericValue());
        }
        return ComponentAccessor.getComponent(IssueSecuritySchemeManager.class).hasSchemeAuthority(issue.getSecurityLevelId(), issue.getGenericValue(), user, issueCreation);
    }

    private boolean doProjectPermissionCheck(ProjectPermissionKey permissionKey, final Project project, final User user, final boolean issueCreation)
    {
        if (project == null || project.getId() == null)
        {
            throw new IllegalArgumentException("The Project argument and its backing generic value must not be null");
        }

        return doEntityPermissionCheck(permissionKey, project.getGenericValue(), user, issueCreation);
    }

    protected boolean doEntityPermissionCheck(ProjectPermissionKey permissionKey, final GenericValue entity, final User user, final boolean issueCreation)
    {
        if (!projectPermissionTypesManager.exists(permissionKey))
        {
            return false;
        }

        // Check scheme manager for the project to see it this project has the permission
        if (user == null)
        {
            return ComponentAccessor.getPermissionSchemeManager().hasSchemeAuthority(permissionKey, entity);
        }
        else
        {
            return user.isActive() && ComponentAccessor.getPermissionSchemeManager().hasSchemeAuthority(permissionKey, entity, user, issueCreation);
        }
    }

    /**
     * Remove all permissions that have used this group
     *
     * @param group The name of the group that needs to be removed, must NOT be null and must be a real group
     */
    public void removeGroupPermissions(final String group) throws RemoveException
    {
        notNull("group", group);
        notNull(ComponentAccessor.getGroupManager().getGroup(group));

        ComponentAccessor.getGlobalPermissionManager().removePermissions(group);
        ComponentAccessor.getPermissionSchemeManager().removeEntities(GroupDropdown.DESC, group);

        //If there is issue level security check that otherwise the must be able to see the issue
        ComponentAccessor.getComponent(IssueSecuritySchemeManager.class).removeEntities(GroupDropdown.DESC, group);
    }

    public void removeUserPermissions(final String username) throws RemoveException
    {
        notNull("username", username);
        final ApplicationUser user = ComponentAccessor.getUserManager().getUserByName(username);
        notNull(username, user);
        removeUserPermissions(user);
    }

    public void removeUserPermissions(final ApplicationUser user) throws RemoveException
    {
        notNull("user", user);

        ComponentAccessor.getPermissionSchemeManager().removeEntities(SingleUser.DESC, user.getKey());

        //If there is issue level security check that otherwise the must be able to see the issue
        ComponentAccessor.getComponent(IssueSecuritySchemeManager.class).removeEntities(SingleUser.DESC, user.getKey());
    }

    /**
     * Can this user see at least one project with this permission
     *
     * @param permissionId must NOT be a global permission
     * @param user user
     * @throws IllegalArgumentException if the passed in permission id matches a global permission
     */
    public boolean hasProjects(final int permissionId, final User user)
    {
        return hasProjects(permissionId, from(user));
    }

    public boolean hasProjects(final int permissionId, final ApplicationUser user)
    {
        ProjectPermissionKey permissionKey = getNonGlobalKey(permissionId);
        return hasProjects(permissionKey, user);
    }

    public boolean hasProjects(final ProjectPermissionKey permissionKey, final ApplicationUser user)
    {
        return projectPermissionTypesManager.exists(permissionKey) &&
                Iterables.any(ComponentAccessor.getProjectManager().getProjectObjects(), new Predicate<Project>()
        {
            @Override
            public boolean apply(final Project project)
            {
                return hasPermission(permissionKey, project, user);
            }
        });
    }

    public Collection<Project> getProjectObjects(final int permissionId, final User user)
    {
        ProjectPermissionKey permissionKey = getNonGlobalKey(permissionId);
        return getProjectObjects(permissionKey, user);
    }

    public Collection<Project> getProjects(int permissionId, ApplicationUser user)
    {
        ProjectPermissionKey permissionKey = getNonGlobalKey(permissionId);
        return getProjects(permissionKey, user);
    }

    @Override
    public Collection<Project> getProjects(ProjectPermissionKey permissionKey, ApplicationUser user)
    {
        return getProjectObjects(permissionKey, toDirectoryUser(user));
    }

    protected Collection<Project> getProjectObjects(ProjectPermissionKey permissionKey, final User user)
    {
        return getProjectObjectsWithPermission(ComponentAccessor.getProjectManager().getProjectObjects(), permissionKey, user);
    }

    public Collection<GenericValue> getProjects(final int permissionId, final User user, final GenericValue category)
    {
        if (isGlobalPermission(permissionId))
        {
            throw new IllegalArgumentException("Permission type passed must NOT be a global permission, " + permissionId + " is global");
        }

        final Collection<GenericValue> projects;
        if (category == null)
        {
            projects = ComponentAccessor.getProjectManager().getProjectsWithNoCategory();
        }
        else
        {
            projects = ComponentAccessor.getProjectManager().getProjectsFromProjectCategory(category);
        }

        return getProjectsWithPermission(projects, permissionId, user);
    }

    public Collection<Project> getProjects(int permissionId, User user, ProjectCategory projectCategory)
    {
        ProjectPermissionKey permissionKey = getKey(permissionId);
        return getProjects(permissionKey, user, projectCategory);
    }

    public Collection<Project> getProjects(int permissionId, ApplicationUser user, ProjectCategory projectCategory)
    {
        ProjectPermissionKey permissionKey = getNonGlobalKey(permissionId);
        return getProjects(permissionKey, user, projectCategory);
    }

    public Collection<Project> getProjects(ProjectPermissionKey permissionKey, ApplicationUser user, ProjectCategory projectCategory)
    {
        return getProjects(permissionKey, toDirectoryUser(user), projectCategory);
    }

    private Collection<Project> getProjects(ProjectPermissionKey permissionKey, User user, ProjectCategory projectCategory)
    {
        final Collection<Project> projects;
        if (projectCategory == null)
        {
            projects = ComponentAccessor.getProjectManager().getProjectObjectsWithNoCategory();
        }
        else
        {
            projects = ComponentAccessor.getProjectManager().getProjectsFromProjectCategory(projectCategory);
        }

        return getProjectObjectsWithPermission(projects, permissionKey, user);
    }

    private Collection<GenericValue> getProjectsWithPermission(final Collection<GenericValue> projects, final int permissionId, final User user)
    {
        final List<GenericValue> permissibleProjects = new ArrayList<GenericValue>();
        for (final GenericValue project : projects)
        {
            if (hasPermission(permissionId, project, user))
            {
                permissibleProjects.add(project);
            }
        }
        return permissibleProjects;
    }

    private Collection<Project> getProjectObjectsWithPermission(final Collection<Project> projects, final ProjectPermissionKey permissionKey, final User user)
    {
        if (!projectPermissionTypesManager.exists(permissionKey))
        {
            return emptyList();
        }

        return Lists.newArrayList(Iterables.filter(projects, new Predicate<Project>()
        {
            @Override
            public boolean apply(final Project project)
            {
                return hasPermission(permissionKey, project, user);
            }
        }));
    }

    private ProjectPermissionKey getNonGlobalKey(int permissionId)
    {
        if (isGlobalPermission(permissionId))
        {
            throw new IllegalArgumentException("PermissionType passed to this function must NOT be a global permission, " + permissionId + " is global");
        }

        return getKey(permissionId);
    }

    protected boolean isGlobalPermission(final int permissionId)
    {
        return Permissions.isGlobalPermission(permissionId);
    }

    public Collection<Group> getAllGroups(int permissionId, Project project)
    {
        // get a set of the groups we're talking about
        final Set<Group> groups = new HashSet<Group>();

        groups.addAll(ComponentAccessor.getPermissionSchemeManager().getGroups((long) permissionId, project));
        groups.addAll(ComponentAccessor.getGlobalPermissionManager().getGroupsWithPermission(permissionId));

        return groups;
    }

    public Collection<GenericValue> getProjects(int permissionId, User user)
    {
        if (isGlobalPermission(permissionId))
        {
            throw new IllegalArgumentException("Permission type passed must NOT be a global permission " + permissionId + " is global");
        }

        final Collection<GenericValue> projects = ComponentAccessor.getProjectManager().getProjects();
        return getProjectsWithPermission(projects, permissionId, user);
    }


    private boolean withPermissionOverriding(final boolean corePermissionCheckResult, final ProjectPermissionKey permissionKey,
            final Project project, final ApplicationUser applicationUser)
    {
        // We currently don't allow overriding Browse Project because it would mean changing JQL searching and it is not required for SD ABP
        if (!corePermissionCheckResult || BROWSE_PROJECTS.equals(permissionKey))
        {
            return corePermissionCheckResult;
        }

        final Boolean pluginPermissionCheckDeclined = Objects.firstNonNull(Iterables.any(ComponentAccessor.getPluginAccessor().getEnabledModuleDescriptorsByClass(ProjectPermissionOverrideModuleDescriptor.class),
            new Predicate<ProjectPermissionOverrideModuleDescriptor>()
            {
                @Override
                public boolean apply(final ProjectPermissionOverrideModuleDescriptor permissionOverrideModuleDescriptor)
                {
                    return SafePluginPointAccess.safe(new Predicate<ProjectPermissionOverrideModuleDescriptor>()
                    {
                        @Override
                        public boolean apply(final ProjectPermissionOverrideModuleDescriptor permissionOverrideModuleDescriptor)
                        {
                            final Decision decision = permissionOverrideModuleDescriptor.getModule().hasPermission(permissionKey, project, applicationUser);
                            if (log.isDebugEnabled() && decision == DENY)
                            {
                                log.debug("Permission check result to project " + project.getKey() + "was overriden by " + permissionOverrideModuleDescriptor.getCompleteKey());
                            }
                            return decision == DENY;
                        }
                    }).apply(permissionOverrideModuleDescriptor);
                }
            }), false);

        // if any plugin declined we return opposite
        return !pluginPermissionCheckDeclined;
    }

    private Either<Project, Issue> getProjectOrIssue(final GenericValue entity)
    {
        notNull("entity", entity);

        if ("Issue".equals(entity.getEntityName()))
        {
            return Either.<Project, Issue>right(IssueImpl.getIssueObject(entity));
        }
        else if ("Project".equals(entity.getEntityName()))
        {
            return Either.<Project, Issue>left(new ProjectImpl(entity));
        }
        else
        {
            throw new IllegalArgumentException("The entity passed must be a Project or an Issue not a " + entity.getEntityName());
        }
    }

    @VisibleForTesting
    public void setProjectPermissionTypesManager(ProjectPermissionTypesManager projectPermissionTypesManager)
    {
        this.projectPermissionTypesManager = projectPermissionTypesManager;
    }
}
