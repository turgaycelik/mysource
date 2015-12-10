package com.atlassian.jira.bc.projectroles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.role.ProjectRoleDeletedEvent;
import com.atlassian.jira.event.role.ProjectRoleUpdatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.type.ProjectRoleSecurityAndNotificationType;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.sharing.SharePermissionDeleteUtils;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.condition.InProjectRoleCondition;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of the ProjectRoleService
 */
public class DefaultProjectRoleService implements ProjectRoleService
{
    private static final Logger log = Logger.getLogger(DefaultProjectRoleService.class);
    private ProjectRoleManager projectRoleManager;
    private PermissionManager permissionManager;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private RoleActorFactory roleActorFactory;
    private NotificationSchemeManager notificationSchemeManager;
    private PermissionSchemeManager permissionSchemeManager;
    private WorkflowManager workflowManager;
    private ProjectManager projectManager;
    private SchemeFactory schemeFactory;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final SharePermissionDeleteUtils sharePermissionDeleteUtils;
    private IssueSecuritySchemeManager issueSecuritySchemeManager;
    private EventPublisher eventPublisher;

    public DefaultProjectRoleService(final ProjectRoleManager projectRoleManager, final PermissionManager permissionManager,
            final JiraAuthenticationContext jiraAuthenticationContext, final RoleActorFactory roleActorFactory,
            final NotificationSchemeManager notificationSchemeManager, final PermissionSchemeManager permissionSchemeManager,
            final WorkflowManager workflowManager, final ProjectManager projectManager, final SchemeFactory schemeFactory,
            final IssueSecurityLevelManager issueSecurityLevelManager, final SharePermissionDeleteUtils sharePermissionDeleteUtils,
            final IssueSecuritySchemeManager issueSecuritySchemeManager, final EventPublisher eventPublisher)
    {
        this.projectRoleManager = projectRoleManager;
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.roleActorFactory = roleActorFactory;
        this.notificationSchemeManager = notificationSchemeManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.workflowManager = workflowManager;
        this.projectManager = projectManager;
        this.schemeFactory = schemeFactory;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.sharePermissionDeleteUtils = sharePermissionDeleteUtils;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Collection<ProjectRole> getProjectRoles(User currentUser, ErrorCollection errorCollection)
    {
        return getProjectRoles(errorCollection);
    }

    @Override
    public ProjectRole getProjectRole(User currentUser, Long id, ErrorCollection errorCollection)
    {
        return getProjectRole(id, errorCollection);
    }

    @Override
    public ProjectRole getProjectRoleByName(User currentUser, String name, ErrorCollection errorCollection)
    {
        return getProjectRoleByName(name, errorCollection);
    }

    @Override
    public ProjectRole createProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return createProjectRole(ApplicationUsers.from(currentUser), projectRole, errorCollection);
    }

    private ProjectRole createProjectRole(ApplicationUser currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        ProjectRole createdProjectRole = null;
        boolean internalError = false;

        String roleName = null;
        if (projectRole == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null.create"));
            internalError = true;
        }
        else
        {
            roleName = projectRole.getName();
        }

        if (roleName == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.name.null.create"));
            internalError = true;
        }

        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }

        if (!isProjectRoleNameUnique(currentUser, roleName, errorCollection))
        {
            internalError = true;
        }

