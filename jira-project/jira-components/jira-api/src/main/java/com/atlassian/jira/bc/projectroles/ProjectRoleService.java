package com.atlassian.jira.bc.projectroles;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.util.ErrorCollection;
import org.apache.commons.collections.MultiMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the business layer component that must be used to access all ProjectRole functionality. This will
 * perform validation before it hands off to the project role manager. Operations will not be performed if
 * validation fails.
 */
@PublicApi
public interface ProjectRoleService
{
    public static final String PROJECTROLE_ISSUE_SECURITY_TYPE = "projectrole";
    public static final String PROJECTROLE_PERMISSION_TYPE = "projectrole";
    public static final String PROJECTROLE_NOTIFICATION_TYPE = "Project_Role";

    public Collection<ProjectRole> getProjectRoles(ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #getProjectRoles(com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Get all the ProjectRoles available in JIRA. Currently this list is global.
     * @return  The global list of project roles in JIRA
     */
    public Collection<ProjectRole> getProjectRoles(User currentUser, ErrorCollection errorCollection);

    public ProjectRole getProjectRole(Long id, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #getProjectRole(Long, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Will return the project role with the given <code>id</code>, and checking the <code>currentUser</code>
     * has the correct permissions to perform the operation.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     * @param currentUser the user performing the call
     * @param id the id of the ProjectRole
     * @param errorCollection will contain any errors in calling the method
     * @return the ProjectRole for the given id, will return null if no role found
     */
    public ProjectRole getProjectRole(User currentUser, Long id, ErrorCollection errorCollection);

    public ProjectRole getProjectRoleByName(String name, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #getProjectRoleByName(String, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     * Will return the project role with the given <code>name</code>, and checking the <code>currentUser</code>
     * has the correct permissions to perform the operation.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     * @param currentUser the user performing the call
     * @param name the name of the project role to return
     * @param errorCollection will contain any errors in calling the method
     * @return the ProjectRole for the given name, will return null if no role found
     */
    public ProjectRole getProjectRoleByName(User currentUser, String name, ErrorCollection errorCollection);

    public ProjectRole createProjectRole(ProjectRole projectRole, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #createProjectRole(com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Will create the project role with the given <code>projectRole.getName()</code>,
     * <code>projectRole.getDescription()</code> and checking the <code>currentUser</code> has the correct permissions
     * to perform the create operation. The passed in <code>errorCollection</code> will contain any errors that are
     * generated, such as permission violations.
     * @param currentUser the user performing the call
     * @param projectRole can not be null and will contain the name and description for the project role to create
     * @param errorCollection will contain any errors in calling the method
     * @return the ProjectRole object that was created
     */
    public ProjectRole createProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection);

    public boolean isProjectRoleNameUnique(String name, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #isProjectRoleNameUnique(String, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Will tell you if a role name exists or not.
     * @param name the name of the project role to check
     * @return true if unique, false if one already exists with that name
     */
    public boolean isProjectRoleNameUnique(User currentUser, String name, ErrorCollection errorCollection);

    public void deleteProjectRole(ProjectRole projectRole, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #deleteProjectRole(com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Will delete the given <code>projectRole</code> and checks
     * the <code>currentUser</code> has the correct permissions to perform the delete operation.
     * This will also delete all ProjectRoleActor associations that it is the parent of.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     * @param currentUser the user performing the call
     * @param projectRole
     * @param errorCollection will contain any errors in calling the method
     */
    public void deleteProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection);

    public void addActorsToProjectRole(Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #addActorsToProjectRole(java.util.Collection, com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.project.Project, String, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Will add project role actor associations for the given <code>actors</code> and checking
     * the <code>currentUser</code> has the correct permissions to perform the update operation.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     * @param currentUser the user performing the call
     * @param actors is a list of strings (e.g. user keys or group names) that the RoleActor impl should be able to handle
     * @param projectRole is the project role to associate with
     * @param project is the project to associate with
     * @param actorType is a type that defines the type of role actor to instantiate (ex./ UserRoleActor.TYPE, GroupRoleActor.TYPE)
     * @param errorCollection will contain any errors in calling the method
     */
    public void addActorsToProjectRole(User currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection);

    public void removeActorsFromProjectRole(Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #removeActorsFromProjectRole(java.util.Collection, com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.project.Project, String, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Will remove project role actor associations for the given <code>actors</code> and checking
     * the <code>currentUser</code> has the correct permissions to perform the update operation.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     * @param currentUser the user performing the call
     * @param actors is a list of strings (e.g. user keys or group names) that the RoleActor impl should be able to handle
     * @param projectRole is the project role to associate with
     * @param project is the project to associate with
     * @param actorType is a type that defines the type of role actor to instantiate (ex./ UserRoleActor.TYPE, GroupRoleActor.TYPE)
     * @param errorCollection will contain any errors in calling the method
     */
    public void removeActorsFromProjectRole(User currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection);

    public void setActorsForProjectRole(Map<String, Set<String>> newRoleActors, ProjectRole projectRole, Project project, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #setActorsForProjectRole(java.util.Map, com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.project.Project, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Will set the project role actor associations for the given <code>newRoleActors</code> and checking
     * the <code>currentUser</code> has the correct permissions to perform the update operation.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     * @param currentUser the user performing the call
     * @param newRoleActors is a mapping of actor types to actor parameters that will be set for the project role
     * @param projectRole is the project role to associate with
     * @param project is the project to associate with
     * @param errorCollection will contain any errors in calling the method
     */
    public void setActorsForProjectRole(User currentUser, Map<String, Set<String>> newRoleActors, ProjectRole projectRole, Project project, ErrorCollection errorCollection);

    public void updateProjectRole(ProjectRole projectRole, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #updateProjectRole(com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     * Will update <code>projectRole</code>, checking
     * the <code>currentUser</code> has the correct permissions to perform the update operation.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     * @param currentUser the user performing the call
     * @param projectRole
     * @param errorCollection will contain any errors in calling the method
     */
    public void updateProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection);

    public ProjectRoleActors getProjectRoleActors(ProjectRole projectRole, Project project, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #getProjectRoleActors(com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.project.Project, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Will return a {@link ProjectRoleActors) for the given <code>projectRole</code> and <code>project</code> checking
     * the <code>currentUser</code> has the correct permissions to perform the get operation.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     * @param currentUser the user performing the call
     * @param projectRole
     * @param project
     * @param errorCollection will contain any errors in calling the method
     * @return the ProjectRoleActor representing the projectRole and project
     */
    public ProjectRoleActors getProjectRoleActors(User currentUser, ProjectRole projectRole, Project project, ErrorCollection errorCollection);

    public DefaultRoleActors getDefaultRoleActors(ProjectRole projectRole, ErrorCollection collection);

    /**
     * @deprecated Use {@link #getProjectRoleActors(com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.project.Project, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Will return a {@link DefaultRoleActors} for the given <code>projectRole</code> checking the <code>currentUser</code>
     * has the correct permissions to perform the get operation.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     * @param currentUser the user performing the call
     * @param projectRole
     * @param collection
     * @return the default role actors
     */
    public DefaultRoleActors getDefaultRoleActors(User currentUser, ProjectRole projectRole, ErrorCollection collection);

    public void addDefaultActorsToProjectRole(Collection<String> actors, ProjectRole projectRole, String type, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #addActorsToProjectRole(java.util.Collection, com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.project.Project, String, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Will add default role actor associations based off the passed in <code>actors</code> and checking
     * the <code>currentUser</code> has the correct permissions to perform the update operation.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     * @param currentUser the user performing the call
     * @param actors is a list of strings (e.g. user keys or group names) that the RoleActor impl should be able to handle
     * @param projectRole is the project role to associate with
     * @param type is a type that defines the type of role actor to instantiate (ex./ UserRoleActor.TYPE, GroupRoleActor.TYPE)
     * @param errorCollection will contain any errors in calling the method
     */
    public void addDefaultActorsToProjectRole(User currentUser, Collection<String> actors, ProjectRole projectRole, String type, ErrorCollection errorCollection);

    public void removeDefaultActorsFromProjectRole(Collection<String> actors, ProjectRole projectRole, String actorType, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #removeDefaultActorsFromProjectRole(java.util.Collection, com.atlassian.jira.security.roles.ProjectRole, String, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Will remove default actor associations based off the passed in <code>actors</code>, <code>projectRole</code> and
     * <code>actorType</code> and checking the <code>currentUser</code> has the correct permissions to perform the update operation.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     * @param currentUser the user performing the call
     * @param actors is a list of strings (e.g. user keys or group names) that the RoleActor impl should be able to handle
     * @param projectRole is the project role to associate with
     * @param actorType is a type that defines the type of role actor to instantiate (ex./ UserRoleActor.TYPE, GroupRoleActor.TYPE)
     * @param errorCollection will contain any errors in calling the method
     */
    public void removeDefaultActorsFromProjectRole(User currentUser, Collection<String> actors, ProjectRole projectRole, String actorType, ErrorCollection errorCollection);

    public void removeAllRoleActorsByNameAndType(String name, String type, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #removeAllRoleActorsByNameAndType(String, String, ErrorCollection)} instead. Since v6.0.
     *
     * Will remove all role actors with the specified name and the specified type. This method should be used
     * to clean up after the actual subject of the role actor has been deleted (ex. deleting a user from the system).
     * @param currentUser the user performing the call
     * @param name this is the name that the role actor is stored under (ex. username of 'admin', group name of
     * 'jira-users')
     * @param type this is the role type parameter, (ex. GroupRoleActor.TYPE, UserRoleActor.TYPE)
     * @param errorCollection will contain any errors in calling the method
     * @depecated use pair of validateRemoveAllRoleActorsByNameAndType and removeAllRoleActorsByProject instead
     */
    public void removeAllRoleActorsByNameAndType(User currentUser, String name, String type, ErrorCollection errorCollection);

    public ErrorCollection validateRemoveAllRoleActorsByNameAndType(String name, String type);

    /**
     * @deprecated Use {@link #removeAllRoleActorsByNameAndType(String, String)} instead. Since v6.0.
     *
     * Will validate removing all role actors with the specified name and the specified type. This method should be used
     * before clean up after the actual subject of the role actor has been deleted (ex. deleting a user from the system).
     * Validation error wil be reported when name does not exists, type does not exists or user performing validation
     * does not have administrative rights
     * @param currentUser the user performing the call
     * @param name this is the name that the role actor is stored under (ex. username of 'admin', group name of
     * 'jira-users')
     * @param type this is the role type parameter, (ex. GroupRoleActor.TYPE, UserRoleActor.TYPE)
     * @return errorCollection will contain any errors in calling the method
     */
    public ErrorCollection validateRemoveAllRoleActorsByNameAndType(User currentUser, String name, String type);

    /**
     * @deprecated Use {@link #removeAllRoleActorsByNameAndType(String, String, com.atlassian.jira.util.ErrorCollection)} (String, String)} instead. Since v6.0.
     *
     * Will remove all role actors with the specified name and the specified type. This method should be used
     * to clean up after the actual subject of the role actor has been deleted (ex. deleting a user from the system).
     *
     * @param name this is the name that the role actor is stored under (ex. username of 'admin', group name of
     * 'jira-users')
     * @param type this is the role type parameter, (ex. GroupRoleActor.TYPE, UserRoleActor.TYPE)
     * @return errorCollection will contain any errors in calling the method
     */
    public void removeAllRoleActorsByNameAndType(String name, String type);

    public void removeAllRoleActorsByProject(Project project, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #removeAllRoleActorsByProject(com.atlassian.jira.project.Project, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     * Will remove all role actors associated with the specified project. This method should be used
     * to clean up just before the actual project has been deleted (ex. deleting a project from the system).
     * @param currentUser the user performing the call
     * @param project
     * @param errorCollection will contain any errors in calling the method
     */
    public void removeAllRoleActorsByProject(User currentUser, Project project, ErrorCollection errorCollection);

    public Collection getAssociatedNotificationSchemes(ProjectRole projectRole, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #getAssociatedNotificationSchemes(com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Will get all notification scheme's that the specified projectRole is currently used in.
     * @param currentUser the user performing the call
     * @param projectRole
     * @param errorCollection will contain any errors in calling the method
     * @return a collection of schemes, if no schemes are found this will be an empty collection.
     */
    public Collection getAssociatedNotificationSchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection);

    public Collection getAssociatedPermissionSchemes(ProjectRole projectRole, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #getAssociatedPermissionSchemes(com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     * Will get all permission scheme's that the specified projectRole is currently used in.
     * @param currentUser the user performing the call
     * @param projectRole
     * @param errorCollection will contain any errors in calling the method
     * @return a collection of schemes, if no schemes are found this will be an empty collection.
     */
    public Collection getAssociatedPermissionSchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection);

    public Collection getAssociatedIssueSecuritySchemes(ProjectRole projectRole, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #getAssociatedIssueSecuritySchemes(com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Will get all issue security scheme's that the specified projectRole is currently used in.
     * @param currentUser the user performing the call
     * @param projectRole
     * @param errorCollection will contain any errors in calling the method
     * @return a collection of schemes, if no schemes are found this will be an empty collection.
     */
    public Collection getAssociatedIssueSecuritySchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection);

    public MultiMap getAssociatedWorkflows(ProjectRole projectRole, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #getAssociatedWorkflows(com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Returns workflows and their actions that are associated with the given
     * {@link ProjectRole}. e.g. com.atlassian.jira.workflow.condition.InProjectRoleCondition
     * workflow elements that block workflow transition unless the acting user is
     * in the ProjectRole.
     * @param currentUser the acting user.
     * @param projectRole the project role.
     * @param errorCollection will contain any errors in calling the method
     * @return a possibly empty MultiMap of Workflow objects to List of Actions.
     */
    public MultiMap getAssociatedWorkflows(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection);

    public Collection<Project> getProjectsContainingRoleActorByNameAndType(String name, String type, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #getProjectsContainingRoleActorByNameAndType(String, String, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Returns the {@link Project}'s which contain a role actor of the specified name and type within any role. This is a method
     * that is provided so that you can efficiently tell which users or groups have been associated with
     * any role within projects.
     * @param currentUser the acting user.
     * @param name this is the name that the role actor is stored under (ex. username of 'admin', group name of
     * 'jira-users')
     * @param type this is the role type parameter, (ex. GroupRoleActor.TYPE, UserRoleActor.TYPE)
     * @param errorCollection will contain any errors in calling the method
     * @return a collection of {@link Project}'s which have a role which contains the role actor with the
     * specified name and type.
     */
    public Collection<Project> getProjectsContainingRoleActorByNameAndType(User currentUser, String name, String type, ErrorCollection errorCollection);

    public List<Long> roleActorOfTypeExistsForProjects(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #roleActorOfTypeExistsForProjects(java.util.List, com.atlassian.jira.security.roles.ProjectRole, String, String, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Returns a list of projectId's for which the role actor of the specified type exists in the project for the
     * provided projectRole. This is a method that is meant to efficiently allow discovery of whether a UserRoleActor
     * exists in a project role for a subset of projects.
     * @param currentUser the acting user.
     * @param projectsToLimitBy this will limit the range of projects the method queries. This is a list of Long, project
     * id's. The returned list will be either the same as this list or a subset.
     * @param projectRole the project role to find out if an actor is a member of.
     * @param projectRoleType the type of role actor you want to query for, in most cases this will be UserRoleActor.TYPE.
     * @param projectRoleParameter the parameter describing the role actor, in the case of a UserRoleActor this will be
     * the username.
     * @param errorCollection will contain any errors in calling the method
     * @return A list of Long, project id's. If a projectId is in this list then the project contains has a role
     * associated for the passed in actor and project role.
     */
    public List<Long> roleActorOfTypeExistsForProjects(User currentUser, List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter, ErrorCollection errorCollection);

    public Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #getProjectIdsForUserInGroupsBecauseOfRole(java.util.List, com.atlassian.jira.security.roles.ProjectRole, String, String, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     *
     * Returns a Map of Lists. The key of the map is a Long, project id and the value of the map is a list of group
     * names that the user is a member of for the project. This method is meant to provide an efficient means to
     * discover which groups that are associated with a project role implicitly include the specified user in that project
     * role. We allow you to specify a range of projectsToLimitBy so that you can perform only one query to find this
     * information for many projects for a single projectRole.
     * @param currentUser the acting user.
     * @param projectsToLimitBy this will limit the range of projects the method queries. This is a list of Long, project
     * id's. The returned list will be either the same as this list or a subset.
     * @param projectRole the project role to find out if an actor is a member of.
     * @param projectRoleType the type of role actor you want to query for, in most cases this will be UserRoleActor.TYPE.
     * @param userName the username to find out if the user is in the role because of a group
     * @param errorCollection will contain any errors in calling the method
     * @return Returns a Map of Lists. The key of the map is a Long, project id and the value of the map is a list of group
     * names that the user is a member of for the project.
     */
    public Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(User currentUser, List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName, ErrorCollection errorCollection);

    public boolean hasProjectRolePermission(Project project);

    /**
     * @deprecated Use {@link #hasProjectRolePermission(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.project.Project)} instead. Since v6.0.
     *
     * Will have permission to modify roles if they are a JIRA admin or, if in enterprise, the user is a project administrator.
     * @param currentUser the acting user.
     * @param project is the project to check permissions against
     * @return true if you have permission, false otherwise.
     */
    public boolean hasProjectRolePermission(User currentUser, Project project);
}
