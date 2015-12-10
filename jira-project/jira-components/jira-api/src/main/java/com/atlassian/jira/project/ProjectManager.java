/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.user.ApplicationUser;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Implementations of this interface are responsible for all management of project entities within JIRA.
 */
public interface ProjectManager
{
    /**
     * Creates the project in the database, and adds default project roles for this project.  If no name, key or
     * lead are provided an exception will be thrown.
     *
     * @param name The name of the new project
     * @param key The project key of the new project
     * @param description An optional description for the project
     * @param lead The lead developer for the project
     * @param url An optional URL for the new project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @return The newly created project.
     */
    Project createProject(String name, String key, String description, String lead, String url, Long assigneeType);

    /**
     * Creates the project in the database, and adds default project roles for this project.  If no name, key or
     * lead are provided an exception will be thrown.
     * <p>
     * <strong>WARNING</strong>: In 6.0, the documentation for this method incorrectly stated that this method would
     * interpret the {@code lead} as a username, when it was in fact interpreted as a userkey.  The method signatures
     * and documentation have been updated to describe the actual behaviour.
     *
     * @param name The name of the new project
     * @param key The project key of the new project
     * @param description An optional description for the project
     * @param leadKey The userkey of the lead developer for the project
     * @param url An optional URL for the new project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @param avatarId the id of an existing system avatar.
     * @return The newly created project.
     */
    Project createProject(String name, String key, String description, String leadKey, String url, Long assigneeType, Long avatarId);

    /**
     * Updates the project provided with the new attributes passed in.  This method is responsible for persisting
     * any changes to the database.

     * @param originalProject The project to be updated.
     * @param name The name for the updated project
     * @param description An optional description for the project
     * @param leadKey The userkey of the lead developer for the project
     * @param url An optional URL for the updated project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @return The updated project
     */
    Project updateProject(Project originalProject, String name, String description, String leadKey, String url, Long assigneeType);

    /**
     * Updates the project provided with the new attributes passed in.  This method is responsible for persisting
     * any changes to the database.

     * @param originalProject The project to be updated.
     * @param name The name for the updated project
     * @param description An optional description for the project
     * @param leadKey The userkey of the lead developer for the project
     * @param url An optional URL for the updated project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @param projectKey The new project key (not updated if null)
     * @return The updated project
     * @since v6.1
     */
    Project updateProject(Project originalProject, String name, String description, String leadKey, String url, Long assigneeType, Long avatarId, String projectKey);

    /**
     * Updates the project provided with the new attributes passed in.  This method is responsible for persisting
     * any changes to the database.

     * @param originalProject The project to be updated.
     * @param name The name for the updated project
     * @param description An optional description for the project
     * @param leadKey The userkey of the lead developer for the project
     * @param url An optional URL for the updated project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @param avatarId the id of an existing avatar.
     * @return The updated project
     */
    Project updateProject(Project originalProject, String name, String description, String leadKey, String url, Long assigneeType, Long avatarId);


    /**
     * Removes all issues for a particular project.  A RemoveException will be thrown on any errors removing issues.
     *
     * @param project The project for which issues will be deleted.
     * @throws RemoveException if there's any errors removing issues
     */
    void removeProjectIssues(Project project) throws RemoveException;

    /**
     * Deletes the provided project from the database.
     * Please note that this method only deletes the project itself and not any related entities.
     * Use {@link com.atlassian.jira.bc.project.ProjectService#deleteProject} to
     * properly delete a project.
     *
     * @param project The project to be deleted.
     */
    public void removeProject(Project project);

    /**
     * Returns a project {@link GenericValue} that an issue is in.
     *
     * @param issue the issue.
     * @return GenericValue representation of a Project.
     * @throws DataAccessException If any errors occur accessing the DB.
     * @deprecated Use {@link com.atlassian.jira.issue.Issue#getProjectObject()} instead. Deprecated since v4.0.
     */
    @Deprecated
    GenericValue getProject(GenericValue issue) throws DataAccessException;

    /**
     * Retrieve a single project by it's id.
     *
     * @param id the Project ID.
     * @return GenericValue representation of a Project.
     * @throws DataAccessException If any errors occur accessing the DB.
     * @deprecated please use {@link #getProjectObj}
     */
    @Deprecated
    GenericValue getProject(Long id) throws DataAccessException;