        if (!internalError)
        {
            createdProjectRole = projectRoleManager.createRole(projectRole);
        }
        return createdProjectRole;
    }

    @Override
    public boolean isProjectRoleNameUnique(User currentUser, String name, ErrorCollection errorCollection)
    {
        return isProjectRoleNameUnique(ApplicationUsers.from(currentUser), name, errorCollection);
    }

    private boolean isProjectRoleNameUnique(ApplicationUser currentUser, String name, ErrorCollection errorCollection)
    {
        boolean roleNameUnique = false;

        if (hasAdminPermission(currentUser))
        {
            roleNameUnique = projectRoleManager.isRoleNameUnique(name);
            if (!roleNameUnique)
            {
                errorCollection.addError("name", getText("admin.projectroles.duplicate.role.name.error", name));
            }
        }
        else
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
        return roleNameUnique;
    }

    @Override
    public void deleteProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        deleteProjectRole(ApplicationUsers.from(currentUser), projectRole, errorCollection);
    }

    private void deleteProjectRole(ApplicationUser currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        if (projectRole == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null.delete"));
            internalError = true;
        }

        if (!internalError && projectRole.getId() == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null.id.delete"));
            internalError = true;
        }


        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }

        if (!internalError)
        {
            // delete all the entries in the notification schemes that are a reference to this role
            try
            {
                notificationSchemeManager.removeEntities(PROJECTROLE_NOTIFICATION_TYPE, projectRole.getId().toString());
            }
            catch (RemoveException e)
            {
                log.error("Unable to remove notification scheme entites for project role: " + projectRole.getName());
            }
            // delete all the entries in the permission schemes that are a reference to this role
            try
            {
                permissionSchemeManager.removeEntities(PROJECTROLE_PERMISSION_TYPE, projectRole.getId().toString());
            }
            catch (RemoveException e)
            {
                log.error("Unable to remove permission scheme entites for project role: " + projectRole.getName());
            }

            try
            {
                issueSecuritySchemeManager.removeEntities(PROJECTROLE_ISSUE_SECURITY_TYPE, projectRole.getId().toString());
            }
            catch (RemoveException e)
            {
                log.error("Unable to remove issue security scheme entites for project role: " + projectRole.getName());
            }

            // clean up all SharePermissions for that role
            sharePermissionDeleteUtils.deleteRoleSharePermissions(projectRole.getId());

            projectRoleManager.deleteRole(projectRole);
            clearIssueSecurityLevelCache();
            eventPublisher.publish(new ProjectRoleDeletedEvent(projectRole));
        }
    }

    @Override
    public void updateProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        updateProjectRole(ApplicationUsers.from(currentUser), projectRole, errorCollection);
    }

    private void updateProjectRole(ApplicationUser currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        if (projectRole == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null.update"));
            internalError = true;
        }

        if (!internalError && projectRole.getId() == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null.id.update"));
            internalError = true;
        }

        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }

        if (!internalError)
        {
            // JRA-13157 - we need to let the update, update itself, sometimes users are changing the case of the
            // role name on a case-insensitive db so the name get will return a role, we just need to see if that role
            // is the one we are updating.
            ProjectRole roleByName = projectRoleManager.getProjectRole(projectRole.getName());
            if (roleByName != null && !roleByName.getId().equals(projectRole.getId()))
            {
                errorCollection.addErrorMessage(getText("admin.projectroles.duplicate.role.name.error", projectRole.getName()));
            }
            else
            {
                projectRoleManager.updateRole(projectRole);
            }
        }
    }

    @Override
    public ProjectRoleActors getProjectRoleActors(User currentUser, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
        return getProjectRoleActors(ApplicationUsers.from(currentUser), projectRole, project, errorCollection);
    }

    private ProjectRoleActors getProjectRoleActors(ApplicationUser currentUser, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        if (projectRole == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.actors.null.project.role"));
            internalError = true;
        }

        if (project == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.actors.null.project"));
            internalError = true;
        }

        ProjectRoleActors projectRoleActors = null;
        if (!internalError && hasProjectRolePermission(currentUser, project))
        {
            projectRoleActors = projectRoleManager.getProjectRoleActors(projectRole, project);
        }
        else
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.permission"));
        }
        return projectRoleActors;
    }

    @Override
    public void addActorsToProjectRole(User currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
        updateActorsToProjectRole(ApplicationUsers.from(currentUser), actors, projectRole, project, actorType, errorCollection, true, true);
    }

    @Override
    public void removeActorsFromProjectRole(User currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
        removeActorsFromProjectRole(ApplicationUsers.from(currentUser), actors, projectRole, project, actorType, errorCollection, true);
    }

    private void removeActorsFromProjectRole(ApplicationUser currentUser, Collection<String> actors,
            ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection, boolean triggerEvent)
    {
        if (hasProjectRolePermission(currentUser, project))
        {
            if (canRemoveCurrentUser(currentUser, actors, projectRole, project, actorType))
            {
                updateActorsToProjectRole(currentUser, actors, projectRole, project, actorType, errorCollection, false, triggerEvent);
            }
            else
            {
                errorCollection.addErrorMessage(getText("project.roles.service.error.removeself.actor"));
            }
        }
        else
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.no.permission.to.remove"));
        }
    }

    @Override
    public void setActorsForProjectRole(User currentUser, Map<String, Set<String>> newRoleActors, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
        setActorsForProjectRole(ApplicationUsers.from(currentUser), newRoleActors, projectRole, project, errorCollection);
    }

    private void setActorsForProjectRole(ApplicationUser currentUser, Map<String, Set<String>> newRoleActors, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
        // Get all the project role actors for the projectRole and project context
        ProjectRoleActors projectRoleActors = getProjectRoleActors(currentUser, projectRole, project, errorCollection);

        // Permissions may have changed and we may already have errored out. Don't confuse the user with
        // more error messages than needed.
        if(errorCollection.hasAnyErrors())
        {
            return;
        }

        // Validate that we found the project role actors
        if (projectRoleActors == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.actors.null"));
            // We do not want to allow anything to get further than this so return with errors
            return;
        }

        // Validate that we have a non-null set of new role actors
        if (newRoleActors == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.new.role.actors.null"));
            // We do not want to allow anything to get further than this so return with errors
            return;
        }

        final Set<RoleActor> roleActors = projectRoleActors.getRoleActors();

        // First get the existing role actors into the same shape our new role actors are coming in
        // so we can iterate over them nicely
        final Map<String, Set<String>> existingRoleActors = Maps.newHashMap();
        for (final RoleActor roleActor : roleActors)
        {
            final String actorType = roleActor.getType();
            final String actorName = roleActor.getParameter();
            Set<String> actorNames = existingRoleActors.get(actorType);
            if(actorNames == null)
            {
                actorNames = Sets.newHashSet();
            }
            actorNames.add(actorName);
            existingRoleActors.put(actorType, actorNames);
        }

        final Set<String> allActorTypes = CollectionBuilder.<String>newBuilder()
                .addAll(existingRoleActors.keySet())
                .addAll(newRoleActors.keySet())
                .asSet();

        final Map<String, Set<String>> toDelete = Maps.newHashMap(existingRoleActors);
        final Map<String, Set<String>> toAdd = Maps.newHashMap(newRoleActors);

        // Iterate over all actor types and determine, for each actor type what actors are added and deleted.
        // Replace a set if we are modifying it as the maps contain a reference
        //
        // {actorsToDelete} = {existingActors} - {newActors}
        // {actorsToAdd} = {newActors} - {existingActors}
        for (final String actorType : allActorTypes)
        {
            final Set<String> newActors = newRoleActors.get(actorType);
            final Set<String> actorsToDelete = toDelete.get(actorType);

            if(newActors != null && actorsToDelete != null)
            {
                final HashSet<String> actorsToDeleteCopy = Sets.newHashSet(actorsToDelete);
                actorsToDeleteCopy.removeAll(newActors);
                toDelete.put(actorType, actorsToDeleteCopy);
            }

            final Set<String> existingActors = existingRoleActors.get(actorType);
            final Set<String> actorsToAdd = toAdd.get(actorType);

            if(existingActors != null && actorsToAdd != null)
            {
                final HashSet<String> actorsToAddCopy = Sets.newHashSet(actorsToAdd);
                actorsToAddCopy.removeAll(existingActors);
                toAdd.put(actorType, actorsToAddCopy);
            }
        }

        // Finally, rely on existing service methods for doing the actual adding and removing. Use the same
        // errorCollection to keep errors arising from both operations.
        for (final String actorType : allActorTypes)
        {
            final Set<String> actorNamesToAdd = toAdd.get(actorType);
            if(actorNamesToAdd != null && actorNamesToAdd.size() > 0)
            {
                updateActorsToProjectRole(jiraAuthenticationContext.getUser(), actorNamesToAdd, projectRole,
                        project, actorType, errorCollection, true, false);
            }
            final Set<String> actorNamesToDelete = toDelete.get(actorType);
            if(actorNamesToDelete != null && actorNamesToDelete.size() > 0)
            {
                removeActorsFromProjectRole(jiraAuthenticationContext.getUser(), actorNamesToDelete, projectRole,
                        project, actorType, errorCollection, false);
            }
        }

        eventPublisher.publish(new ProjectRoleUpdatedEvent(project, projectRole,  projectRoleManager.getProjectRoleActors(projectRole, project), projectRoleActors));
    }

    /**
     * This method makes certain that the currentUser can not remove themselves from a project role if it means
     * that they will no longer be a roleMember by the action of the removal.
     * <p/>
     * Note this method is package-private for unit testing purposes.
     * @param currentUser the user making the call
     * @param actorType String representing the type of project role actor
     */
    boolean canRemoveCurrentUser(ApplicationUser currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType)
    {
        //JRA-12528: if we are a global admin, we're allowed to do anything.
        if (permissionManager.hasPermission(Permissions.ADMINISTER, currentUser))
        {
            return true;
        }
        // We only need to check if this is the last user reference in the project roles if the project role is in
        // use with the permission scheme in the "Administer Projects" permission.
        if (!doesProjectRoleExistForAdministerProjectsPermission(project, projectRole))
        {
            return true;
        }

        ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(projectRole, project);
        List<User> allUsers = new ArrayList<User>();
        int rolesRemovedWithMeInIt = 0;

        // Iterate through the current role actors
        for (RoleActor roleActorFromProjectRole : projectRoleActors.getRoleActors())
        {
            Collection<? extends User> roleActorFromProjectRoleUsers = roleActorFromProjectRole.getUsers();

            // Always store all the users that are referenced by all the actors currently contained in the db so
            // that we can find out how many times total the currentUser is included in the role.
            allUsers.addAll(roleActorFromProjectRoleUsers);

            // We want to keep track of the amount of times we run into an existing role actor that contains the current
            // user within the users Set that the actor represents
            if (roleActorsToRemoveContainsRoleActorFromProjectRole(roleActorFromProjectRole, actorType, currentUser, actors, projectRole, project))
            {
                rolesRemovedWithMeInIt++;
            }
        }

        // Now that we know how many times the currentUser is being removed via one of the roleActors in the actors
        // collection, we need to find out how many times, total, the currentUser is referenced in the conglomeration
        // of roleActor users
        int amountOfTimesIAmReferenced = getAmountOfTimesUsernameInList(allUsers, currentUser.getDirectoryUser());

        // We will only allow the user to delete himself if they are referenced more times then the amount of times
        // they are removed by the roles being removed.
        return amountOfTimesIAmReferenced > rolesRemovedWithMeInIt;
    }

    /**
     * Check if the Project Role is in "Administer Projects" for the permission scheme that is associated with
     * the Project.
     * <p/>
     * Note this method is package-private for unit testing purposes.
     */
    boolean doesProjectRoleExistForAdministerProjectsPermission(Project project, ProjectRole projectRole)
    {
        if (permissionSchemeManager == null)
        {
            throw new NullPointerException("Instance of " + PermissionSchemeManager.class.getName() + " required.");
        }
        if (schemeFactory == null)
        {
            throw new NullPointerException("Instance of " + SchemeFactory.class.getName() + " required.");
        }
        if (project == null)
        {
            throw new NullPointerException("Instance of " + Project.class.getName() + " required.");
        }
        if (projectRole == null)
        {
            throw new NullPointerException("Instance of " + ProjectRole.class.getName() + " required.");
        }

        // We need to get a hold of the permission scheme that is associated with the project we are looking at
        List<GenericValue> schemesGvs;
        try
        {
            schemesGvs = permissionSchemeManager.getSchemes(project.getGenericValue());
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        // For each of the schemes we are going to look at all the entities for the project admin permission
        for (GenericValue schemeGv : schemesGvs)
        {
            Scheme scheme = schemeFactory.getSchemeWithEntitiesComparable(schemeGv);
            List<SchemeEntity> entitiesForProjectAdmin = scheme.getEntitiesByType((long) Permissions.PROJECT_ADMIN);

            // This should be all the scheme entities for the project admin permission
            for (SchemeEntity schemeEntity : entitiesForProjectAdmin)
            {
                // We want to determine if the current project role is referenced in the scheme entities for the
                // project admin permission in the current permission scheme
                boolean schemeEntityIsForProjectRole = (schemeEntity.getParameter() != null) && projectRole.getId().toString().equals(schemeEntity.getParameter());
                boolean schemeEntityIsOfTypeProjectRole = ProjectRoleSecurityAndNotificationType.PROJECT_ROLE.equals(schemeEntity.getType());
                if (schemeEntityIsOfTypeProjectRole && schemeEntityIsForProjectRole)
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public DefaultRoleActors getDefaultRoleActors(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return getDefaultRoleActors(ApplicationUsers.from(currentUser), projectRole, errorCollection);
    }

    private DefaultRoleActors getDefaultRoleActors(ApplicationUser currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        if (projectRole == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.actors.null.project.role"));
            internalError = true;
        }

        DefaultRoleActors defaultRoleActors = null;
        if (!internalError && hasAdminPermission(currentUser))
        {
            defaultRoleActors = projectRoleManager.getDefaultRoleActors(projectRole);
        }
        else if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
        return defaultRoleActors;
    }

    @Override
    public void addDefaultActorsToProjectRole(User currentUser, Collection<String> actors, ProjectRole projectRole, String type, ErrorCollection errorCollection)
    {
        updateActorsToDefaultRole(ApplicationUsers.from(currentUser), actors, projectRole, type, errorCollection, true);
    }

    @Override
    public void removeDefaultActorsFromProjectRole(User currentUser, Collection<String> actors, ProjectRole projectRole, String actorType, ErrorCollection errorCollection)
    {
        updateActorsToDefaultRole(ApplicationUsers.from(currentUser), actors, projectRole, actorType, errorCollection, false);
    }

    @Override
    public void removeAllRoleActorsByNameAndType(User currentUser, String name, String type, ErrorCollection errorCollection)
    {
        removeAllRoleActorsByNameAndType(ApplicationUsers.from(currentUser), name, type, errorCollection);
    }

    private void removeAllRoleActorsByNameAndType(ApplicationUser currentUser, String name, String type, ErrorCollection errorCollection)
    {
        ErrorCollection errors = validateRemoveAllRoleActorsByNameAndType(currentUser, name, type);
        if (errors.hasAnyErrors()) {
            errorCollection.addErrorCollection(errors);
        } else {
            removeAllRoleActorsByNameAndType(name, type);
        }
    }

    @Override
    public ErrorCollection validateRemoveAllRoleActorsByNameAndType(final User currentUser, final String name, final String type/*, final ErrorCollection errorCollection*/)
    {
        return validateRemoveAllRoleActorsByNameAndType(ApplicationUsers.from(currentUser), name, type);
    }

    private ErrorCollection validateRemoveAllRoleActorsByNameAndType(final ApplicationUser currentUser, final String name, final String type)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        if (!TextUtils.stringSet(name))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.name.null.remove"));
        }

        if (!TextUtils.stringSet(type))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.type.null.remove"));
        }

        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
        return errorCollection;
    }

    @Override
    public void removeAllRoleActorsByNameAndType(final String name, final String type)
    {
        projectRoleManager.removeAllRoleActorsByNameAndType(name, type);
    }

    @Override
    public void removeAllRoleActorsByProject(User currentUser, Project project, ErrorCollection errorCollection)
    {
        removeAllRoleActorsByProject(ApplicationUsers.from(currentUser), project, errorCollection);
    }

    private void removeAllRoleActorsByProject(ApplicationUser currentUser, Project project, ErrorCollection errorCollection)
    {
        boolean internalError = false;
        if (project == null || project.getId() == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.null"));
            internalError = true;
        }
        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }

        if (!internalError)
        {
            projectRoleManager.removeAllRoleActorsByProject(project);
        }
    }

    @Override
    public Collection getAssociatedNotificationSchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return getAssociatedNotificationSchemes(projectRole, errorCollection);
    }

    @Override
    public Collection<GenericValue> getAssociatedIssueSecuritySchemes(ProjectRole projectRole, ErrorCollection errorCollection)
    {
        boolean internalError = false;
        Collection<GenericValue> schemes = new ArrayList<GenericValue>();

        if (projectRole == null || projectRole.getId() == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null"));
            internalError = true;
        }

        if (!internalError)
        {
            schemes = issueSecuritySchemeManager.getSchemesContainingEntity(PROJECTROLE_ISSUE_SECURITY_TYPE, projectRole.getId().toString());
        }
        return schemes;
    }

    @Override
    public Collection getAssociatedIssueSecuritySchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return getAssociatedIssueSecuritySchemes(projectRole, errorCollection);
    }

    @Override
    public Collection getAssociatedPermissionSchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return getAssociatedPermissionSchemes(projectRole, errorCollection);
    }

    @Override
    public MultiMap getAssociatedWorkflows(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return getAssociatedWorkflows(projectRole, errorCollection);
    }

    @Override
    public Collection<Project> getProjectsContainingRoleActorByNameAndType(User currentUser, String name, String type, ErrorCollection errorCollection)
    {
        return  getProjectsContainingRoleActorByNameAndType(ApplicationUsers.from(currentUser), name, type, errorCollection);
    }

    private Collection<Project> getProjectsContainingRoleActorByNameAndType(ApplicationUser currentUser, String name, String type, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        if (!TextUtils.stringSet(name))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.name.null.remove"));
            internalError = true;
        }

        if (!TextUtils.stringSet(type))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.type.null.remove"));
            internalError = true;
        }

        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }
        if (internalError)
        {
            return Collections.emptyList();
        }

        Collection<Long> projectIds = projectRoleManager.getProjectIdsContainingRoleActorByNameAndType(name, type);
        if (projectIds == null)
        {
            return Collections.emptyList();
        }
        return projectManager.convertToProjectObjects(projectIds);
    }

    @Override
    public List<Long> roleActorOfTypeExistsForProjects(User currentUser, List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter, ErrorCollection errorCollection)
    {
        return roleActorOfTypeExistsForProjects(ApplicationUsers.from(currentUser), projectsToLimitBy, projectRole, projectRoleType, projectRoleParameter, errorCollection);
    }

    private List<Long> roleActorOfTypeExistsForProjects(ApplicationUser currentUser, List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter, ErrorCollection errorCollection)
    {
        List<Long> projectsRoleActorExistsFor = new ArrayList<Long>();
        boolean internalError = false;

        if (projectsToLimitBy == null || projectsToLimitBy.isEmpty())
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.projects.to.limit.needed"));
            internalError = true;
        }

        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }

        if (!internalError)
        {
            projectsRoleActorExistsFor.addAll(projectRoleManager.roleActorOfTypeExistsForProjects(projectsToLimitBy, projectRole, projectRoleType, projectRoleParameter));
        }
        return projectsRoleActorExistsFor;
    }

    @Override
    public Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(User currentUser, List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName, ErrorCollection errorCollection)
    {
        return getProjectIdsForUserInGroupsBecauseOfRole(ApplicationUsers.from(currentUser), projectsToLimitBy, projectRole, projectRoleType, userName, errorCollection);
    }

    private Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(ApplicationUser currentUser, List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }

        if (!internalError)
        {
            return projectRoleManager.getProjectIdsForUserInGroupsBecauseOfRole(projectsToLimitBy, projectRole, projectRoleType, userName);
        }
        return new HashMap<Long, List<String>>();
    }

    /**
     * Recursive depth-first search of ConditionsDescriptor tree for Conditions
     * that have an argument name that matches
     * {@link InProjectRoleCondition#KEY_PROJECT_ROLE_ID}. See
     * {@link ConditionsDescriptor} and {@link ConditionDescriptor}
     * (beware the Captain Insano Class naming technique).
     *
     * @param conditionsDescriptor the tree root to search in this invocation.
     * @param projectRoleId the role to search for
     * @return true only if the conditions descriptor tree refers to a matching Condition
     */
    private boolean conditionsDescriptorContainsProjectRoleCondition(ConditionsDescriptor conditionsDescriptor, Long projectRoleId)
    {
        for (Object o : conditionsDescriptor.getConditions())
        {
            if (o instanceof ConditionsDescriptor)
            {
                // recursive step
                if (conditionsDescriptorContainsProjectRoleCondition((ConditionsDescriptor) o, projectRoleId))
                {
                    // Found!
                    return true;
                }
            }
            else
            {
                // leaf
                ConditionDescriptor conditionDescriptor = (ConditionDescriptor) o;
                Map args = conditionDescriptor.getArgs();
                String foundProjectRoleId = (String) args.get(InProjectRoleCondition.KEY_PROJECT_ROLE_ID);
                if (foundProjectRoleId != null && foundProjectRoleId.equals(projectRoleId.toString()))
                {
                    // Found!
                    return true;
                }
            }
        }
        // Not Found :(
        return false;
    }

    private void updateActorsToProjectRole(ApplicationUser currentUser, Collection<String> actorKeys, ProjectRole projectRole,
            Project project, String actorType, ErrorCollection errorCollection, boolean add, boolean triggerEvent)
    {
        // Get all the project role actors for the projectRole and project context
        final ProjectRoleActors originalRoleActors = getProjectRoleActors(currentUser, projectRole, project, errorCollection);

        // Validate that we found the project role actors
        if (originalRoleActors == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.actors.null"));
            // We do not want to allow anything to get further than this so return with errors
            return;
        }

        List<RoleActor> actors = new ArrayList<RoleActor>();
        // Turn the actor name strings into a collection of RoleActor objects, populating them into the actors list
        boolean internalError = createRoleActors(actorKeys, projectRole, project, actorType, originalRoleActors, errorCollection, actors, add);

        // If we have not run into an error in this method and the current user has permission to perform this operation,
        // then lets do it!
        final Project projectRoleProject = projectManager.getProjectObj(originalRoleActors.getProjectId());
        final boolean hasPermission = hasProjectRolePermission(currentUser, projectRoleProject);
        if (!internalError && hasPermission && actors.size() > 0)
        {
            // adding or removing the role actors
            final ProjectRoleActors newRoleActors;
            if (add)
            {
                newRoleActors = (ProjectRoleActors) originalRoleActors.addRoleActors(actors);
            }
            else
            {
                newRoleActors = (ProjectRoleActors) originalRoleActors.removeRoleActors(actors);
            }
            projectRoleManager.updateProjectRoleActors(newRoleActors);
            clearIssueSecurityLevelCache();

            if (triggerEvent)
            {
                eventPublisher.publish(new ProjectRoleUpdatedEvent(projectRoleProject, projectRole, newRoleActors, originalRoleActors));
            }
        }
        else if (!hasPermission)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.permission"));
        }
    }

    private boolean validateAllUsersAreActive(List<RoleActor> roleActors, ErrorCollection errorCollection)
    {
        boolean result = true;
        for (RoleActor actor : roleActors)
        {
            for (User user : actor.getUsers())
            {
                if (!user.isActive())
                {
                    errorCollection.addErrorMessage("User '" + user.getName() + "' does not exist.");
                    result = false;
                }
            }
        }
        return result;
    }

    // A user will have permission to update if they are a JIRA admin or, if in enterprise, the user is a project admin
    @Override
    public boolean hasProjectRolePermission(User currentUser, Project project)
    {
        return hasProjectRolePermission(ApplicationUsers.from(currentUser), project);
    }

    private boolean hasProjectRolePermission(ApplicationUser currentUser, Project project)
    {
        return hasAdminPermission(currentUser) || hasProjectAdminPermission(currentUser, project);
    }

    private boolean createRoleActors(Collection<String> actors, ProjectRole projectRole, Project project,
            String actorType, DefaultRoleActors roleActors, ErrorCollection errorCollection, List<RoleActor> actorsTo,
            boolean add)
    {
        boolean internalError = false;
        // Run through the actor names
        for (String actorKey : actors)
        {
            ProjectRoleActor projectRoleActor;
            try
            {
                // create a role actor for the provided type, this can thrown an IllegalArgumentException if the
                // roleActor class is not able to resolve the actorName into something it understands

                final Long projectId = (project != null) ? project.getId() : null;
                final Long projectRoleId = (projectRole != null) ? projectRole.getId() : null;
                projectRoleActor = roleActorFactory.createRoleActor(null, projectRoleId, projectId, actorType, actorKey);
                // We should only do a contains validation if we are adding, if we are removing then who cares?
                if (add)
                {
                    if (roleActors.getRoleActors().contains(projectRoleActor))
                    {
                        String actorIdForErrorMessage = actorKey;
                        if ("atlassian-user-role-actor".equals(projectRoleActor.getType()))
                        {
                            Iterator<User> actorUserIterator = projectRoleActor.getUsers().iterator();
                            if (actorUserIterator.hasNext())
                            {
                                User actorUser = actorUserIterator.next();
                                if (actorUser != null) { actorIdForErrorMessage = actorUser.getName(); }
                            }
                        }
                        errorCollection.addErrorMessage(getText("admin.user.role.actor.action.error.exists", actorIdForErrorMessage));
                        internalError = true;
                    }
                    else if (!projectRoleActor.isActive())
                    {
                        errorCollection.addErrorMessage
                                (
                                        jiraAuthenticationContext.getI18nHelper().getText
                                                (
                                                        "admin.user.role.actor.action.error.inactive",
                                                        projectRoleActor.getParameter(),
                                                        String.valueOf(projectRoleActor.getProjectRoleId())
                                                )
                                );
                        internalError = true;
                    }
                    else
                    {
                        actorsTo.add(projectRoleActor);
                    }
                }
                else
                {
                    actorsTo.add(projectRoleActor);
                }
            }
            catch (RoleActorDoesNotExistException ex)
            {
                errorCollection.addErrorMessage(getText("admin.user.role.actor.action.error.invalid", actorKey));
                internalError = true;
            }
        }
        return internalError;
    }

    private String getText(String key)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key);
    }

    private String getText(String key, String param)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key, param);
    }

    private boolean hasProjectAdminPermission(ApplicationUser currentUser, Project project)
    {
        return permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, currentUser);
    }

    private boolean hasAdminPermission(ApplicationUser currentUser)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, currentUser);
    }

    private void updateActorsToDefaultRole(ApplicationUser currentUser, Collection<String> actorNames, ProjectRole projectRole, String actorType, ErrorCollection errorCollection, boolean add)
    {
        // Get all the project role actors for the projectRole and project context
        DefaultRoleActors defaultRoleActors = getDefaultRoleActors(currentUser, projectRole, errorCollection);

        // Validate that we found the project role actors
        if (defaultRoleActors == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.actors.null"));
            // We do not want to allow anything to get further than this so return with errors
            return;
        }

        List<RoleActor> actors = new ArrayList<RoleActor>();
        // Turn the actor name strings into a collection of RoleActor objects, populating them into the actors list
        boolean internalError = createRoleActors(actorNames, projectRole, null, actorType, defaultRoleActors, errorCollection, actors, add);

        // If we have not run into an error in this method and the current user has permission to perform this operation,
        // then lets do it!
        final boolean hasAdminPermission = hasAdminPermission(currentUser);
        if (!internalError && hasAdminPermission && actors.size() > 0)
        {
            // adding or removing the role actors
            if (add)
            {
                defaultRoleActors = defaultRoleActors.addRoleActors(actors);
            }
            else
            {
                defaultRoleActors = defaultRoleActors.removeRoleActors(actors);
            }
            projectRoleManager.updateDefaultRoleActors(defaultRoleActors);
        }
        else if (!hasAdminPermission)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
    }

    private int getAmountOfTimesUsernameInList(List<User> allUsers, User currentUser)
    {
        int numberOfTimesIAmReferenced = 0;
        for (User user : allUsers)
        {
            if(user.getName().equals(currentUser.getName()))
            {
                numberOfTimesIAmReferenced++;
            }
        }
        return numberOfTimesIAmReferenced;
    }

    private boolean roleActorsToRemoveContainsRoleActorFromProjectRole(RoleActor roleActorFromProjectRole, String actorType, ApplicationUser currentUser, Collection<String> actors, ProjectRole projectRole, Project project)
    {
        // We want to keep track of the amount of times we run into an existing role actor that contains the current
        // user within the users Set that the actor represents
        if (roleActorFromProjectRole.getType().equals(actorType) && roleActorFromProjectRole.contains(currentUser))
        {
            for (String actorKey : actors)
            {
                // Create a roleActor object from the actorName and type so we can compare the equality of the
                // actor to be removed with the roleActorFromProjectRole
                RoleActor roleActorToRemove;
                try
                {
                    roleActorToRemove = roleActorFactory.createRoleActor(null, projectRole.getId(), project.getId(), actorType, actorKey);
                }
                catch (RoleActorDoesNotExistException e)
                {
                    throw new IllegalArgumentException("Unexpected error: the role actor '" + actorKey + "' of type '" + actorType + "' does not exist.");
                }

                // If the role to be removed is the same as the role we are looking at then we keep a count of
                // it since this is a role that is being removed and the role includes the current user.
                if (roleActorToRemove.equals(roleActorFromProjectRole))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void clearIssueSecurityLevelCache()
    {
        try
        {
            if (issueSecurityLevelManager != null)
            {
                issueSecurityLevelManager.clearUsersLevels();
            }
        }
        catch (UnsupportedOperationException uoe)
        {
            log.debug("Unsupported operation was thrown when trying to clear the issue security level manager cache", uoe);
        }
    }

    @Override
    public Collection<ProjectRole> getProjectRoles(ErrorCollection errorCollection)
    {
        return projectRoleManager.getProjectRoles();

    }

    @Override
    public ProjectRole getProjectRole(Long id, ErrorCollection errorCollection)
    {
        ProjectRole projectRole = null;
        if (id == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.id.null"));
        }
        else
        {
            projectRole = projectRoleManager.getProjectRole(id);
        }
        return projectRole;
    }

    @Override
    public ProjectRole getProjectRoleByName(String name, ErrorCollection errorCollection)
    {
        ProjectRole projectRole = null;
        if (StringUtils.isBlank(name))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.name.null"));
        }
        else
        {
            projectRole = projectRoleManager.getProjectRole(name);
        }
        return projectRole;
    }

    @Override
    public ProjectRole createProjectRole(ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return createProjectRole(jiraAuthenticationContext.getUser(), projectRole, errorCollection);
    }

    @Override
    public boolean isProjectRoleNameUnique(String name, ErrorCollection errorCollection)
    {
        return isProjectRoleNameUnique(jiraAuthenticationContext.getUser(), name, errorCollection);
    }

    @Override
    public void deleteProjectRole(ProjectRole projectRole, ErrorCollection errorCollection)
    {
        deleteProjectRole(jiraAuthenticationContext.getUser(), projectRole, errorCollection);
    }

    @Override
    public void updateProjectRole(ProjectRole projectRole, ErrorCollection errorCollection)
    {
        updateProjectRole(jiraAuthenticationContext.getUser(), projectRole, errorCollection);
    }

    @Override
    public ProjectRoleActors getProjectRoleActors(ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
        return getProjectRoleActors(jiraAuthenticationContext.getUser() , projectRole, project, errorCollection);
    }

    @Override
    public DefaultRoleActors getDefaultRoleActors(ProjectRole projectRole, ErrorCollection errorCollection)
    {
        return getDefaultRoleActors(jiraAuthenticationContext.getUser(), projectRole, errorCollection);
    }

    @Override
    public void addActorsToProjectRole(Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
        updateActorsToProjectRole(jiraAuthenticationContext.getUser(), actors, projectRole, project, actorType, errorCollection, true, true);
    }

    @Override
    public void removeActorsFromProjectRole(Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
        removeActorsFromProjectRole(jiraAuthenticationContext.getUser(), actors, projectRole, project, actorType, errorCollection, true);
    }

    @Override
    public void setActorsForProjectRole(Map<String, Set<String>> newRoleActors, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
        setActorsForProjectRole(jiraAuthenticationContext.getUser(), newRoleActors, projectRole, project, errorCollection);
    }

    @Override
    public void addDefaultActorsToProjectRole(Collection<String> actors, ProjectRole projectRole, String type, ErrorCollection errorCollection)
    {
        updateActorsToDefaultRole(jiraAuthenticationContext.getUser(), actors, projectRole, type, errorCollection, true);
    }

    @Override
    public void removeDefaultActorsFromProjectRole(Collection<String> actors, ProjectRole projectRole, String actorType, ErrorCollection errorCollection)
    {
        updateActorsToDefaultRole(jiraAuthenticationContext.getUser(), actors, projectRole, actorType, errorCollection, false);
    }

    @Override
    public ErrorCollection validateRemoveAllRoleActorsByNameAndType(final String name, final String type)
    {
        return validateRemoveAllRoleActorsByNameAndType(jiraAuthenticationContext.getUser(), name, type);
    }

    @Override
    public void removeAllRoleActorsByNameAndType(String name, String type, ErrorCollection errorCollection)
    {
        removeAllRoleActorsByNameAndType(jiraAuthenticationContext.getUser(), name, type, errorCollection);
    }

    @Override
    public void removeAllRoleActorsByProject(Project project, ErrorCollection errorCollection)
    {
        removeAllRoleActorsByProject(jiraAuthenticationContext.getUser(), project, errorCollection);
    }

    @Override
    public Collection getAssociatedNotificationSchemes(ProjectRole projectRole, ErrorCollection errorCollection)
    {
        boolean internalError = false;
        Collection schemes = new ArrayList();

        if (projectRole == null || projectRole.getId() == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null"));
            internalError = true;
        }

        if (!internalError)
        {
            schemes = notificationSchemeManager.getSchemesContainingEntity(PROJECTROLE_NOTIFICATION_TYPE, projectRole.getId().toString());
        }
        return schemes;
    }

    @Override
    public Collection<GenericValue> getAssociatedPermissionSchemes(ProjectRole projectRole, ErrorCollection errorCollection)
    {
        boolean internalError = false;
        Collection<GenericValue> schemes = new ArrayList<GenericValue>();

        if (projectRole == null || projectRole.getId() == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null"));
            internalError = true;
        }

        if (!internalError)
        {
            schemes = permissionSchemeManager.getSchemesContainingEntity(PROJECTROLE_PERMISSION_TYPE, projectRole.getId().toString());
        }
        return schemes;
    }

    @Override
    public MultiMap getAssociatedWorkflows(ProjectRole projectRole, ErrorCollection errorCollection)
    {

        Collection<JiraWorkflow> workflows = workflowManager.getWorkflows();
        MultiMap associatedWorkflows = new MultiHashMap(workflows.size());
        for (JiraWorkflow jiraWorkflow : workflows)
        {
            Collection<ActionDescriptor> actions = jiraWorkflow.getAllActions();
            for (ActionDescriptor actionDescriptor : actions)
            {
                RestrictionDescriptor restriction = actionDescriptor.getRestriction();
                if (restriction != null)
                {
                    ConditionsDescriptor conditionsDescriptor = restriction.getConditionsDescriptor();
                    if (conditionsDescriptorContainsProjectRoleCondition(conditionsDescriptor, projectRole.getId()))
                    {
                        associatedWorkflows.put(jiraWorkflow, actionDescriptor);
                        // workflow matches, don't need to check the conditions on any more actions
                    }
                }
            }

        }
        return associatedWorkflows;
    }

    @Override
    public Collection<Project> getProjectsContainingRoleActorByNameAndType(String name, String type, ErrorCollection errorCollection)
    {
        return getProjectsContainingRoleActorByNameAndType(jiraAuthenticationContext.getUser(), name, type, errorCollection);
    }

    @Override
    public List<Long> roleActorOfTypeExistsForProjects(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter, ErrorCollection errorCollection)
    {
        return roleActorOfTypeExistsForProjects(jiraAuthenticationContext.getUser(), projectsToLimitBy, projectRole, projectRoleType, projectRoleParameter, errorCollection);
    }

    @Override
    public Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName, ErrorCollection errorCollection)
    {
        return getProjectIdsForUserInGroupsBecauseOfRole(jiraAuthenticationContext.getUser(), projectsToLimitBy, projectRole, projectRoleType, userName, errorCollection);
    }

    // A user will have permission to update if they are a JIRA admin or, if in enterprise, the user is a project admin
    @Override
    public boolean hasProjectRolePermission(Project project)
    {
        return hasProjectRolePermission(jiraAuthenticationContext.getUser(), project);
    }
}
