/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import java.util.Collection;
import java.util.List;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.project.Project;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public interface SchemeManager
{
    /** The one and only "association type". */
    public static final String PROJECT_ASSOCIATION = "ProjectScheme";

    /**
     * Gets a scheme based on the Id of the scheme
     *
     * @param id Id of the scheme
     * @return The scheme
     *
     * @throws GenericEntityException DB Error
     * @deprecated Use {@link #getSchemeObject(Long)} instead. Since v5.0.
     */
    GenericValue getScheme(Long id) throws GenericEntityException;

    /**
     * Gets a scheme by id from the database.
     * @param id the id of the scheme to get.
     * @return the Scheme
     * @throws DataAccessException if there is trouble retrieving from the database.
     */
    Scheme getSchemeObject(Long id) throws DataAccessException;

    /**
     * Gets all the Schemes (of the type defined by the subclass) from the database.
     * @return the schemes.
     * @throws GenericEntityException DB Error
     * @deprecated use {@link #getSchemeObjects()}. Since 2010.
     */
    List<GenericValue> getSchemes() throws GenericEntityException;

    /**
     * Gets all scheme objects in the database.
     * @return the schemes.
     * @throws DataAccessException if the database is down or equivalent.
     */
    List<Scheme> getSchemeObjects() throws DataAccessException;

    /**
     * Gets all schemes of this type in the database taht are associated with at least one project.
     *
     * @param withEntitiesComparable if true then the scheme entites will be logically comparable (they will not include
     * database specific information such as the pk id). Otherwise the object will be a full representation of the row
     * stored in the database.
     * @return List of associated schemes
     */
    List<Scheme> getAssociatedSchemes(boolean withEntitiesComparable);

    /**
     * Gets a scheme by name from the database.
     * @param name the name of the scheme to get.
     * @return the Scheme
     * @throws GenericEntityException DB error
     * @deprecated use {@link #getSchemeObject(String)} instead. Since 2010.
     */
    GenericValue getScheme(String name) throws GenericEntityException;

    /**
     * Gets a scheme by name from the database.
     *
     * @param name the name of the scheme to get.
     * @return the Scheme
     *
     * @throws DataAccessException if there is trouble retrieving from the database.
     */
    Scheme getSchemeObject(String name) throws DataAccessException;

    /**
     * Get all schemes of this type attached to the given project.
     *
     * @param project The project that the schemes are attached to
     * @return List of schemes
     * @throws GenericEntityException If a DB error occurs
     *
     * @deprecated Use {@link #getSchemeFor(com.atlassian.jira.project.Project)} instead. Since v5.0.
     */
    List<GenericValue> getSchemes(GenericValue project) throws GenericEntityException;

    /**
     * Get the scheme of this type attached to the given project.
     *
     * @param project The project
     * @return Thoe associated schem for this project.
     */
    Scheme getSchemeFor(Project project);

    /**
     * Determine if the given scheme name exists.
     *
     * @param name The name of the scheme
     * @return true is the schem exists.
     * @throws GenericEntityException If a DB error occurs
     */
    boolean schemeExists(String name) throws GenericEntityException;

    /**
     * Creates a new scheme
     *
     * @param name The name of the new scheme
     * @param description The description of the new scheme
     * @return The new scheme object
     * @throws GenericEntityException If a DB error occurs
     *
     * @deprecated Use {@link #createSchemeObject(String, String)} instead. Since v5.0.
     */
    GenericValue createScheme(String name, String description) throws GenericEntityException;

    /**
     * Creates a new scheme
     *
     * @param name The name of the new scheme
     * @param description The description of the new scheme
     * @return The new scheme object
     */
    Scheme createSchemeObject(String name, String description);

    Scheme createSchemeAndEntities(Scheme scheme) throws DataAccessException;

    /**
     * Gets a scheme entity based on the id of the entity
     *
     * @param id The id of the entity
     * @return The scheme entity object
     * @throws GenericEntityException If a DB error occurs
     */
    GenericValue getEntity(Long id) throws GenericEntityException;

    /**
     * Get all Scheme entity records for a particular scheme
     *
     * @param scheme The scheme that the entities belong to
     * @return List of (GenericValue) entities
     * @throws GenericEntityException If a DB error occurs
     */
    List<GenericValue> getEntities(GenericValue scheme) throws GenericEntityException;

    List<GenericValue> getEntities(GenericValue scheme, Long entityTypeId) throws GenericEntityException;

    List<GenericValue> getEntities(GenericValue scheme, String entityTypeId) throws GenericEntityException;

    List<GenericValue> getEntities(GenericValue scheme, Long entityTypeId, String parameter) throws GenericEntityException;

    List<GenericValue> getEntities(GenericValue scheme, String type, Long entityTypeId) throws GenericEntityException;

    /**
     * Updates any changes to the given scheme
     *
     * @param entity The modified scheme
     * @throws GenericEntityException If a DB error occurs
     *
     * @deprecated Use {@link #updateScheme(Scheme)} instead. Since v5.0.
     */
    void updateScheme(GenericValue entity) throws GenericEntityException;

    /**
     * Updates any changes to the scheme object. This does not include changes to the scheme entities.
     *
     * @param scheme The modified scheme object
     */
    void updateScheme(Scheme scheme);

    /**
     * Deletes a scheme from the database
     *
     * @param id Id of the scheme to be deleted
     * @throws GenericEntityException If a DB error occurs
     */
    void deleteScheme(Long id) throws GenericEntityException;

    /**
     * Adds a scheme to a particular project
     *
     * @param project The project that the scheme is to be added to
     * @param scheme The scheme to be added
     *
     * @throws GenericEntityException If a DB error occurs
     *
     * @deprecated Use {@link #addSchemeToProject(com.atlassian.jira.project.Project, Scheme)} instead. Since v5.0.
     */
    void addSchemeToProject(GenericValue project, GenericValue scheme) throws GenericEntityException;

    /**
     * Adds a scheme to a particular project
     *
     * @param project The project that the scheme is to be added to
     * @param scheme The scheme to be added
     */
    void addSchemeToProject(Project project, Scheme scheme);

    /**
     * Removes all schemes from a project
     *
     * @param project The project that all schemes are to be deleted from
     *
     * @throws GenericEntityException If a DB error occurs
     *
     * @deprecated Use {@link #removeSchemesFromProject(com.atlassian.jira.project.Project)} instead. Since v4.4.
     */
    void removeSchemesFromProject(GenericValue project) throws GenericEntityException;

    /**
     * Removes all schemes from a project
     *
     * @param project The project that all schemes are to be deleted from
     */
    void removeSchemesFromProject(Project project);

    GenericValue createSchemeEntity(GenericValue scheme, SchemeEntity entity) throws GenericEntityException;

    /**
     * Deletes an entity with the given id from the database.
     *
     * @param id The id of the entity to be deleted
     * @throws GenericEntityException If a DB error occurs
     */
    void deleteEntity(Long id) throws GenericEntityException;

    /**
     * Gets all projects that are associated with that scheme
     *
     * @param scheme The scheme used to get all projects
     * @return List of (GenericValue) projects
     * @deprecated Use {@link #getProjects(Scheme)} instead. Since 2009.
     * @throws GenericEntityException If a DB error occurs
     */
    @Deprecated
    List<GenericValue> getProjects(GenericValue scheme) throws GenericEntityException;

    /**
     * Gets all projects that are associated with that scheme
     *
     * @param scheme The scheme used to get all projects
     * @return List of {@link Project}'s
     */
    List<Project> getProjects(Scheme scheme);

    /**
     * Creates a default scheme, with an id of 0
     *
     * @return The new permission scheme object
     * @throws GenericEntityException If a DB error occurs
     */
    GenericValue createDefaultScheme() throws GenericEntityException;

    /**
     * Gets the default scheme. This should have an id of 0
     *
     * This does not work for the Default Notification scheme as it does not have an id of 0.
     *
     * @return The default scheme
     * @throws GenericEntityException If a DB error occurs
     *
     * @deprecated Use {@link #getDefaultSchemeObject()} instead. Since v5.0.
     */
    GenericValue getDefaultScheme() throws GenericEntityException;

    /**
     * Gets the default scheme. This should have an id of 0
     *
     * This does not work for the Default Notification scheme as it does not have an id of 0.
     *
     * @return The default scheme
     */
    Scheme getDefaultSchemeObject();

    /**
     * Adds the default scheme to a particular project
     *
     * @param project The project that the scheme is to be added to
     * @throws GenericEntityException If a DB error occurs
     *
     * @deprecated Use {@link #addDefaultSchemeToProject(com.atlassian.jira.project.Project)} instead. Since v5.0.
     */
    void addDefaultSchemeToProject(GenericValue project) throws GenericEntityException;

    /**
     * Adds the default scheme to a particular project
     *
     * @param project The project that the scheme is to be added to
     */
    void addDefaultSchemeToProject(Project project);

    /**
     * Copys a scheme, giving the new scheme the same entities as the original one
     *
     * @param scheme The permission scheme to be copied
     * @return The new permission scheme
     * @throws GenericEntityException If a DB error occurs
     *
     * @deprecated Use {@link #copyScheme(Scheme)} instead. Since v5.0.
     */
    GenericValue copyScheme(GenericValue scheme) throws GenericEntityException;

    /**
     * Copys a scheme, giving the new scheme the same entities as the original one
     *
     * @param scheme The permission scheme to be copied
     * @return The new permission scheme
     */
    Scheme copyScheme(Scheme scheme);

    /**
     * Checks anonymous permission of the given permission type for the given entity.
     *
     * @param entityType permission type.
     * @param entity     the entity to which permission is being checked.
     * @return true only if the anonymous user is permitted.
     */
    boolean hasSchemeAuthority(Long entityType, GenericValue entity);

    /**
     * Checks the given user's permission of the given permission type for the given entity.
     *
     * @param entityType    permission type.
     * @param entity        the entity to which permission is being checked.
     * @param user          the user.
     * @param issueCreation whether the permission is for creating an issue.
     * @return true only if the user is permitted.
     */
    boolean hasSchemeAuthority(Long entityType, GenericValue entity, User user, boolean issueCreation);

    /**
     * Retrieves all the entites for this permission and then removes them.
     *
     * @param scheme to remove entites from must NOT be null
     * @param entityTypeId to remove
     * @return True all the time (legacy)
     *
     * @deprecated Use {@link #getEntities(org.ofbiz.core.entity.GenericValue, Long)} and {@link #deleteEntity(Long)} instead. Since v5.0.
     * @throws RemoveException if the delete fails (DB error)
     */
    boolean removeEntities(GenericValue scheme, Long entityTypeId) throws RemoveException;

    /**
     * Get all entity records with a particular parameter
     *
     * @param type      The type of entity you wish to retrieve, eg 'user', 'group', 'projectrole'
     * @param parameter The parameter in the entity
     * @return List of (GenericValue) entities
     * @throws GenericEntityException If a DB error occurs
     */
    List<GenericValue> getEntities(String type, String parameter) throws GenericEntityException;

    /**
     * Removes all scheme entities with this parameter and type
     *
     * @param type      the 'type' of entity you are deleting, eg 'group', 'user', 'projectrole'
     * @param parameter must NOT be null
     * @return true always (legacy)
     * @throws RemoveException if the delete fails (DB error)
     */
    boolean removeEntities(String type, String parameter) throws RemoveException;

    /**
     * @deprecated Use {@link #getGroups(Long, com.atlassian.jira.project.Project)} instead. Since v5.0.
     */
    Collection<Group> getGroups(Long permissionId, GenericValue project);

    Collection<Group> getGroups(Long permissionId, Project project);

    Collection<User> getUsers(Long permissionId, Project project);

    Collection<User> getUsers(Long permissionId, Issue issue);

    /**
     * @deprecated Use {@link #getUsers(Long, com.atlassian.jira.permission.PermissionContext)} instead. Since 2006.
     */
    @Deprecated
    Collection<User> getUsers(Long permissionId, GenericValue issueOrProject);

    Collection<User> getUsers(Long permissionId, PermissionContext ctx);

    /**
     * Will return all @link Scheme objects that are not currently associated with any projects.
     *
     * @return list of @link Scheme objects
     * @throws DataAccessException if the database is down or equivalent.
     */
    List<Scheme> getUnassociatedSchemes() throws DataAccessException;

}