    /**
     * Retrieves a single {@link Project} by its id.
     *
     * @param id ID of the Project.
     * @return Project object or null if project with that id doesn't exist.
     * @throws DataAccessException If any errors occur accessing the DB.
     */
    Project getProjectObj(Long id) throws DataAccessException;

    /**
     * Retrieve a single project by it's project name.
     *
     * @param name project name
     * @return GenericValue representation of a Project.
     * @throws DataAccessException If any errors occur accessing the DB.
     * @deprecated Please use {@link #getProjectObjByName} instead. Deprecated since v4.0.
     */
    @Deprecated
    GenericValue getProjectByName(String name) throws DataAccessException;

    /**
     * Returns a {@link Project} object based on the passed in project name.
     *
     * @param projectName the name of the project
     * @return the {@link Project} object specified by the supplied name or null
     */
    Project getProjectObjByName(String projectName);

    /**
     * Retrieve a single project by it's project key.
     *
     * @param key the project key
     * @return GenericValue representation of a Project.
     * @throws DataAccessException If any errors occur accessing the DB.
     * @deprecated please use the {@link #getProjectObjByKey} method to return a {@link Project} object
     */
    @Deprecated
    GenericValue getProjectByKey(String key) throws DataAccessException;

    /**
     * Returns a {@link Project} object based on the passed in project key.
     *
     * @param projectKey the Project key.
     * @return the {@link Project} object specified by the key or null
     */
    Project getProjectObjByKey(String projectKey);

    /**
     * Returns the {@link Project} with the given project key.
     * <p>
     * This method will strictly only return the project whose current project key is the one given.
     * <p>
     * This method is added to the API in anticipation of being able to edit the project key, but this feature has not
     * actually been added in 6.0.
     *
     * @param projectKey the Project key.
     * @return the {@link Project} with the given project key.
     *
     * @since 6.0
     * @see #getProjectObjByKey(String)
     */
    @ExperimentalApi
    Project getProjectByCurrentKey(String projectKey);

    /**
     * Returns a {@link Project} object based on the passed in project key, not taking into account the case
     * of the project key.
     *
     * @param projectKey the project key, case does not matter.
     * @return the project object specified by the key or null if no such project exists.
     */
    Project getProjectByCurrentKeyIgnoreCase(final String projectKey);

    /**
     * Returns a {@link Project} object based on the passed in project key, not taking into account the case
     * of the project key. Matches also by previous keys that were associated with a project.
     *
     * @param projectKey the project key, case does not matter.
     * @return the project object specified by the key or null if no such project exists.
     */
    Project getProjectObjByKeyIgnoreCase(final String projectKey);

    /**
     * Returns all project keys that are associated with {@link Project}.
     *
     * @return all project keys (including the current one) associated with the project
     */
    Set<String> getAllProjectKeys(final Long projectId);

    /**
     * Get a component from within this project, by it's id.
     *
     * @param id id
     * @return A component {@link GenericValue}.
     * @throws DataAccessException If any errors occur accessing the DB.
     * @deprecated Use ProjectComponentManager instead. Deprecated in v4.0.
     * @see com.atlassian.jira.bc.project.component.ProjectComponentManager#find(Long)
     */
    @Deprecated
    GenericValue getComponent(Long id) throws DataAccessException;

    /**
     * Get a component from within this project, based on the project and the component name.
     * <b>NOTE:</b> This is not cached currently.
     *
     * @param project project
     * @param name name
     * @return A component {@link GenericValue}.
     * @throws DataAccessException If any errors occur accessing the DB.
     * @deprecated Use ProjectComponentManager instead. Deprecated in v4.0.
     * @see com.atlassian.jira.bc.project.component.ProjectComponentManager#findByComponentName
     */
    @Deprecated
    GenericValue getComponent(GenericValue project, String name) throws DataAccessException;

    /**
     * Returns a collection of components in a project.
     *
     * @param project project
     * @return A collection of {@link GenericValue}s.
     * @throws DataAccessException If any errors occur accessing the DB.
     * @deprecated Use ProjectComponentManager instead. Deprecated in v4.0.
     * @see com.atlassian.jira.bc.project.component.ProjectComponentManager#findAllForProject
     */
    @Deprecated
    Collection<GenericValue> getComponents(GenericValue project) throws DataAccessException;

