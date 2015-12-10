package com.atlassian.jira.security.roles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.MultiMap;
import com.atlassian.jira.util.collect.MultiMaps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;

/**
 * This class allows us to CRUD ProjectRoles. A Project Role is way of grouping the users associated with a
 * project (eg 'Testers', 'Developers').
 */
public interface ProjectRoleManager
{
    /**
     * Get all the ProjectRoles available in JIRA. Currently this list is global.
     *
     * @return The global list of project roles in JIRA
     */
    Collection<ProjectRole> getProjectRoles();

    /**
     * This will return all the {@link ProjectRole}s that the user is currently a member of for the given project.
     *
     * @param user the user
     * @param project the project
     * @return a Collection of ProjectRoles.
     * @throws IllegalArgumentException is thrown if the <code>project</code> is null
     */
    Collection<ProjectRole> getProjectRoles(ApplicationUser user, Project project);

    /**
     * This will return all the {@link ProjectRole}s that the user is currently a member of for the given project.
     *
     *
     * @param user the user
     * @param project the project
     * @return a Collection of ProjectRoles.
     * @throws IllegalArgumentException is thrown if the <code>project</code> is null
     * @deprecated Use {@link this.getProjectRoles} instead. Since v6.0.
     */
    Collection<ProjectRole> getProjectRoles(User user, Project project);

    /**
     * Retrieves a project role object
     *
     * @param id
     * @return the project role based on the id or null if it doesn't exist.
     * @throws IllegalArgumentException is thrown if the <code>id</code> is null
     */
    ProjectRole getProjectRole(Long id);

    /**
     * Retrieves a project role object by name
     *
     * @param name
     * @return the project role based on the name or null if it doesn't exist.
     * @throws IllegalArgumentException is thrown if the <code>name</code> is null, or an empty String
     */
    ProjectRole getProjectRole(String name);

    /**
     * Creates a project role object
     *
     * @param projectRole the project role to create, if the id field is non-null then this will be ignored. Only the
     *                    roles name and description are used by this method.
     * @return the created project role object
     * @throws IllegalArgumentException is thrown if <code>projectRole</code> or <code>projectRole.getName</code> are null
     */
    ProjectRole createRole(ProjectRole projectRole);

    /**
     * Will tell you if a role name exists or not.
     *
     * @param name the name of the project role to check
     * @return true if unique, false if one already exists with that name
     */
    boolean isRoleNameUnique(String name);

    /**
     * Deletes a project role object
     *
     * @param projectRole
     * @throws IllegalArgumentException is thrown if the <code>projectRole</code> is null
     */
    void deleteRole(ProjectRole projectRole);

    /**
     * Updates a project role object
     *
     * @param projectRole
     * @throws IllegalArgumentException is thrown if the <code>projectRole</code> or <code>projectRole.getName()</code> is null
     */
    void updateRole(ProjectRole projectRole);

    /**
     * This method will retrieve the object that represents the actors associate with the given <code>projectRole</code>
     * and <code>project</code> context
     *
     * @param projectRole
     * @param project
     * @return the projectRoleActors object for the given projectRole and project context
     * @throws IllegalArgumentException if the given <code>projectRole</code> or <code>project</code> is null
     */
    ProjectRoleActors getProjectRoleActors(ProjectRole projectRole, Project project);

    /**
     * Commits the given ProjectRoleActors to permanent store, saving any updates made.
     *
     * @param projectRoleActors
     * @throws IllegalArgumentException if the given <code>projectRoleActors</code> is null or the projectRoleActors
     *                                  project, projectRole or roleActors is null.
     */
    void updateProjectRoleActors(ProjectRoleActors projectRoleActors);

    /**
     * This method will return the default role actors for a <code>ProjectRole</code>
     *
     * @param projectRole
     * @throws IllegalArgumentException will be thrown if <code>ProjectRole</code> is null
     */
    DefaultRoleActors getDefaultRoleActors(ProjectRole projectRole);