    /**
     * Return all project {@link GenericValue}s.
     *
     * @throws DataAccessException If any errors occur accessing the DB.
     * @return all projects as Collection<GenericValue>
     * @deprecated Use {@link #getProjectObjects()} instead. Deprecated in v4.0.
     */
    Collection<GenericValue> getProjects() throws DataAccessException;

    /**
     * Return all {@link Project}s ordered by name.
     *
     * @throws DataAccessException If any errors occur accessing the DB.
     * @return all projects ordered by name.
     */
    List<Project> getProjectObjects() throws DataAccessException;

    /**
     * Return the total number of {@link Project}s.
     *
     * @return A long value representing tht total number of projects.
     * @throws DataAccessException if any errors occur accessing the DB.
     */
    long getProjectCount() throws DataAccessException;

    /**
     * Get the next issue ID from this project (transactional).
     * Each project maintains an internal counter for the number of issues.
     * This method may be used to construct a new issue key.
     *
     * @param project The Project
     * @return A long value representing a new issue id for the project.
     * @throws DataAccessException If any errors occur accessing the DB.
     */
    long getNextId(Project project) throws DataAccessException;

    /**
     * Causes a full refresh of the project cache.
     */
    void refresh();

    /**
     * Get all ProjectCategories.
     *
     * @return A collection of category {@link GenericValue}s
     * @throws DataAccessException If any errors occur accessing the DB.
     * @deprecated since v4.4. Use {@link #getAllProjectCategories()} instead.
     */
    Collection<GenericValue> getProjectCategories() throws DataAccessException;

    /**
     * Returns all ProjectCategories, ordered by name.
     *
     * @return all ProjectCategories, ordered by name.
     * @throws DataAccessException If any errors occur accessing the DB.
     */
    Collection<ProjectCategory> getAllProjectCategories() throws DataAccessException;

    /**
     * Returns a single project category by id.
     *
     * @param id Project Category ID.
     * @return A category {@link GenericValue}
     *
     * @deprecated Use {@link #getProjectCategoryObject(Long)} instead. Since v4.4.
     */
    GenericValue getProjectCategory(Long id) throws DataAccessException;

    /**
     * Returns a single project category by id.
     *
     * @param id Project Category ID.
     * @return The project category
     */
    ProjectCategory getProjectCategoryObject(Long id) throws DataAccessException;

    /**
     * Find a project category by name.
     * <b>NOTE:</B> The current implementation is not the most efficient.
     *
     * @param projectCategoryName Name of the Project Category
     * @return A category {@link GenericValue}
     * @throws DataAccessException If any errors occur accessing the DB.
     * @deprecated Use {@link #getProjectCategoryObjectByName(String)} instead. Since v4.4.
     */
    GenericValue getProjectCategoryByName(String projectCategoryName) throws DataAccessException;

    /**
     * Find a project category by name.
     *
     * @param projectCategoryName Name of the Project Category
     * @return The ProjectCategory or null if none found
     */
    ProjectCategory getProjectCategoryObjectByName(String projectCategoryName);

    /**
     * Find a project category by name ignoring the case of the category name.
     *
     * @param projectCategoryName Name of the Project Category
     * @return The ProjectCategory or null if none found
     */
    ProjectCategory getProjectCategoryObjectByNameIgnoreCase(String projectCategoryName);

    /**
     * Find a project category by name ignoring the case of the category name.
     * <b>NOTE:</B> The current implementation is not the most efficient.
     *
     * @param projectCategoryName Name of the Project Category
     * @return A category {@link GenericValue}
     * @throws DataAccessException If any errors occur accessing the DB.
     * @deprecated Use {@link #getProjectCategoryObjectByNameIgnoreCase(String)} instead. Since v4.4.
     */
    GenericValue getProjectCategoryByNameIgnoreCase(String projectCategoryName);

    /**
     * Persist an updated project category.
     *
     * @param projectCat project category.
     * @throws DataAccessException If any errors occur accessing the DB.
     * @deprecated Use {@link #updateProjectCategory(ProjectCategory)} instead. Since v4.4.
     */
    void updateProjectCategory(GenericValue projectCat) throws DataAccessException;

    /**
     * Persist an updated project category.
     *
     * @param projectCategory project category.
     * @throws DataAccessException If any errors occur accessing the DB.
     */
    void updateProjectCategory(ProjectCategory projectCategory) throws DataAccessException;

    /**
     * Returns a list of projects in a particular category.
     *
     * @param projectCategory project category.
     * @return A collection of project {@link GenericValue}s sorted by name.
     * @throws DataAccessException If any errors occur accessing the DB.
     *
     * @deprecated Use {@link #getProjectsFromProjectCategory(ProjectCategory)} instead. Since v4.4.
     */
    Collection<GenericValue> getProjectsFromProjectCategory(GenericValue projectCategory) throws DataAccessException;

    /**
     * Returns a list of projects in a particular category.
     *
     * @param projectCategory project category.
     * @return A collection of projects sorted by name.
     * @throws DataAccessException If any errors occur accessing the DB.
     */
    Collection<Project> getProjectsFromProjectCategory(ProjectCategory projectCategory) throws DataAccessException;

    /**
     * Returns a list of projects in a particular category.
     *
     * @param projectCategoryId project category id.
     * @return A collection of project {@link Project}s sorted by name.
     * @throws DataAccessException If any errors occur accessing the DB.
     * @since v4.0
     */
    Collection<Project> getProjectObjectsFromProjectCategory(Long projectCategoryId) throws DataAccessException;

    /**
     * Returns a list of projects without project category, sorted by project name
     *
     * @return A collection of project {@link GenericValue}s sorted by name
     * @throws DataAccessException If any errors occur accessing the DB.
     * @deprecated Use {@link #getProjectObjectsWithNoCategory()} instead. Since v4.4.
     */
    Collection<GenericValue> getProjectsWithNoCategory() throws DataAccessException;

    /**
     * Returns a list of projects without project category, sorted by project name
     *
     * @return A collection of {@link com.atlassian.jira.project.Project}s sorted by name
     * @throws DataAccessException If any errors occur accessing the DB.
     * @since v4.0
     */
    Collection<Project> getProjectObjectsWithNoCategory() throws DataAccessException;

    /**
     * Returns a project's category.
     *
     * @param project project
     * @return A category {@link GenericValue} or null if no category exists.
     * @throws DataAccessException If any errors occur accessing the DB.
     *
     * @deprecated Use {@link #getProjectCategoryForProject} instead. Since 4.4
     */
    GenericValue getProjectCategoryFromProject(GenericValue project) throws DataAccessException;

    /**
     * Returns a project's category.
     *
     * @param project project
     * @return A ProjectCategory or null if this project has no category.
     * @throws DataAccessException If any errors occur accessing the DB.
     * @since 4.4
     */
    ProjectCategory getProjectCategoryForProject(Project project) throws DataAccessException;

    /**
     * Sets a projects category.
     *
     * @param project project
     * @param category category
     * @throws DataAccessException If any errors occur accessing the DB.
     * @throws IllegalArgumentException if the project provided is null
     * @deprecated since v4.4. Use {@link #setProjectCategory(com.atlassian.jira.project.Project, com.atlassian.jira.project.ProjectCategory)} instead.
     */
    void setProjectCategory(GenericValue project, GenericValue category) throws DataAccessException;

    /**
     * Sets a project's category.
     *
     * @param project project
     * @param category category
     * @throws DataAccessException If any errors occur accessing the DB.
     * @throws IllegalArgumentException if the project provided is null
     */
    void setProjectCategory(Project project, ProjectCategory category) throws DataAccessException;

    /**
     * Creates a new ProjectCategory with the given name and description.
     *
     * @param name the Name
     * @param description the Description.
     * @return the new ProjectCategory.
     */
    ProjectCategory createProjectCategory(String name, String description);

    /**
     * Removes the given ProjectCategory.
     *
     * @param id the ProjectCategory to remove.
     */
    void removeProjectCategory(Long id);

    /**
     * Checks if there is a valid default assignee for a given project.
     *
     * @param project project
     * @return False if no assignee type is set for a project and unassigned issues are not allowed, and the projectlead is not assignable.
     *         Also false, if either the assigneetype is not unassigned or unassigned issues are not allowed and the projectlead is not assignable.
     *         Otherwise returns TRUE since there is a default assignee.
     * @deprecated Use {@link #getDefaultAssignee(Project, java.util.Collection)} and check for DefaultAssigneeException. Since v4.4.
     */
    boolean isDefaultAssignee(GenericValue project);