    /**
     * This method will update the associations of actors for the default projectRole, specified by the given
     * defaultRoleActors object. The actors will be updated to reflect the state of the roleActors set contained
     * within the given defaultRoleActors object.
     *
     * @param defaultRoleActors
     * @throws IllegalArgumentException if the given <code>defaultRoleActors</code> is null or the projectRole
     *                                  or roleActors is null.
     */
    void updateDefaultRoleActors(DefaultRoleActors defaultRoleActors);

    /**
     * This method will insert all the default roles into the role associations for the provided project. If one
     * of the default actors is already associated with the project, we will leave that association. NOTE: This method
     * is meant to only be called immediatly after a project is created.
     *
     * @param project the project to associate the role defaults with
     * @throws IllegalArgumentException if <code>project</code>
     */
    void applyDefaultsRolesToProject(Project project);

    /**
     * This will remove all role actors with the specified name and the specified type. This method should be used
     * to clean up after the actual subject of the role actor has been deleted (ex. deleting a user from the system).
     *
     *
     * @param key
     * @param type this is the role type parameter, (ex. GroupRoleActor.TYPE, UserRoleActor.TYPE)
     * @throws IllegalArgumentException if <code>name</code> or <code>type</code> is null
     */
    void removeAllRoleActorsByNameAndType(String key, String type);

    /**
     * Will remove all role actors associated with the specified project. This method should be used
     * to clean up just before the actual project has been deleted (ex. deleting a project from the system).
     *
     * @param project this is the project that the role actors are associated with
     * @throws IllegalArgumentException if the <code>project</code> is null
     */
    void removeAllRoleActorsByProject(Project project);

    /**
     * Returns true only if the given user is in the given project role for the
     * given project. This could be because they are a member of a particular
     * group (groups can be in roles) as well as being a user in a role.
     *
     * @param user        The user to check. If user is null, this will implicitly return false.
     * @param projectRole The role.
     * @param project     The project.
     * @return true if the User is in the role.
     * @throws IllegalArgumentException if <code>ProjectRole</code> or <code>Project</code> is null.
     */
    boolean isUserInProjectRole(ApplicationUser user, ProjectRole projectRole, Project project);

    /**
     * Returns true only if the given user is in the given project role for the
     * given project. This could be because they are a member of a particular
     * group (groups can be in roles) as well as being a user in a role.
     *
     *
     * @param user        The user to check. If user is null, this will implicitly return false.
     * @param projectRole The role.
     * @param project     The project.
     * @return true if the User is in the role.
     * @throws IllegalArgumentException if <code>ProjectRole</code> or <code>Project</code> is null.
     *
     * @deprecated Use {@link #isUserInProjectRole(com.atlassian.jira.user.ApplicationUser, ProjectRole, com.atlassian.jira.project.Project)} instead. Since v6.0.
     */
    boolean isUserInProjectRole(User user, ProjectRole projectRole, Project project);

    /**
     * Returns the project id's which contain a role actor of the specified name and type within any role. This is a method
     * that is provided so that you can efficiently tell which users or groups have been associated with
     * any role within projects.
     *
     * @param name this is the name that the role actor is stored under (ex. username of 'admin', group name of
     *             'jira-users')
     * @param type this is the role type parameter, (ex. GroupRoleActor.TYPE, UserRoleActor.TYPE)
     * @return a collection of Long project id's which have a role which contains the role actor with the
     *         specified name and type.
     */
    Collection<Long> getProjectIdsContainingRoleActorByNameAndType(String name, String type);