    /**
     * This function checks if there is a valid default assignee set in the system<br>
     * If this returns false then the {@link #getDefaultAssignee(GenericValue, GenericValue)} will throw an exception
     *
     * @param project project
     * @param component component
     * @return true if either the component is not null and has an assignee type != project default, or if
     *         {@link #isDefaultAssignee(org.ofbiz.core.entity.GenericValue)} is true
     * @deprecated Use {@link #getDefaultAssignee(Project, java.util.Collection)} and check for DefaultAssigneeException. Since v4.4.
     */
    boolean isDefaultAssignee(GenericValue project, GenericValue component);

    /**
     * Gets the default assignee for a project and/or component depending on if a component was specified.
     *
     * @param project project
     * @param component component
     * @return A {@link User}
     * @throws DefaultAssigneeException If the default assignee does NOT have ASSIGNABLE permission OR Unassigned issues are turned off.
     *
     * @deprecated Please use {@link #getDefaultAssignee(Project, ProjectComponent)}. Since v4.3
     */
    User getDefaultAssignee(GenericValue project, GenericValue component);

    /**
     * Gets the default assignee for a project and/or component depending on if a component was specified.
     *
     * @param project project
     * @param component component
     * @return the default assignee for this project/component
     * @throws DefaultAssigneeException If the default assignee does NOT have ASSIGNABLE permission OR Unassigned issues are turned off.
     *
     * @deprecated Use {@link #getDefaultAssignee(Project, java.util.Collection)} which allows for multiple components. Since v4.4.
     */
    User getDefaultAssignee(Project project, ProjectComponent component);

    /**
     * Gets the default assignee for an issue given its project and list of Components.
     * <p>
     * If the default assignee configuration is invalid, then a DefaultAssigneeException is thrown.
     * This could be because the default is unassigned, and unassigned issues are not allowed, or because the default user
     * does not have permission to be assigned to issues in this project.
     *
     * @param project project
     * @param components The components
     * @return the default assignee for this project/components
     * @throws DefaultAssigneeException If the default assignee is invalid (eg user does not have assign permission) .
     */
    User getDefaultAssignee(Project project, Collection<ProjectComponent> components) throws DefaultAssigneeException;

    /**
     * Returns all the projects that leadUser is the project lead for ordered by the name of the Project.
     *
     * @param leadUser Project Lead
     * @return A collection of projects
     */
    List<Project> getProjectsLeadBy(User leadUser);

    /**
     * Returns all the projects that leadUser is the project lead for ordered by the name of the Project.
     *
     * @param leadUser Project Lead
     * @return A collection of projects
     */
    List<Project> getProjectsLeadBy(ApplicationUser leadUser);

    /**
     * Returns all the projects that leadUser is the project lead for.
     *
     * @param leadUser Project Lead
     * @return A collection of project {@link GenericValue}s
     *
     * @deprecated Use {@link #getProjectsLeadBy(com.atlassian.crowd.embedded.api.User)} instead
     */
    Collection<GenericValue> getProjectsByLead(User leadUser);

    /**
     * Converts a collection of projectIds to a collection of projects. Will return null if is null or blank
     *
     * @param projectIds a Collection of Longs
     * @return List of Project {@link GenericValue}s. Null if input is empty
     *
     * @deprecated Use {@link #convertToProjectObjects(java.util.Collection)} instead. Since v4.4.
     */
    List<GenericValue> convertToProjects(final Collection<Long> projectIds);

    /**
     * Converts a collection of projectIds to a list of projects.
     * <p>
     * Will return null if incoming collection is null.
     * <p>
     * The returned list of Project Objects will have the same sort order as the incoming collection of IDs.
     *
     * @param projectIds a Collection of Project IDs
     * @return List of Projects, or null if input is null
     */
    List<Project> convertToProjectObjects(final Collection<Long> projectIds);

    /**
     * Returns the curremt issue counter for the given project. This value is for information only; you should
     * not use it to predict or create issue ids, because it may change concurrently as new issues are created.
     *
     * @param id the ID of the project for which to retrieve the counter
     * @return the current project counter (the id of the next issue to be created).
     */
    long getCurrentCounterForProject(Long id);

    /**
     * Set the project counter. <b>Warning</b> Setting the project counter is not needed in the normal
     * operations of JIRA, this method exist for functionality like project-import etc.
     *
     * @param project the project for which to set the counter (required)
     * @param counter the counter value to set
     */
    void setCurrentCounterForProject(Project project, long counter);
}