    /**
     * Returns a list of projectId's for which the role actor of the specified type exists in the project for the
     * provided projectRole. This is a method that is meant to efficiently allow discovery of whether a UserRoleActor
     * exists in a project role for a subset of projects.
     *
     * @param projectsToLimitBy    this will limit the range of projects the method queries. This is a list of Long, project
     *                             id's. The returned list will be either the same as this list or a subset.
     * @param projectRole          the project role to find out if an actor is a member of.
     * @param projectRoleType      the type of role actor you want to query for, in most cases this will be UserRoleActor.TYPE.
     * @param projectRoleParameter the parameter describing the role actor, in the case of a UserRoleActor this will be
     *                             the username.
     * @return A list of Long, project id's. If a projectId is in this list then the project contains has a role
     *         associated for the passed in actor and project role.
     */
    List<Long> roleActorOfTypeExistsForProjects(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter);

    /**
     * Returns a Map of Lists. The key of the map is a Long, project id and the value of the map is a list of group
     * names that the user is a member of for the project. This method is meant to provide an efficient means to
     * discover which groups that are associated with a project role implicitly include the specified user in that project
     * role. We allow you to specify a range of projectsToLimitBy so that you can perform only one query to find this
     * information for many projects for a single projectRole.
     *
     * @param projectsToLimitBy this will limit the range of projects the method queries. This is a list of Long, project
     *                          id's. The returned list will be either the same as this list or a subset.
     * @param projectRole       the project role to find out if an actor is a member of.
     * @param projectRoleType   the type of role actor you want to query for, in most cases this will be UserRoleActor.TYPE.
     * @param userName          the username to find out if the user is in the role because of a group
     * @return Returns a Map of Lists. The key of the map is a Long, project id and the value of the map is a list of group
     *         names that the user is a member of for the project.
     */
    Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName);

    ProjectIdToProjectRoleIdsMap createProjectIdToProjectRolesMap(ApplicationUser user, Collection<Long> projectIds);

    /**
     * @deprecated Use {@link #createProjectIdToProjectRolesMap(com.atlassian.jira.user.ApplicationUser, java.util.Collection)} instead. Since v6.0.
     */
    ProjectIdToProjectRoleIdsMap createProjectIdToProjectRolesMap(User user, Collection<Long> projectIds);

    /**
     * This class implements is backed by the map with project ID as a key and
     * a collection of project role IDs as the mapped value of the map.
     */
    public static class ProjectIdToProjectRoleIdsMap implements Iterable<ProjectIdToProjectRoleIdsMap.Entry>
    {
        private final MultiMap<Long, Long, List<Long>> map = MultiMaps.create(new Supplier<List<Long>>()
        {
            public List<Long> get()
            {
                return new ArrayList<Long>();
            }
        });

        /*
         * NOTE: Null parameters are silently ignored - nothing is added.
         */
        public void add(final Long projectId, final Long projectRoleId)
        {
            if ((projectId != null) && (projectRoleId != null))
            {
                map.putSingle(projectId, projectRoleId);
            }
        }

        /**
         * Returns true if the map is empty, false otherwise
         * @return true if the map is empty, false otherwise
         */
        public boolean isEmpty()
        {
            return map.isEmpty();
        }

        /**
         * Returns an iterator for this map
         * @return an iterator
         */
        public Iterator<Entry> iterator()
        {
            return CollectionUtil.transformIterator(map.entrySet().iterator(), new Function<Map.Entry<Long, List<Long>>, Entry>()
            {
                public Entry get(final java.util.Map.Entry<Long, List<Long>> entry)
                {
                    return new Entry(entry.getKey(), entry.getValue());
                }
            });
        }

        /**
         * Map entry that holds the project id and the collection of project
         * role ids
         */
        public static class Entry
        {
            private final Long projectId;
            private final List<Long> projectRoleIds;

            private Entry(final Long projectId, final List<Long> projectRoleIds)
            {
                this.projectId = projectId;
                this.projectRoleIds = unmodifiableList(new ArrayList<Long>(projectRoleIds));
            }

            /**
             * Returns the project ID
             * @return project ID
             */
            public Long getProjectId()
            {
                return projectId;
            }

            /**
             * Returns the project role ID
             * @return project role ID
             */
            public List<Long> getProjectRoleIds()
            {
                return projectRoleIds;
            }
        }
    }
}
